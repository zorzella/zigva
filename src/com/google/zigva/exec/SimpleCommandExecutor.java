// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.exec;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.zigva.collections.CircularBuffer;
import com.google.zigva.exec.CommandExecutor.Builder;
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

public class SimpleCommandExecutor implements CommandExecutor {

  private final ZigvaThreadFactory threadFactory;
  private final Zystem zystem;
  private final ShellCommand.Builder shellCommandBuilder;
  private final CommandExecutor.Builder cmdExecutorBuilder;

//TODO: make this an ImmutableSelfBuilder?
    @Inject
  public SimpleCommandExecutor(
      Zystem zystem,
      ZigvaThreadFactory threadFactory,
      CommandExecutor.Builder cmdExecutorBuilder,
      ShellCommand.Builder shellCommandBuilder) {
    this.zystem = zystem;
    this.threadFactory = threadFactory;
    this.cmdExecutorBuilder = cmdExecutorBuilder;
    this.shellCommandBuilder = shellCommandBuilder;
  }
  
  @Override
  public PreparedCommand command(String... shellCommand) {
    SimplePreparedCommand pc = new SimplePreparedCommand(
        threadFactory, 
        cmdExecutorBuilder, 
        shellCommandBuilder, 
        zystem, 
        shellCommandBuilder.build(shellCommand));
    return pc;
  }

  @Override
  public PreparedCommand command(Command command) {
    SimplePreparedCommand pc = new SimplePreparedCommand(
        threadFactory,
        cmdExecutorBuilder, 
        shellCommandBuilder, 
        zystem, 
        command);
    return pc;
  }

  static class SimplePreparedCommand implements PreparedCommand {

    private final ZigvaThreadFactory threadFactory;
    private final CommandExecutor.Builder cmdExecutorBuilder;
    private final ShellCommand.Builder shellCommandBuilder;
    private final Zystem zystem;
    private final List<Command> commands;

    public SimplePreparedCommand(
        ZigvaThreadFactory threadFactory,
        CommandExecutor.Builder cmdExecutorBuilder,
        ShellCommand.Builder shellCommandBuilder,
        Zystem zystem, 
        Command command) {
      Preconditions.checkNotNull(command);
      this.threadFactory = threadFactory;
      this.cmdExecutorBuilder = cmdExecutorBuilder;
      this.shellCommandBuilder = shellCommandBuilder;
      this.zystem = zystem;
      //TODO: make it immutable?
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
        CommandExecutor.Builder temp = cmdExecutorBuilder.with(tempZystem);
        allTasksExecuted.add(new SyncZivaTask(command.buildTask(temp, tempZystem)));
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
  
  
 public static final class Builder implements CommandExecutor.Builder {
    
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
      return new SimpleCommandExecutor(zystem, threadFactory, this, shellCommandBuilder);
    }
    
    public Builder with(Zystem zystem) {
      return new Builder(zystem, shellCommandBuilder, threadFactory);
    }
    
  }

  
}
