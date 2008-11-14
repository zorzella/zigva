// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.lang;

public class ZigvaInterruptedException extends RuntimeException {

  public ZigvaInterruptedException(InterruptedException e) {
    super(e);
  }

  public ZigvaInterruptedException() {
    super();
  }

}
