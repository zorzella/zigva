// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.zigva.java.io;

import com.google.zigva.collections.CircularBuffer;
import com.google.zigva.io.DataNotReadyException;
import com.google.zigva.io.DataSourceClosedException;
import com.google.zigva.io.EndOfDataException;
import com.google.zigva.io.FailedToCloseException;
import com.google.zigva.io.Source;

import java.io.IOException;
import java.io.InputStream;

/**
 * Implementation of {@link Source} backed by an {@link InputStream}.
 * 
 * @author Luiz-Otavio Zorzella
 * @author John Thomas
 */
public class InputStreamSource implements Source<Integer> {

  private static final int DEFAULT_CAPACITY = 100;
  private static final int DEFAULT_CLOSE_TIMEOUT = 500;
  
  private final Thread producer;
  private final CircularBuffer<Integer> queue;
  private final int closeTimeout;
  private final Object lock;

  private boolean isClosed;
  private Integer nextDataPoint;

  public InputStreamSource(InputStream in) {
    this(in, DEFAULT_CAPACITY, DEFAULT_CLOSE_TIMEOUT);
  }

  public InputStreamSource(final InputStream in, int capacity) {
    this(in, capacity, DEFAULT_CLOSE_TIMEOUT);
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
   *   e) Close right after "queue.put(dataPoint);"
   * 
   * There should be tests for each.
   */
  public InputStreamSource(final InputStream in, int capacity, int closeTimeout) {
    this.closeTimeout = closeTimeout;
    this.lock = "LOCK";
    this.queue = new CircularBuffer<Integer>(capacity, lock);
    this.producer = new Thread (new Runnable() {
      @Override
      public void run() {
        try {
          int dataPoint;
          do  {
            dataPoint = in.read();
            synchronized(lock) {
              queue.enq(dataPoint);
            }
          } while (dataPoint != -1 && !isClosed);
        } catch (InterruptedException e) {
          if (!isClosed) {
            throw new RuntimeException(e);
          }
          // "Normal" code path -- we have either interrupted a blocked read or 
          // put by closing this Source (cases "b" and "d" above).
        } catch (IOException e) {
          throw new RuntimeException(e);
        } finally {
          try {
            in.close();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }, "InputStreamSource Producer");
    this.producer.start();
  }

  @Override
  public void close() {
    if (isClosed) {
      throw new DataSourceClosedException();
    }
    isClosed = true;
    this.producer.interrupt();
    queue.interrupt();
    synchronized(lock) {
      lock.notifyAll();
    }
    try {
      this.producer.join(closeTimeout);
      if (this.producer.isAlive()) {
        throw new FailedToCloseException("Underlying stream is blocked. " +
            "Until it unblocks, there will be a thread TODO... " +
        "Suggest to use Interruptible");
      }

    } catch (InterruptedException e) {
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
        nextDataPoint = queue.deq();
    } catch (InterruptedException e) {
      if (!isClosed) {
        throw new RuntimeException(e);
      }
      // We have closed this Source while a thread was blocked on "take"
      throw new DataSourceClosedException();
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
