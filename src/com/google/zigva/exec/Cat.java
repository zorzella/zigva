// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.exec;

import com.google.inject.Inject;
import com.google.zigva.exec.CommandExecutor.Command;
import com.google.zigva.io.Sink;
import com.google.zigva.io.Source;
import com.google.zigva.lang.NamedRunnable;
import com.google.zigva.lang.Zystem;

import java.util.List;
import java.util.concurrent.ThreadFactory;

public class Cat implements Command {

  public static final class Builder {
    
    private final ThreadFactory threadFactory;

    @Inject
    private Builder(ThreadFactory threadFactory) {
      this.threadFactory = threadFactory;
    }
    
    public Cat create() {
      return new Cat(threadFactory);
    }
    
  }

  private final ThreadFactory threadFactory;
  
  private Cat(ThreadFactory threadFactory) {
    this.threadFactory = threadFactory;
  }
  
  
  private final class MyZivaTask implements ZivaTask, NamedRunnable {
    
    private final Zystem zystem;
    private Boolean done = false;
    private boolean killed = false;

    public MyZivaTask(Zystem zystem) {
      this.zystem = zystem;
    }
    
    @Override
    public void waitFor() {
      while(!done) {
        try {
          synchronized(this) {
            wait();
          }
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }

    @Override
    public void kill() {
      killed = true;
    }

    @Override
    public void run() {
      Source<Character> in = zystem.ioFactory().buildIn();
      Sink<Character> out = zystem.ioFactory().buildOut();
      while (!in.isEndOfStream() && !killed) {
        out.write(in.read());
      }
      out.close();
      in.close();
      done = true;
      synchronized(this) {
        notifyAll();
      }
    }

    @Override
    public String getName() {
      return "Cat";
    }
  }

  @Override
  public ZivaTask execute(final Zystem zystem) {
    MyZivaTask result = new MyZivaTask(zystem);
    threadFactory.newThread(result).start();
    return result;
  }
}
