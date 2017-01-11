package com.google.zigva.lang;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
  
  public static ConvenienceWaitable from(final Collection<Pair> waitabls) {
    
//    final ImmutableList<Pair> waitables = ImmutableList.copyOf(waitabls);
    final Collection<Pair> waitables = waitabls;
    
    final ZigvaThreadFactory ztf = new ZigvaThreadFactory();
    
    final Set<ZRunnable> runnables = Sets.newHashSet();
    final Set<ConvenienceWaitable> finished = Sets.newHashSet();
    final List<RuntimeException> exceptions = Lists.newArrayList();
    
    final CountDownLatch noOfThreadNotYetFinished = 
      new CountDownLatch(waitables.size());
    
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
                noOfThreadNotYetFinished.countDown();
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
    final int noOfRunnables = runnables.size();
    
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

        boolean result;
        try {
          result = noOfThreadNotYetFinished.await(timeoutInMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
          throw new ZigvaInterruptedException(e);
        }
        if (exceptions.size() > 0) {
          throw ClusterException.create(exceptions);
        }
        return result;
      }

      @Override
      public void waitFor() {
        try {
          noOfThreadNotYetFinished.await();
        } catch (InterruptedException e) {
          throw new ZigvaInterruptedException(e);
        }
        if (exceptions.size() > 0) {
          throw new CommandFailedException(ClusterException.create(exceptions));
        }
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
