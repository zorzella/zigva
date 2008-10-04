// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.java;

import com.google.zigva.io.DataNotReadyException;
import com.google.zigva.io.DataSourceClosedException;
import com.google.zigva.io.EndOfDataException;
import com.google.zigva.io.Source;

public class SpecialSourceSource<T> implements Source<T> {

  private final Source<T> source;
  private final Object lock;
  
  private boolean isClosed;

  public SpecialSourceSource(Source<T> source, Object lock) {
    this.source = source;
    this.lock = lock;
  }
  
  @Override
  public void close() {
    synchronized (lock) {
      isClosed = true;
      lock.notifyAll();
    }
  }

  @Override
  public boolean isEndOfStream() throws DataSourceClosedException {
    synchronized (lock) {
      while (!source.isReady()) {
        try {
          lock.wait();
        } catch (InterruptedException e) {
          if (!isClosed) {
            throw new RuntimeException(e);
          }
          throw new DataSourceClosedException();
        }
      }
      return source.isEndOfStream();
    }
  }

  @Override
  public boolean isReady() throws DataSourceClosedException {
    return source.isReady();
  }

  @Override
  public T read() throws DataNotReadyException, DataSourceClosedException, EndOfDataException {
    return source.read();
  }
  
  

}
