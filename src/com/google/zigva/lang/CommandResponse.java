// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.lang;

import com.google.zigva.exec.ZigvaTask;
import com.google.zigva.exec.CommandExecutor.Command;
import com.google.zigva.io.Source;

public class CommandResponse implements ConvenienceWaitable {

  private static final ConvenienceWaitable NO_WAIT = new ConvenienceWaitable() {
    
    @Override
    public boolean waitFor(long timeoutInMillis) {
      return true;
    }
  
    @Override
    public void waitFor() {
    }
    
    @Override
    public String toString() {
      return "NO_WAIT";
    }
  };

  private static final Command NO_COMMAND = new Command() {
  
    @Override
    public CommandResponse go(Zystem zystem, Source<Character> in) {
      return null;
    }
  
    @Override
    public ZigvaTask buildTask(Zystem zystem) {
      return null;
    }
    
    public String toString() {
      return "NO_COMMAND";
    };
  };
  
  private final Source<Character> out;
  private final Source<Character> err;
  private final ConvenienceWaitable waitable;
  private final Command command;
  
  private CommandResponse(
      Command command,
      Source<Character> out, 
      Source<Character> err, 
      ConvenienceWaitable waitable) {
    this.command = command;
    this.out = out;
    this.err = err;
    this.waitable = waitable;
  }
  
  public static CommandResponse forOut(Source<Character> out) {
    return new CommandResponse(NO_COMMAND, out, null, NO_WAIT);
  }
  
  public static CommandResponse forErr(Source<Character> err) {
    return new CommandResponse(NO_COMMAND, null, err, NO_WAIT);
  }
  
  public static CommandResponse forOutErr(Source<Character> out, Source<Character> err) {
    return new CommandResponse(NO_COMMAND, out, err, NO_WAIT);
  }

  public static CommandResponse forOut(Source<Character> out, Waitable waitable) {
    return new CommandResponse(NO_COMMAND, out, null, Waitables.from(waitable));
  }
  
  public static CommandResponse forErr(Source<Character> err, Waitable waitable) {
    return new CommandResponse(NO_COMMAND, null, err, Waitables.from(waitable));
  }

  public static CommandResponse forOutErr(
      Command c,
      Source<Character> out, 
      Source<Character> err, 
      Waitable waitable) {
    return new CommandResponse(c, out, err, Waitables.from(waitable));
  }

  public Source<Character> out() {
    return out;
  }
  
  public Source<Character> err() {
    return err;
  }

  @Override
  public void waitFor() {
    waitFor(0);
  }

  @Override
  public boolean waitFor(long timeoutInMillis) {
    return waitable.waitFor(timeoutInMillis);
  }

  @Override
  public String toString() {
    return String.format(
        "Command: [%s], out: [%s], err: [%s], waitable: [%s]", 
        command, out, err, waitable);
  }
}
