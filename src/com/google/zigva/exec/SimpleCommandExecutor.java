// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.exec;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.zigva.collections.CircularBuffer;
import com.google.zigva.guice.ZigvaThreadFactory;
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
import com.google.zigva.lang.ZigvaInterruptedException;
import com.google.zigva.lang.Zystem;
import com.google.zigva.lang.Waitables.Pair;

import java.util.Iterator;
import java.util.List;

public class SimpleCommandExecutor implements CommandExecutor {

  private final Zystem zystem;
  private final ThreadRunner threadRunner;

  @Inject
  public SimpleCommandExecutor(
      Zystem zystem,
      ThreadRunner threadRunner) {
    this.zystem = zystem;
    this.threadRunner = threadRunner;
  }
  
  @Override
  public PreparedCommand command(Command command) {
    SimplePreparedCommand pc = new SimplePreparedCommand(
        zystem, 
        command, 
        threadRunner);
    return pc;
  }

  static class SimplePreparedCommand implements PreparedCommand {

    private final Zystem zystem;
    private final List<Command> commands;
    private final ThreadRunner threadRunner;

    public SimplePreparedCommand(
        Zystem zystem, 
        Command command, 
        ThreadRunner threadRunner) {
      Preconditions.checkNotNull(command);
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
      Iterator<Command> iterator = commands.iterator();
      Source<Character> nextIn = zystem.ioFactory().in().build();
      
      final List<Waitables.Pair> waitableList = Lists.newArrayList();
      

      while (iterator.hasNext()) {


        Command command = iterator.next();
        CommandResponse commandResponse = command.go(zystem, nextIn);
        
        // TODO: swap etc
        nextIn = commandResponse.out();
        
        Waitables.ExceptionModifier exceptionModifier = 
          Waitables.IDENTITY_EXCEPTION_MODIFIER;
        
        if (commandResponse.err() != null) {
          
          
          
          
          final PassiveSinkToString errMonitor = new PassiveSinkToString();
          @SuppressWarnings("unchecked")
          SinkFactory<Character> forkedErrFactory =
            new ForkingSinkFactory<Character>(
                threadRunner, 
                zystem.ioFactory().err(), 
                errMonitor.asSinkFactory());

          
          
          
          final ZRunnable errDrainer = threadRunner.schedule(
              forkedErrFactory.build(commandResponse.err()));
          waitableList.add(new Waitables.Pair(errDrainer, Waitables.IDENTITY_EXCEPTION_MODIFIER));
          
          exceptionModifier = new Waitables.ExceptionModifier() {
          
            @Override
            public RuntimeException modify(RuntimeException exception) {
              
              errDrainer.waitFor();
              return new RuntimeException(String.format(
                  "stderr of command was:\n" +
                  "******************************************\n" +
                  "%s \n" +
                  "******************************************", errMonitor.asString()), 
                  exception);
            }
          };
        }

        Pair temp = new Waitables.Pair(commandResponse, exceptionModifier);
        
        waitableList.add(temp);
      }
      final ZRunnable runnableCommand = Runnables.fromRunnable(
          zystem.ioFactory().out().build(nextIn));
      waitableList.add(new Waitables.Pair(runnableCommand, Waitables.IDENTITY_EXCEPTION_MODIFIER));
      final ConvenienceWaitable toWait = Waitables.from(waitableList);
      threadRunner.schedule(runnableCommand);
     
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
      return new SimpleCommandExecutor(zystem, threadRunner);
    }
    
    public Builder with(Zystem zystem) {
      return new Builder(zystem, threadFactory, threadRunner);
    }
  }
}
