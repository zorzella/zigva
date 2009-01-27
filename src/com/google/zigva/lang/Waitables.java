// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.lang;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.zigva.guice.ZigvaThreadFactory;

import java.util.Set;


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
      
      @Override
      public String toString() {
        return waitable.toString();
      }
    };
  }
  
  // TODO MULTIPLEX EXCEPTIONS!
  public static ConvenienceWaitable from(final Iterable<ConvenienceWaitable> waitables) {
    
    ZigvaThreadFactory ztf = new ZigvaThreadFactory();
    
    final Set<ZRunnable> runnables = Sets.newHashSet();
    final Set<ConvenienceWaitable> finished = Sets.newHashSet();
    
    for (final ConvenienceWaitable waitable : waitables) {
      runnables.add(
          Runnables.fromRunnable(new Runnable() {

            @Override
            public void run() {
              try {
                waitable.waitFor();
              } finally {
                synchronized(finished) {
                  finished.add(waitable);
                  finished.notify();
                }
              }
            }

            @Override
            public String toString() {
              return String.format(
                  "runnable from waitable: [%s]", waitable.toString());
            }
          }));
    }
    for (ZRunnable r : runnables) {
      ztf.newDaemonThread(r).ztart();
    }
    
    return new ConvenienceWaitable() {
    
      @Override
      public boolean waitFor(long timeoutInMillis) {
        // "0" is a special case
        if (timeoutInMillis == 0) {
          waitFor();
          return true;
        }
        if (timeoutInMillis < 0) {
          throw new IllegalArgumentException();
        }
        synchronized(finished) {
          if (finished.size() != runnables.size()) {
            try {
              finished.wait(timeoutInMillis);
            } catch (InterruptedException e) {
              throw new ZigvaInterruptedException(e);
            }
          }
          return finished.size() == runnables.size();
        }
        
        
//        //TODO
//        long now = System.currentTimeMillis();
//        long then = now + timeoutInMillis;
//        boolean result = true;
//        for (Waitable waitable : waitables) {
//          long movingTimeout = then - System.currentTimeMillis();
//          if (movingTimeout < 1) {
//            return result;
//          }
//          if (!waitable.waitFor(movingTimeout)) {
//            result = false;
//          }
//        }
//        return result;
      }
    
      @Override
      public void waitFor() {
        synchronized(finished) {
          while (finished.size() != runnables.size()) {
            try {
              finished.wait();
            } catch (InterruptedException e) {
              throw new ZigvaInterruptedException(e);
            }
          }
        }
        //        for (ConvenienceWaitable waitable: waitables) {
        //          waitable.waitFor();
        //        }
      }
    };
  }
  
  public ConvenienceWaitable from(final NaiveWaitable waitable) {
    if (waitable instanceof ConvenienceWaitable) {
      return (ConvenienceWaitable) waitable;
    }
    
    final ZRunnable myRunnable = Runnables.fromRunnable(new Runnable() {
      @Override
      public void run() {
        waitable.waitFor();
      }
      
      @Override
      public String toString() {
        return waitable.toString();
      }
    });
    final ZThread thread = zigvaThreadFactory.newDaemonThread(myRunnable).ztart();
    
    return new ConvenienceWaitable() {
    
      @Override
      public boolean waitFor(long timeoutInMillis) {
        return myRunnable.waitFor(timeoutInMillis);
      }
    
      @Override
      public void waitFor() {
        waitFor(0);
      }
      
      @Override
      public String toString() {
        return waitable.toString();
      }
    };
  }
}
