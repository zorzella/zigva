// Copyright 2008 Google Inc.  All Rights Reserved.
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
import com.google.zigva.io.ForkingSinkFactory;
import com.google.zigva.io.PassiveSink;
import com.google.zigva.io.PassiveSinkToString;
import com.google.zigva.io.SimpleSink;
import com.google.zigva.io.Sink;
import com.google.zigva.io.Source;
import com.google.zigva.lang.CommandResponse;
import com.google.zigva.lang.ConvenienceWaitable;
import com.google.zigva.lang.Runnables;
import com.google.zigva.lang.SinkFactory;
import com.google.zigva.lang.Waitables;
import com.google.zigva.lang.ZRunnable;
import com.google.zigva.lang.ZThread;
import com.google.zigva.lang.ZigvaInterruptedException;
import com.google.zigva.lang.Zystem;

import java.util.Iterator;
import java.util.List;

public class SimpleCommandExecutor implements CommandExecutor {

  private final ZigvaThreadFactory threadFactory;
  private final Zystem zystem;
  private final CommandExecutor.Builder cmdExecutorBuilder;
  private final ThreadRunner threadRunner;

//TODO: make this an ImmutableSelfBuilder?
    @Inject
  public SimpleCommandExecutor(
      Zystem zystem,
      ZigvaThreadFactory threadFactory,
      CommandExecutor.Builder cmdExecutorBuilder, 
      ThreadRunner threadRunner) {
    this.zystem = zystem;
    this.threadFactory = threadFactory;
    this.cmdExecutorBuilder = cmdExecutorBuilder;
    this.threadRunner = threadRunner;
  }
  
  @Override
  public PreparedCommand command(Command command) {
    SimplePreparedCommand pc = new SimplePreparedCommand(
        threadFactory,
        cmdExecutorBuilder, 
        zystem, 
        command, 
        threadRunner);
    return pc;
  }

  static class SimplePreparedCommand implements PreparedCommand {

    private final ZigvaThreadFactory threadFactory;
    private final CommandExecutor.Builder cmdExecutorBuilder;
    private final Zystem zystem;
    private final List<Command> commands;
    private final ThreadRunner threadRunner;

    public SimplePreparedCommand(
        ZigvaThreadFactory threadFactory,
        CommandExecutor.Builder cmdExecutorBuilder,
        Zystem zystem, 
        Command command, ThreadRunner threadRunner) {
      Preconditions.checkNotNull(command);
      this.threadFactory = threadFactory;
      this.cmdExecutorBuilder = cmdExecutorBuilder;
      this.zystem = zystem;
      //TODO: make it immutable?
      this.commands = Lists.newArrayList(command);
      this.threadRunner = threadRunner;
    }

    @Override
    public String toString() {
      return String.format("[%s:%s]", zystem.toString(), commands.toString());
    }
    
    @Override
    public WaitableZivaTask execute() {
      if (true) {
        return execute_old();
      } else {
        return execute_new();
      }
    }
      
    public WaitableZivaTask execute_new() {
      Iterator<Command> iterator = commands.iterator();
      Source<Character> in = zystem.ioFactory().in().build();
      
      final List<ConvenienceWaitable> waitableList = Lists.newArrayList();
      
      while (iterator.hasNext()) {
        Command command = iterator.next();
        CommandResponse temp = command.go(zystem, in);
        waitableList.add(temp);
        
        // TODO: swap etc
        in = temp.out();
        if (temp.err() != null) {
          
          
          
          PassiveSinkToString errMonitor = new PassiveSinkToString();
          @SuppressWarnings("unchecked")
          SinkFactory<Character> forkedErrFactory =
            new ForkingSinkFactory<Character>(
                threadFactory, 
                zystem.ioFactory().err(), 
                errMonitor.asSinkFactory());

          
          
          // TODO: return this as well!
          ZRunnable bar = Runnables.fromRunnable(
              forkedErrFactory.build(temp.err()));
          ZThread foo = threadFactory.newDaemonThread(
              bar).ztart();
        }
      }
      final ZRunnable runnableCommand = Runnables.fromRunnable(
          zystem.ioFactory().out().build(in));
      waitableList.add(runnableCommand);
      final ConvenienceWaitable toWait = Waitables.from(waitableList);
      threadRunner.schedule(runnableCommand);
//      threadFactory.newDaemonThread(runnableCommand).ztart();
     
      return new WaitableZivaTask() {
      
        @Override
        public boolean waitFor(long timeout) {
          return toWait.waitFor(timeout);
        }
      
        @Override
        public void waitFor() {
          waitFor(0);
        }
      
        @Override
        public String getName() {
          return "NAME";
        }
      
        @Override
        public void run() throws RuntimeException {
          throw new UnsupportedOperationException();
        }
      
        @Override
        public void kill() {
          throw new UnsupportedOperationException();
        }
      };
    }
    
