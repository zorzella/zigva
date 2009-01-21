// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.lang;

import com.google.inject.Inject;
import com.google.zigva.guice.ZigvaThreadFactory;


public class Waitables {

  private final ZigvaThreadFactory zigvaThreadFactory;

  @Inject
  public Waitables(ZigvaThreadFactory zigvaThreadFactory) {
    this.zigvaThreadFactory = zigvaThreadFactory;
  }
  
  public static ConvenienceWaitable from(final Waitable waitable) {
    if (waitable instanceof ConvenienceWaitable) {
      return (ConvenienceWaitable) waitable;
    }
    return new ConvenienceWaitable() {
    
      @Override
      public boolean waitFor(long timeoutInMillis) {
        return waitable.waitFor(timeoutInMillis);
      }
    
      @Override
      public void waitFor() {
        waitable.waitFor(0);
      }
    };
  }

  public ConvenienceWaitable from(final NaiveWaitable waitable) {
    if (waitable instanceof ConvenienceWaitable) {
      return (ConvenienceWaitable) waitable;
    }
    
    final Object lock = new StringBuilder("ConvenienceWaitable lock");

    final MyRunnable myRunnable = new MyRunnable(waitable, lock);
    final ZThread thread = zigvaThreadFactory.newDaemonThread(myRunnable);
    
    return new ConvenienceWaitable() {
    
      @Override
      public boolean waitFor(long timeoutInMillis) {
        synchronized(lock) {
          if (myRunnable.isDone) {
            return true;
          }
          try {
            lock.wait(timeoutInMillis);
          } catch (InterruptedException e) {
            throw new ZigvaInterruptedException(e);
          }
          if (myRunnable.isDone) {
            return true;
          }
          return false;
        }
      }
    
      @Override
      public void waitFor() {
        waitFor(0);
      }
    };
  }

  private final class MyRunnable implements Runnable {
    private final NaiveWaitable waitable;
    private final Object lock;
    boolean isDone;

    private MyRunnable(NaiveWaitable waitable, Object lock) {
      this.waitable = waitable;
      this.lock = lock;
    }

    @Override
    public void run() {
      waitable.waitFor();
      synchronized(lock) {
        isDone = true;
        lock.notifyAll();
      }
    }
  }
  
}
