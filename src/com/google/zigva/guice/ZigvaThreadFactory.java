// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.guice;

import com.google.zigva.lang.ZThread;

import java.util.concurrent.ThreadFactory;

public class ZigvaThreadFactory implements ThreadFactory {

  @Override
  public Thread newThread(Runnable r) {
    return new ZThread(r);
  }

}
