// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.lang;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import java.util.List;
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
  
//  public static ConvenienceWaitable fromWaitables(final Iterable<ConvenienceWaitable> waitables) {
//    
//  }
  
  public static ConvenienceWaitable from(final Iterable<Pair> waitables) {
    
    ZigvaThreadFactory ztf = new ZigvaThreadFactory();
    
    final Set<ZRunnable> runnables = Sets.newHashSet();
    final Set<ConvenienceWaitable> finished = Sets.newHashSet();
    final List<RuntimeException> exceptions = Lists.newArrayList();
    
    for (final Pair waitable : waitables) {
      runnables.add(
          Runnables.fromRunnable(new Runnable() {

            @Override
            public void run() {
              try {
                waitable.waitable.waitFor();
              } catch (RuntimeException e) {
                exceptions.add(waitable.exceptionModifier.modify(e));
              } finally {
                synchronized(finished) {
                  finished.add(waitable.waitable);
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
          if (exceptions.size() > 0) {
            throw ExceptionCollection.create(exceptions);
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
          if (exceptions.size() > 0) {
            throw ExceptionCollection.create(exceptions);
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
  
  public interface ExceptionModifier {
    
    RuntimeException modify(RuntimeException exception);
    
  }
  
  public static final ExceptionModifier IDENTITY_EXCEPTION_MODIFIER = 
      new ExceptionModifier() {
  
    @Override
    public RuntimeException modify(RuntimeException exception) {
      return exception;
    }
  };

  public static class Pair {

    public final ConvenienceWaitable waitable;
    public final ExceptionModifier exceptionModifier;

    public Pair(
        ConvenienceWaitable waitable, 
        ExceptionModifier exceptionModifier) {
      this.waitable = waitable;
      this.exceptionModifier = exceptionModifier;
    }
  }
}
