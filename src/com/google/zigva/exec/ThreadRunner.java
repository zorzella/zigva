// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.exec;

import com.google.zigva.lang.Closure;
import com.google.zigva.lang.ClosureResult;
import com.google.zigva.lang.ZRunnable;

public interface ThreadRunner {

  ZRunnable schedule(Runnable runnable);
  
  <T> ClosureResult<T> schedule(Closure<T> closure);
}
