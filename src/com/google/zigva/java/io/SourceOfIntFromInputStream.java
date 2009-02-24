// Copyright 2008 Google Inc.  All Rights Reserved.
// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.zigva.java.io;

import com.google.inject.Inject;
import com.google.zigva.io.Source;
import com.google.zigva.lang.ZigvaThreadFactory;

import java.io.InputStream;

public final class SourceOfIntFromInputStream {
  
  private static final int DEFAULT_CAPACITY = 100;
  private static final int DEFAULT_CLOSE_TIMEOUT = 500;
  
  private final ZigvaThreadFactory threadFactory;
  private final int capacity;
  private final int closeTimeout;
  private final Object lock;
  
  public SourceOfIntFromInputStream(
      ZigvaThreadFactory threadFactory,
      int capacity, 
      int closeTimeout, 
      Object lock
      ) {
    super();
    this.capacity = capacity;
    this.closeTimeout = closeTimeout;
    this.lock = lock;
    this.threadFactory = threadFactory;
  }

  @Inject
  public SourceOfIntFromInputStream(ZigvaThreadFactory threadFactory) {
    this(threadFactory, DEFAULT_CAPACITY, DEFAULT_CLOSE_TIMEOUT, 
        new StringBuilder("LOCK"));
  }
  
  public Source<Integer> create(InputStream in) {
    if (in == null) {
      throw new NullPointerException();
    }
    return new InputStreamSource(threadFactory, in, capacity, closeTimeout, lock);
  }
  
  public SourceOfIntFromInputStream withCapacity(int capacity) {
    return new SourceOfIntFromInputStream(threadFactory, capacity, closeTimeout, lock);
  }

  public SourceOfIntFromInputStream withCloseTimeout(int closeTimeout) {
    return new SourceOfIntFromInputStream(threadFactory, capacity, closeTimeout, lock);
  }

  public SourceOfIntFromInputStream withLock(Object lock) {
    return new SourceOfIntFromInputStream(threadFactory, capacity, closeTimeout, lock);
  }
  
  public SourceOfIntFromInputStream withCombo(int capacity, int closeTimeout, Object lock) {
    return new SourceOfIntFromInputStream(threadFactory, capacity, closeTimeout, lock);
  }
}