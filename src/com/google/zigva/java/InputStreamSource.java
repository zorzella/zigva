// Copyright 2008 Google Inc.  All Rights Reserved.
// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.zigva.java;

import com.google.zigva.io.DataNotReadyException;
import com.google.zigva.io.DataSourceClosedException;
import com.google.zigva.io.EndOfDataException;
import com.google.zigva.io.Source;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Implementation of {@link Source} backed by an {@link InputStream}.
 * 
 * @author Luiz-Otavio Zorzella
 * @author John Thomas
 */
final class InputStreamSource implements Source {

  //TODO think (maybe expose?)
  private static final int DEFAULT_CAPACITY = 100;
  
  private final InputStream in;
  private boolean isClosed;
  private Integer nextDataPoint;
  private final Thread producer;
  private final BlockingQueue<Integer> queue;

  public InputStreamSource(InputStream in) {
    this(in, DEFAULT_CAPACITY);
  }
  
  public InputStreamSource(final InputStream in, int capacity) {
    this.in = in;
    this.queue = new ArrayBlockingQueue<Integer>(capacity);
    this.producer = new Thread(new Runnable(){
      @Override
      public void run() {
        try {
          int dataPoint;
          do  {
            dataPoint = in.read();
            queue.put(dataPoint);
          } while (dataPoint != -1);
          //TODO: think!!!!
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    });
    this.producer.start();
  }
  
  @Override
  public void close() {
    isClosed = true;
    try {
      in.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isEndOfStream() throws DataSourceClosedException {
    throwIfClosed();
    if (nextDataPoint != null) {
      return nextDataPoint == -1;
    }
    try {
      nextDataPoint = queue.take();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    return nextDataPoint == -1;
  }

  @Override
  public boolean isReady() throws DataSourceClosedException {
    throwIfClosed();
    return nextDataPoint != null || queue.size() > 0;
  }

  @Override
  public int read() throws DataNotReadyException, DataSourceClosedException, EndOfDataException {
    throwIfClosed();
    if (!isReady()) {
      throw new DataNotReadyException();
    }
    if (isEndOfStream()) {
      throw new EndOfDataException();
    }
    Integer result = nextDataPoint;
    nextDataPoint = null;
    return result;
  }

  private void throwIfClosed() {
    if (isClosed) {
      throw new DataSourceClosedException();
    }
  }
}