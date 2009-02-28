// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.exec.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.zigva.collections.CircularBuffer;
import com.google.zigva.exec.CommandExecutor;
import com.google.zigva.exec.ThreadRunner;
import com.google.zigva.io.DataNotReadyException;
import com.google.zigva.io.DataSourceClosedException;
import com.google.zigva.io.EndOfDataException;
import com.google.zigva.io.ForkingSinkFactory;
import com.google.zigva.io.Sink;
import com.google.zigva.io.SinkToString;
import com.google.zigva.io.PumpToSink;
import com.google.zigva.io.Pump;
import com.google.zigva.io.Source;
import com.google.zigva.lang.CommandResponse;
import com.google.zigva.lang.ConvenienceWaitable;
import com.google.zigva.lang.Runnables;
import com.google.zigva.lang.PumpFactory;
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

  @Override
  public CommandExecutor with(Zystem zystem) {
    return new SimpleCommandExecutor(zystem, threadRunner);
  }

  static class SimplePreparedCommand implements PreparedCommand {

    private final Zystem zystem;
    private final ImmutableList<Command> commands;
    private final ThreadRunner threadRunner;

    private SimplePreparedCommand(
        Zystem zystem, 
        Command command, 
        ThreadRunner threadRunner) {
      Preconditions.checkNotNull(command);
      this.zystem = zystem;
      this.commands = ImmutableList.of(command);
      this.threadRunner = threadRunner;
    }

    private SimplePreparedCommand(
        Zystem zystem, 
        ImmutableList<Command> commands, 
        ThreadRunner threadRunner) {
      Preconditions.checkNotNull(commands);
      this.zystem = zystem;
      this.commands = commands;
      this.threadRunner = threadRunner;
    }
    
    @Override
    public String toString() {
      return String.format("[%s:%s]", zystem.toString(), commands.toString());
    }
    
    @Override
    public ConvenienceWaitable execute() {
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
          
          
          
          
          final SinkToString errMonitor = new SinkToString();
          @SuppressWarnings("unchecked")
          PumpFactory<Character> forkedErrFactory =
            new ForkingSinkFactory<Character>(
                threadRunner, 
                zystem.ioFactory().err(), 
                errMonitor.asPumpFactory());

          
          
          
          final ZRunnable errDrainer = threadRunner.schedule(
              forkedErrFactory.getPumpFor(commandResponse.err()));
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
          zystem.ioFactory().out().getPumpFor(nextIn));
      waitableList.add(new Waitables.Pair(runnableCommand, Waitables.IDENTITY_EXCEPTION_MODIFIER));
      final ConvenienceWaitable toWait = Waitables.from(waitableList);
      threadRunner.schedule(runnableCommand);
     
      return new ConvenienceWaitable() {
      
        @Override
        public boolean waitFor(long timeout) {
          return toWait.waitFor(timeout);
        }
      
        @Override
        public void waitFor() {
          waitFor(0);
        }
      };
    }
    
    @Override
    public PreparedCommand pipe(Command command) {
      ImmutableList<Command> newCommandList = 
        ImmutableList.copyOf(Iterables.concat(commands, Lists.newArrayList(command)));
      SimplePreparedCommand result = 
        new SimplePreparedCommand(zystem, newCommandList, threadRunner);
      return result;
    }
  }
  
  public static class ZigvaPipe {
   
    private final class MyPassiveSink implements Sink<Character> {
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
    private final Sink<Character> sink = new MyPassiveSink();

    public PumpFactory<Character> in() {
      return new PumpFactory<Character>(){
      
        @Override
        public Pump getPumpFor(Source<Character> source) {
          return new PumpToSink<Character>(source, sink);
        }
      };
    }
    
    public Source<Character> out() {
      return reader;
    }
  }
}
