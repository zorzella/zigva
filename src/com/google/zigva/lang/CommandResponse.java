// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.lang;

import com.google.zigva.io.Source;

public class CommandResponse implements ConvenienceWaitable {

  private final Source<Character> out;
  private final Source<Character> err;
  private final ConvenienceWaitable waitable;
  
  private CommandResponse(
      Source<Character> out, 
      Source<Character> err, 
      ConvenienceWaitable waitable) {
    this.out = out;
    this.err = err;
    this.waitable = waitable;
  }

  private static final ConvenienceWaitable NO_WAIT = new ConvenienceWaitable() {
  
    @Override
    public boolean waitFor(long timeoutInMillis) {
      return true;
    }
  
    @Override
    public void waitFor() {
    }
  };
  
  public static CommandResponse forOut(Source<Character> out) {
    return new CommandResponse(out, null, NO_WAIT);
  }
  
  public static CommandResponse forErr(Source<Character> err) {
    return new CommandResponse(null, err, NO_WAIT);
  }
  
  public static CommandResponse forOutErr(Source<Character> out, Source<Character> err) {
    return new CommandResponse(out, err, NO_WAIT);
  }

  public static CommandResponse forOut(Source<Character> out, Waitable waitable) {
    return new CommandResponse(out, null, Waitables.from(waitable));
  }
  
  public static CommandResponse forErr(Source<Character> err, Waitable waitable) {
    return new CommandResponse(null, err, Waitables.from(waitable));
  }

  public static CommandResponse forOutErr(
      Source<Character> out, 
      Source<Character> err, 
      Waitable waitable) {
    return new CommandResponse(out, err, Waitables.from(waitable));
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
  
}
