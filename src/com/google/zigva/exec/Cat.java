// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.exec;

import com.google.zigva.exec.CommandExecutor.Command;
import com.google.zigva.io.Sink;
import com.google.zigva.io.Source;
import com.google.zigva.lang.IoFactory;
import com.google.zigva.lang.NamedRunnable;
import com.google.zigva.lang.Zystem;

public class Cat implements Command {

  private final class MyZivaTask implements ZivaTask, NamedRunnable {
    
    private KillableCollector toKill = new KillableCollector();
    private final IoFactory ioFactory;

    private MyZivaTask(Zystem zystem) {
      this.ioFactory = zystem.ioFactory();
    }
    
    @Override
    public void kill() {
      toKill.killable().kill();
    }

    @Override
    public void run() {
      Source<Character> in = toKill.add(ioFactory.buildIn());
      Sink<Character> out = toKill.add(ioFactory.buildOut());
      while (!in.isEndOfStream()) {
        out.write(in.read());
      }
      out.flush();
      out.close();
      in.close();
    }

    @Override
    public String getName() {
      return "Cat";
    }
  }

  @Override
  public ZivaTask execute(final Zystem zystem) {
    ZivaTask result = new SyncZivaTask(new MyZivaTask(zystem));
    return result;
  }
}
