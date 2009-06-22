// Copyright 2008 Google Inc.  All Rights Reserved.
// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.zigva.java.io;

import com.google.inject.Inject;
import com.google.zigva.exec.ThreadRunner;
import com.google.zigva.exec.impl.SimpleThreadRunner;
import com.google.zigva.io.Source;
import com.google.zigva.lang.ZigvaThreadFactory;

import java.io.Reader;

public final class SourceOfCharFromReader {
  
  private static final int DEFAULT_CAPACITY = 100;
  private static final int DEFAULT_CLOSE_TIMEOUT = 500;
  
  private final ZigvaThreadFactory threadFactory;
  private final int capacity;
  private final int closeTimeout;
  private final Object lock;
  private final ThreadRunner threadRunner;
  
  public SourceOfCharFromReader(
      ZigvaThreadFactory threadFactory,
      int capacity, 
      int closeTimeout, 
      Object lock, ThreadRunner threadRunner
      ) {
    super();
    this.capacity = capacity;
    this.closeTimeout = closeTimeout;
    this.lock = lock;
    this.threadFactory = threadFactory;
    this.threadRunner = threadRunner;
  }

  @Inject
  public SourceOfCharFromReader(ZigvaThreadFactory threadFactory, ThreadRunner threadRunner) {
    this(threadFactory, DEFAULT_CAPACITY, DEFAULT_CLOSE_TIMEOUT, 
        new StringBuilder("LOCK"), threadRunner);
  }
  
  public SourceOfCharFromReader(ZigvaThreadFactory zigvaThreadFactory) {
    this(zigvaThreadFactory, new SimpleThreadRunner(zigvaThreadFactory));
  }

  public Source<Character> create(Reader in) {
    //!!!!!!!!!!!!! TODO !!!!!!!!!!!!!
    return oldCreate(in);
  }
  
  public Source<Character> newCreate(Reader in) {
    if (in == null) {
      throw new NullPointerException();
    }
    return new SourceFromCallbackSource<Character>(new ReaderCallbackSource(in), threadRunner);
//    return new ReaderSource(threadFactory, in, capacity, closeTimeout, lock);
  }

  public Source<Character> oldCreate(Reader in) {
    if (in == null) {
      throw new NullPointerException();
    }
    return new ReaderSource(threadFactory, in, capacity, closeTimeout, lock);
  }
  
  public SourceOfCharFromReader withCapacity(int capacity) {
    return new SourceOfCharFromReader(threadFactory, capacity, closeTimeout, lock, threadRunner);
  }

  public SourceOfCharFromReader withCloseTimeout(int closeTimeout) {
    return new SourceOfCharFromReader(threadFactory, capacity, closeTimeout, lock, threadRunner);
  }

  public SourceOfCharFromReader withLock(Object lock) {
    return new SourceOfCharFromReader(threadFactory, capacity, closeTimeout, lock, threadRunner);
  }
  
  public SourceOfCharFromReader withCombo(int capacity, int closeTimeout, Object lock) {
    return new SourceOfCharFromReader(threadFactory, capacity, closeTimeout, lock, threadRunner);
  }
}