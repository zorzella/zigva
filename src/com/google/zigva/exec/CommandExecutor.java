package com.google.zigva.exec;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.zigva.collections.CircularBuffer;
import com.google.zigva.guice.ZystemSelfBuilder;
import com.google.zigva.io.DataNotReadyException;
import com.google.zigva.io.DataSourceClosedException;
import com.google.zigva.io.EndOfDataException;
import com.google.zigva.io.Sink;
import com.google.zigva.io.Source;
import com.google.zigva.java.io.ReaderSource;
import com.google.zigva.lang.Zystem;
import com.google.zigva.sh.ActivePipe;
import com.google.zigva.sh.ShellCommand;

import java.util.Iterator;
import java.util.List;

public class CommandExecutor {

  public static final class Builder {
    
    private final Zystem zystem;
    private final ShellCommand.Builder shellCommandBuilder;

    @Inject
    Builder(Zystem zystem, ShellCommand.Builder shellCommandBuilder) {
      this.zystem = zystem;
      this.shellCommandBuilder = shellCommandBuilder;
    }
    
    public CommandExecutor create() {
      return new CommandExecutor(zystem, shellCommandBuilder);
    }
    
    public Builder with(Zystem zystem) {
      return new Builder(zystem, shellCommandBuilder);
    }
    
  }
  
  public interface Command {
    ZivaTask execute(Zystem zystem);
  }
  
  public interface PreparedCommand {

    WaitableZivaTask execute();

    PreparedCommand pipe(Command command);

    PreparedCommand pipe(String... shellCommand);
  }

  private final Zystem zystem;
  private final ShellCommand.Builder shellCommandBuilder;

  @Inject
  public CommandExecutor(Zystem zystem, ShellCommand.Builder shellCommandBuilder) {
    this.zystem = zystem;
    this.shellCommandBuilder = shellCommandBuilder;
  }
  
  public PreparedCommand command(String... shellCommand) {
    SimplePreparedCommand pc = new SimplePreparedCommand(
        shellCommandBuilder, 
        zystem, 
        shellCommandBuilder.build(shellCommand));
    return pc;
  }

  public PreparedCommand command(Command command) {
    SimplePreparedCommand pc = new SimplePreparedCommand(
        shellCommandBuilder, 
        zystem, 
        command);
    return pc;
  }

  static class SimplePreparedCommand implements PreparedCommand {

    private final ShellCommand.Builder shellCommandBuilder;
    private final Zystem zystem;
    private final List<Command> commands;

    public SimplePreparedCommand(
        ShellCommand.Builder shellCommandBuilder,
        Zystem zystem, 
        Command command) {
      Preconditions.checkNotNull(command);
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
      Source<Character> nextIn = zystem.ioFactory().buildIn();
      Sink<Character> nextOut;
      
      List<ZivaTask> allTasksExecuted = Lists.newArrayList();
      
      Iterator<? extends Command> iterator = commands.iterator();
      while (iterator.hasNext()) {
        Command command = iterator.next();
        ZivaPipe zivaPipe = new ZivaPipe();
        if (iterator.hasNext()) {
          zivaPipe = new ZivaPipe();
          nextOut = zivaPipe.in();
        } else {
          nextOut = zystem.ioFactory().buildOut();
        }
        ZystemSelfBuilder tempZystem = 
          new ZystemSelfBuilder(zystem)
            .withIn(nextIn)
            .withOut(nextOut);
        allTasksExecuted.add(new SyncZivaTask(command.execute(tempZystem)));
        if (iterator.hasNext()) {
          nextIn = zivaPipe.out();
        }
      }
      WaitableZivaTask result = new SyncZivaTask(new CompoundZivaTask(
          zystem.getThreadFactory(), allTasksExecuted));
      
      zystem.getThreadFactory().newThread(result).start();
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
  
  public static class ZivaPipe {
   
    private final class MySink implements Sink<Character> {
      @Override
      public void write(Character data) throws DataSourceClosedException {
        try {
          buffer.enq(data);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public boolean isReady() throws DataSourceClosedException {
        return true;
      }

      @Override
      public void close() {
        reader.isEOS = true;
        synchronized(lock) {
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
        try {
          return buffer.deq();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public boolean isReady() throws DataSourceClosedException {
        return true;
      }

      @Override
      public boolean isEndOfStream() throws DataSourceClosedException {
        while (!isEOS && buffer.size() == 0) {
          try {
            synchronized(lock) {
              lock.wait();
            }
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        }
        return isEOS && buffer.size() == 0;
      }

      @Override
      public void close() {
      }
    }

    private final CircularBuffer<Character> buffer;
    private final String lock;

    public ZivaPipe() {
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