    public WaitableZivaTask execute_old() {
      PassiveSinkToString errMonitor = new PassiveSinkToString();
      @SuppressWarnings("unchecked")
      SinkFactory<Character> forkedErrFactory =
        new ForkingSinkFactory<Character>(
            threadFactory, 
            zystem.ioFactory().err(), 
            errMonitor.asSinkFactory());
      Zystem localZystem = new ZystemSelfBuilder(zystem).withErr(forkedErrFactory);
      
      Source<Character> nextIn = localZystem.ioFactory().in().build();
      SinkFactory<Character> nextOut;
      
      List<ZigvaTask> allTasksToBeExecuted = Lists.newArrayList();
      
      Iterator<? extends Command> iterator = commands.iterator();
      while (iterator.hasNext()) {
        Command command = iterator.next();
        ZigvaPipe zivaPipe = new ZigvaPipe();
        if (iterator.hasNext()) {
          zivaPipe = new ZigvaPipe();
          nextOut = zivaPipe.in();
        } else {
          nextOut = localZystem.ioFactory().out();
        }
        ZystemSelfBuilder tempZystem = 
          new ZystemSelfBuilder(localZystem)
            .withIn(nextIn)
            .withOut(nextOut);
        CommandExecutor.Builder temp = cmdExecutorBuilder.with(tempZystem);
        allTasksToBeExecuted.add(new SyncZivaTask(command.buildTask(tempZystem)));
        if (iterator.hasNext()) {
          nextIn = zivaPipe.out();
        }
      }
      WaitableZivaTask result = new SyncZivaTask(new CompoundZivaTask(
          threadFactory, errMonitor, allTasksToBeExecuted));
      
      threadFactory.newDaemonThread(result).start();
      return result;
    }

    @Override
    public PreparedCommand pipe(Command command) {
      commands.add(command);
      return this;
    }
  }
  
  public static class ZigvaPipe {
   
    private final class MyPassiveSink implements PassiveSink<Character> {
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
      private boolean isClosed;
      
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
        isClosed = true;
      }

      @Override
      public boolean isClosed() {
        return isClosed;
      }
    }

    private final CircularBuffer<Character> buffer;
    private final String lock;

    public ZigvaPipe() {
      this.lock = "LOCK";
      this.buffer = new CircularBuffer<Character>(1000, lock);
    }
    
    private final MySource reader = new MySource();
    private final PassiveSink<Character> sink = new MyPassiveSink();

    public SinkFactory<Character> in() {
      return new SinkFactory<Character>(){
      
        @Override
        public Sink build(Source<Character> source) {
          return new SimpleSink<Character>(source, sink);
        }
      };
    }
    
    public Source<Character> out() {
      return reader;
    }
  }
  
  
 public static final class Builder implements CommandExecutor.Builder {
    
    private final Zystem zystem;
    private final ZigvaThreadFactory threadFactory;
    private final ThreadRunner threadRunner;

    @Inject
    Builder(
        Zystem zystem, 
        ZigvaThreadFactory threadFactory, 
        ThreadRunner threadRunner) {
      this.zystem = zystem;
      this.threadFactory = threadFactory;
      this.threadRunner = threadRunner;
    }
    
    public CommandExecutor create() {
      return new SimpleCommandExecutor(zystem, threadFactory, this, threadRunner);
    }
    
    public Builder with(Zystem zystem) {
      return new Builder(zystem, threadFactory, threadRunner);
    }
    
  }

  
}
