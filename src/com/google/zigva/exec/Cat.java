// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.exec;

import com.google.zigva.exec.CommandExecutor.Command;
import com.google.zigva.io.Sink;
import com.google.zigva.io.Source;
import com.google.zigva.lang.NamedRunnable;
import com.google.zigva.lang.Zystem;

public class Cat implements Command {

  private final class MyZivaTask implements ZivaTask, NamedRunnable {
    
    private final Zystem zystem;
    private Killable killable;

    public MyZivaTask(Zystem zystem) {
      this.zystem = zystem;
    }
    
    @Override
    public void waitFor() {
    }

    @Override
    public void kill() {
      killable.kill();
    }

    @Override
    public void run() {
      Source<Character> in = zystem.ioFactory().buildIn();
      Sink<Character> out = zystem.ioFactory().buildOut();
      this.killable = Killables.of(Killables.of(in), Killables.of(out));
      while (!in.isEndOfStream()) {
        out.write(in.read());
      }
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
