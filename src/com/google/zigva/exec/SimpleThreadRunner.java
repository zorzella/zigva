// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.exec;

import com.google.inject.Inject;
import com.google.zigva.guice.ZigvaThreadFactory;
import com.google.zigva.lang.Runnables;
import com.google.zigva.lang.ZRunnable;

public class SimpleThreadRunner implements ThreadRunner {

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
}
