// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.zigva.java.io;

import com.google.inject.Inject;
import com.google.zigva.collections.CircularBuffer;
import com.google.zigva.io.DataNotReadyException;
import com.google.zigva.io.DataSourceClosedException;
import com.google.zigva.io.EndOfDataException;
import com.google.zigva.io.FailedToCloseException;
import com.google.zigva.io.Source;
import com.google.zigva.lang.NamedRunnable;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ThreadFactory;

/**
 * Implementation of {@link Source} backed by an {@link InputStream}.
 * 
 * @author Luiz-Otavio Zorzella
 * @author John Thomas
 */
public class InputStreamSource implements Source<Integer> {

  private final Thread producer;
  private final CircularBuffer<Integer> queue;
  private final int closeTimeout;
  private final Object lock;

  private boolean isClosed;
  private Integer nextDataPoint;

  public static final class Builder {
    
    private static final int DEFAULT_CAPACITY = 100;
    private static final int DEFAULT_CLOSE_TIMEOUT = 500;
    
    private final ThreadFactory threadFactory;
    private final int capacity;
    private final int closeTimeout;
    private final Object lock;
    
    public Builder(
        ThreadFactory threadFactory,
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
    public Builder(ThreadFactory threadFactory) {
      this(threadFactory, DEFAULT_CAPACITY, DEFAULT_CLOSE_TIMEOUT, 
          new StringBuilder("LOCK"));
    }
    
    public InputStreamSource create(InputStream in) {
      if (in == null) {
        throw new NullPointerException();
      }
      return new InputStreamSource(threadFactory, in, capacity, closeTimeout, lock);
    }
    
    public Builder withCapacity(int capacity) {
      return new Builder(threadFactory, capacity, closeTimeout, lock);
    }

    public Builder withCloseTimeout(int closeTimeout) {
      return new Builder(threadFactory, capacity, closeTimeout, lock);
    }

    public Builder withLock(Object lock) {
      return new Builder(threadFactory, capacity, closeTimeout, lock);
    }
    
    public Builder withCombo(int capacity, int closeTimeout, Object lock) {
      return new Builder(threadFactory, capacity, closeTimeout, lock);
    }
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
  public InputStreamSource(ThreadFactory threadFactory, 
      final InputStream in, int capacity, int closeTimeout, Object lock) {
    this.closeTimeout = closeTimeout;
    this.lock = lock;
    this.queue = new CircularBuffer<Integer>(capacity, lock);
    this.producer = threadFactory.newThread(new NamedRunnable() {
      @Override
      public void run() {
        try {
          int dataPoint;
          do  {
            dataPoint = in.read();
            synchronized(InputStreamSource.this.lock) {
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

      @Override
      public String getName() {
        return "InputStreamSource Producer";
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
