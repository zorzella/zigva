// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.exec;

import com.google.zigva.lang.ZRunnable;

public interface ThreadRunner {

  ZRunnable schedule(Runnable e);
  
}
