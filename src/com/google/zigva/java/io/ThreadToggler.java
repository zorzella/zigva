// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.java.io;

import com.google.zigva.lang.ZigvaInterruptedException;

public class ThreadToggler {

  private Object lock;

  public ThreadToggler(Object lock) {
    if (lock == null) {
      throw new NullPointerException();
    }
    this.lock = lock;
  }

  
  public void toggle(Object lock) {
    if (lock == null) {
      throw new NullPointerException();
    }
    this.lock.notify();
    this.lock = lock;
    try {
      this.lock.wait();
    } catch (InterruptedException e) {
      throw new ZigvaInterruptedException(e);
    }
  }

}
