// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.io;

import com.google.zigva.lang.SinkFactory;
import com.google.zigva.lang.ZigvaInterruptedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ForkingSinkFactory<T> implements SinkFactory<T> {

  private final SinkFactory<T>[] sinkFactories;

  public ForkingSinkFactory(SinkFactory<T>... sinkFactories) {
    this.sinkFactories = sinkFactories;
  }
  
  @Override
  public Sink<T> build(Source<T> source) {
    List<Sink<T>> sinks = new ArrayList<Sink<T>>(sinkFactories.length);
    for (SinkFactory<T> sinkFactory : sinkFactories) {
      sinks.add(sinkFactory.build(source));
    }
    return new MySink<T>(sinks);
  }

  private static final class MySink<T> implements Sink<T> {
    private final Collection<Sink<T>> sinks;
   
    
    public MySink(List<Sink<T>> sinks) {
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
}
