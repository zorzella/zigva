package com.google.zigva.io;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.zigva.guice.ZystemSelfBuilder;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class Executor {

  public interface Command {
    ZivaTask execute(Zystem zystem);
  }
  
  public interface PreparedCommand {

    ZivaTask execute();

    PreparedCommand pipe(String... shellCommand);
  }

  private final Zystem zystem;

  @Inject
  public Executor(Zystem zystem) {
    this.zystem = zystem;
  }
  
  public PreparedCommand command(String... shellCommand) {
    SimplePreparedCommand pc = new SimplePreparedCommand(
        zystem, 
        new ShellCommand(shellCommand));
    return pc;
  }

  public PreparedCommand command(Command command) {
    SimplePreparedCommand pc = new SimplePreparedCommand(
        zystem, 
        command);
    return pc;
  }

  static class SimplePreparedCommand implements PreparedCommand {

    private final Zystem zystem;
    private final List<Command> commands;

    public SimplePreparedCommand(Zystem zystem, Command command) {
      Preconditions.checkNotNull(command);
      this.zystem = zystem;
      //TODO: make it immutable
      this.commands = Lists.newArrayList(command);
    }

    @Override
    public ZivaTask execute() {
      Source<Character> nextIn = zystem.in().get();
      Appendable nextOut;// = zystem.out();
      
      List<ZivaTask> allTasksExecuted = Lists.newArrayList();
      
      Iterator<? extends Command> iterator = commands.iterator();
      while (iterator.hasNext()) {
        Command command = iterator.next();
        ZivaPipe zivaPipe = null;
        if (iterator.hasNext()) {
          zivaPipe = new ZivaPipe();
          nextOut = zivaPipe.in();
        } else {
          nextOut = zystem.out();
        }
        // The last command needs to be special-cased, since it won't have
        // a pipe in front of it
//        if (!iterator.hasNext()) {
          ZystemSelfBuilder tempZystem = 
            new ZystemSelfBuilder(zystem)
              .withIn(nextIn)
              .withOut(nextOut);
          allTasksExecuted.add(command.execute(tempZystem));
//          break;
//        }
        if (iterator.hasNext()) {
          nextIn = zivaPipe.out();
        }
      }
      return new CompoundZivaTask(allTasksExecuted);
    }

    @Override
    public PreparedCommand pipe(String... shellCommand) {
      commands.add(new ShellCommand(shellCommand));
      return this;
    }
  }
  
  public static class ZivaPipe {
   
    private final ArrayBlockingQueue<Character> buffer;

    public ZivaPipe() {
      this.buffer = new ArrayBlockingQueue<Character>(10);
    }
    
    private final Appendable appendable = new AppendableFromLite(new AppendableLite() {
    
      @Override
      public AppendableLite append(char c) {
        try {
          buffer.put(c);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        return this;
      }
    });
    
    private final Source<Character> reader = null;
//      Readers.fromQueue(new ArrayBlockingQueue<Character>(5));

    public Appendable in() {
      return appendable;
    }
    
    public Source<Character> out() {
      return reader;
    }
  }
}













