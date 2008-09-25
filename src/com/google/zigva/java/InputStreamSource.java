// Copyright 2008 Google Inc.  All Rights Reserved.
// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.zigva.java;

import com.google.zigva.io.DataNotReadyException;
import com.google.zigva.io.DataSourceClosedException;
import com.google.zigva.io.EndOfDataException;
import com.google.zigva.io.FailedToCloseException;
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
public final class InputStreamSource implements Source<Integer> {

  private static final int DEFAULT_CAPACITY = 100;
  private static final int DEFAULT_CLOSE_TIMEOUT = 500;
  
  private final InputStream in;
  private boolean isClosed;
  private Integer nextDataPoint;
  private final Thread producer;
  private final BlockingQueue<Integer> queue;
  private final int closeTimeout;

  public InputStreamSource(InputStream in) {
    this(in, DEFAULT_CAPACITY);
  }

  public InputStreamSource(final InputStream in, int capacity) {
    this(in, DEFAULT_CAPACITY, DEFAULT_CLOSE_TIMEOUT);
  }
  
  /*
   * There are 2 possible ways for the Producer to end:
   * 
   * 1) Sunnycase: on  "} while (dataPoint != -1);"
   * 2) If te Source is closed. In this case, there are 4 distinct states:
   *   a) Close rigth before "dataPoint = in.read();"
   *   b) Close while "dataPoint = in.read();" is blocking
   *   c) Close right before "queue.put(dataPoint);"
   *   d) Close while "queue.put(dataPoint);" is blocking
   * 
   * There should be tests for each.
   */
  public InputStreamSource(final InputStream in, int capacity, int closeTimeout) {
    this.in = in;
    this.closeTimeout = closeTimeout;
    this.queue = new ArrayBlockingQueue<Integer>(capacity);
    this.producer = new Thread (new Runnable() {
      @Override
      public void run() {
        try {
          int dataPoint;
          do  {
            dataPoint = in.read();
            queue.put(dataPoint);
          } while (dataPoint != -1 && !isClosed);
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
    if (isClosed) {
      throw new DataSourceClosedException();
    }
    isClosed = true;
    queue.clear();
    try {
      in.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      try {
        // TODO: I don't know if I can rely on "in.close()" always causing the
        // blocked read on the producer thread to get an exception. If so, this
        // is fine. Otherwise, we need to defensively code here...
//        this.producer.join();
        this.producer.join(closeTimeout);
        if (this.producer.isAlive()) {
          throw new FailedToCloseException("Underlying stream is blocked. " +
          		"Until it unblocks, there will be a thread TODO...");
        }
        
      } catch (InterruptedException ex) {
        // TODO: multiplex this exception with "e"
        throw new RuntimeException(ex);
      }
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
  public Integer read() throws DataNotReadyException, DataSourceClosedException, EndOfDataException {
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