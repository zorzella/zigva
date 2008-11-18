// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.io;

import com.google.zigva.lang.ZigvaInterruptedException;

public class ForkingSink<T> implements Sink<T> {

  private final Sink<T>[] sinks;

  public ForkingSink(Sink<T>... sinks) {
    this.sinks = sinks;
  }
  
  @Override
  public void close() {
    for (Sink<T> sink : sinks) {
      sink.close();
    }
  }

  @Override
  public void flush() throws ZigvaInterruptedException {
    for (Sink<T> sink : sinks) {
      sink.flush();
    }
  }

  @Override
  public boolean isReady() throws DataSourceClosedException {
    for (Sink<T> sink : sinks) {
      if (!sink.isReady()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void write(T data) throws DataSourceClosedException, ZigvaInterruptedException {
    for (Sink<T> sink : sinks) {
      sink.write(data);
    }
  }

}
