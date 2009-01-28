// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.exec;

import com.google.inject.Inject;
import com.google.zigva.guice.ZigvaThreadFactory;
import com.google.zigva.lang.Runnables;
import com.google.zigva.lang.ZRunnable;

public class SimpleThreadRunner implements ThreadRunner {

  private final class RunnableWithResult<T> implements Runnable {
    private final Closure<T> c;
    private T result;

    private RunnableWithResult(Closure<T> c) {
      this.c = c;
    }

    @Override
    public void run() {
      result = c.run();
    }
  }

  private final ZigvaThreadFactory zigvaThreadFactory;

  @Inject
  public SimpleThreadRunner(ZigvaThreadFactory zigvaThreadFactory) {
    this.zigvaThreadFactory = zigvaThreadFactory;
  }

  @Override
  public ZRunnable schedule(Runnable runnable) {
    ZRunnable result = Runnables.fromRunnable(runnable);
    zigvaThreadFactory.newDaemonThread(result).ztart();
    return result;
  }

  @Override
  public <T> ClosureResult<T> schedule(final Closure<T> c) {
    
    final RunnableWithResult<T> temp = new RunnableWithResult<T>(c);
    final ZRunnable scheduled = schedule(temp);
    
    return new ClosureResult<T>() {
      @Override
      public T get() {
        scheduled.waitFor();
        return temp.result;
      }
    };
  }
}
