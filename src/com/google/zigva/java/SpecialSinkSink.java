// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.java;

import com.google.zigva.io.DataSourceClosedException;
import com.google.zigva.io.Sink;

public class SpecialSinkSink<T> implements Sink<T> {

  private final Sink<T> sink;
//  private final Object lock;
  
  public SpecialSinkSink(Sink<T> sink) { //, Object lock) {
    this.sink = sink;
//    this.lock = lock;
  }
  
  @Override
  public void close() {
//    synchronized (lock) {
//      lock.notifyAll();
//    }
  }

  @Override
  public boolean isReady() throws DataSourceClosedException {
    return sink.isReady();
  }

  @Override
  public void write(T data) throws DataSourceClosedException {
    sink.write(data);
  }

  @Override
  public void flush() {
    sink.flush();
  }

}
