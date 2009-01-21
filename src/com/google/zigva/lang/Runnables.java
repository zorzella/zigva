// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.lang;

public class Runnables {

  public static ZRunnable fromRunnable(final Runnable delegate) {

    return new ZRunnable() {

      private boolean isDone;
      private Object lock = new StringBuffer("LOCK");
      private RuntimeException exception;

//      @Override
//      public boolean isFinished() {
//        return isDone;
//      }

      @Override
      public void waitFor() {
        waitFor(0);
      }

      @Override
      public boolean waitFor(long timeoutInMillis) {
        if (timeoutInMillis < 0) { 
          throw new IllegalArgumentException("Timeout must be non-negative.");
        }
        long now = System.currentTimeMillis();
        synchronized(lock) {
          while (!isDone) {
            try {
              lock.wait(timeoutInMillis);
              if (now + timeoutInMillis > System.currentTimeMillis() && !isDone) {
                return false;
              }
            } catch (InterruptedException e) {
              throw new ZigvaInterruptedException(e);
            }
          }
          if (exception != null) {
            throw exception;
          }
        }
        return true;
      }

      @Override
      public void run() {
        try {
          delegate.run();
        } catch (RuntimeException e) {
          exception = e;
          throw e;
        } finally {
          synchronized (lock) {
            isDone = true;
            lock.notifyAll();
          }
        }
      }
    };
  }
}