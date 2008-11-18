/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zigva.exec;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.zigva.collections.CircularBuffer;
import com.google.zigva.guice.ZigvaThreadFactory;
import com.google.zigva.guice.ZystemSelfBuilder;
import com.google.zigva.io.DataNotReadyException;
import com.google.zigva.io.DataSourceClosedException;
import com.google.zigva.io.EndOfDataException;
import com.google.zigva.io.ForkingSink;
import com.google.zigva.io.Sink;
import com.google.zigva.io.SinkToString;
import com.google.zigva.io.Source;
import com.google.zigva.lang.ZigvaInterruptedException;
import com.google.zigva.lang.Zystem;
import com.google.zigva.sh.ShellCommand;

import java.util.Iterator;
import java.util.List;

public class CommandExecutor {

  public static final class Builder {
    
    private final Zystem zystem;
    private final ShellCommand.Builder shellCommandBuilder;
    private final ZigvaThreadFactory threadFactory;

    @Inject
    Builder(
        Zystem zystem, 
        ShellCommand.Builder shellCommandBuilder, 
        ZigvaThreadFactory threadFactory) {
      this.zystem = zystem;
      this.shellCommandBuilder = shellCommandBuilder;
      this.threadFactory = threadFactory;
    }
    
    public CommandExecutor create() {
      return new CommandExecutor(zystem, threadFactory, shellCommandBuilder);
    }
    
    public Builder with(Zystem zystem) {
      return new Builder(zystem, shellCommandBuilder, threadFactory);
    }
    
  }
  
  public interface Command {
    ZigvaTask execute(Zystem zystem);
  }
  
  public interface PreparedCommand {

    WaitableZivaTask execute();

    PreparedCommand pipe(Command command);

    PreparedCommand pipe(String... shellCommand);
  }

  private final ZigvaThreadFactory threadFactory;
  private final Zystem zystem;
  private final ShellCommand.Builder shellCommandBuilder;

  //TODO: make this an ImmutableSelfBuilder?
  @Inject
  public CommandExecutor(
      Zystem zystem,
      ZigvaThreadFactory threadFactory,
      ShellCommand.Builder shellCommandBuilder) {
    this.zystem = zystem;
    this.threadFactory = threadFactory;
    this.shellCommandBuilder = shellCommandBuilder;
  }
  
  public PreparedCommand command(String... shellCommand) {
    SimplePreparedCommand pc = new SimplePreparedCommand(
        threadFactory, shellCommandBuilder, 
        zystem, 
        shellCommandBuilder.build(shellCommand));
    return pc;
  }

  public PreparedCommand command(Command command) {
    SimplePreparedCommand pc = new SimplePreparedCommand(
        threadFactory,
        shellCommandBuilder, 
        zystem, 
        command);
    return pc;
  }

  static class SimplePreparedCommand implements PreparedCommand {

    private final ShellCommand.Builder shellCommandBuilder;
    private final Zystem zystem;
    private final List<Command> commands;
    private final ZigvaThreadFactory threadFactory;

    public SimplePreparedCommand(
        ZigvaThreadFactory threadFactory, ShellCommand.Builder shellCommandBuilder,
        Zystem zystem, 
        Command command) {
      Preconditions.checkNotNull(command);
      this.threadFactory = threadFactory;
      this.shellCommandBuilder = shellCommandBuilder;
      this.zystem = zystem;
      //TODO: make it immutable
      this.commands = Lists.newArrayList(command);
    }

    @Override
    public String toString() {
      return String.format("[%s:%s]", zystem.toString(), commands.toString());
    }
    
    @Override
    public WaitableZivaTask execute() {
      SinkToString errMonitor = new SinkToString();
      @SuppressWarnings("unchecked")
      Sink<Character> forkedErr = new ForkingSink<Character>(
          zystem.ioFactory().buildErr(), 
          errMonitor);
      Zystem localZystem = new ZystemSelfBuilder(zystem).withErr(forkedErr);
      
      Source<Character> nextIn = localZystem.ioFactory().buildIn();
      Sink<Character> nextOut;
      
      List<ZigvaTask> allTasksExecuted = Lists.newArrayList();
      
      Iterator<? extends Command> iterator = commands.iterator();
      while (iterator.hasNext()) {
        Command command = iterator.next();
        ZigvaPipe zivaPipe = new ZigvaPipe();
        if (iterator.hasNext()) {
          zivaPipe = new ZigvaPipe();
          nextOut = zivaPipe.in();
        } else {
          nextOut = localZystem.ioFactory().buildOut();
        }
        ZystemSelfBuilder tempZystem = 
          new ZystemSelfBuilder(localZystem)
            .withIn(nextIn)
            .withOut(nextOut);
        allTasksExecuted.add(new SyncZivaTask(command.execute(tempZystem)));
        if (iterator.hasNext()) {
          nextIn = zivaPipe.out();
        }
      }
      WaitableZivaTask result = new SyncZivaTask(new CompoundZivaTask(
          threadFactory, errMonitor, allTasksExecuted));
      
      threadFactory.newThread(result).start();
      return result;
    }

    @Override
    public PreparedCommand pipe(String... shellCommand) {
      commands.add(shellCommandBuilder.build(shellCommand));
      return this;
    }

    @Override
    public PreparedCommand pipe(Command command) {
      commands.add(command);
      return this;
    }
  }
  
  public static class ZigvaPipe {
   
    private final class MySink implements Sink<Character> {
      @Override
      public void write(Character data) throws DataSourceClosedException {
        buffer.enq(data);
      }

      @Override
      public boolean isReady() throws DataSourceClosedException {
        return true;
      }

      @Override
      public void close() {
        synchronized(lock) {
          reader.isEOS = true;
          lock.notifyAll();
        }
      }

      @Override
      public void flush() {
        buffer.blockUntilEmpty();
      }
    }

    private final class MySource implements Source<Character> {
      
      boolean isEOS = false;
      
      @Override
      public Character read() throws DataNotReadyException, DataSourceClosedException,
          EndOfDataException {
        return buffer.deq();
      }

      @Override
      public boolean isReady() throws DataSourceClosedException {
        return true;
      }

      @Override
      public boolean isEndOfStream() throws DataSourceClosedException, ZigvaInterruptedException {
        try {
          synchronized(lock) {
            while (!isEOS && buffer.size() == 0) {
              lock.wait();
            }
          }
        } catch (InterruptedException e) {
          throw new ZigvaInterruptedException(e);
        }
        return isEOS && buffer.size() == 0;
      }

      @Override
      public void close() {
      }
    }

    private final CircularBuffer<Character> buffer;
    private final String lock;

    public ZigvaPipe() {
      this.lock = "LOCK";
      this.buffer = new CircularBuffer<Character>(1000, lock);
    }
    
    private final MySource reader = new MySource();
    private final Sink<Character> sink = new MySink();

    public Sink<Character> in() {
      return sink;
    }
    
    public Source<Character> out() {
      return reader;
    }
  }
}
