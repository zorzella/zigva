package com.google.zigva.java.io;

import com.google.inject.Inject;
import com.google.zigva.io.FilePath;
import com.google.zigva.io.Source;
import com.google.zigva.lang.ZigvaThreadFactory;

import java.io.File;
import java.io.FileDescriptor;

public final class SourceOfCharFromFile {
  
  private static final int DEFAULT_CAPACITY = 100;
  private static final int DEFAULT_CLOSE_TIMEOUT = 500;
  
  private final ZigvaThreadFactory threadFactory;
  private final int capacity;
  private final int closeTimeout;
  private final Object lock;
  
  public SourceOfCharFromFile(
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
  public SourceOfCharFromFile(ZigvaThreadFactory threadFactory) {
    this(threadFactory, DEFAULT_CAPACITY, DEFAULT_CLOSE_TIMEOUT, 
        new StringBuilder("LOCK"));
  }
  
  public Source<Character> create(File in) {
    if (in == null) {
      throw new NullPointerException();
    }
    return new ReaderSource(threadFactory, Readers.buffered(in), capacity, closeTimeout, lock);
  }

  public Source<Character> create(FileDescriptor in) {
    if (in == null) {
      throw new NullPointerException();
    }
    return new ReaderSource(threadFactory, Readers.buffered(in), capacity, closeTimeout, lock);
  }

  public Source<Character> create(FilePath in) {
    if (in == null) {
      throw new NullPointerException();
    }
    return new ReaderSource(threadFactory, Readers.buffered(in), capacity, closeTimeout, lock);
  }
  
  public SourceOfCharFromFile withCapacity(int capacity) {
    return new SourceOfCharFromFile(threadFactory, capacity, closeTimeout, lock);
  }

  public SourceOfCharFromFile withCloseTimeout(int closeTimeout) {
    return new SourceOfCharFromFile(threadFactory, capacity, closeTimeout, lock);
  }

  public SourceOfCharFromFile withLock(Object lock) {
    return new SourceOfCharFromFile(threadFactory, capacity, closeTimeout, lock);
  }
  
  public SourceOfCharFromFile withCombo(int capacity, int closeTimeout, Object lock) {
    return new SourceOfCharFromFile(threadFactory, capacity, closeTimeout, lock);
  }
}