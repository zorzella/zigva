/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zigva.java.io;

import com.google.inject.Inject;
import com.google.zigva.collections.CircularBuffer;
import com.google.zigva.guice.ZigvaThreadFactory;
import com.google.zigva.io.DataNotReadyException;
import com.google.zigva.io.DataSourceClosedException;
import com.google.zigva.io.EndOfDataException;
import com.google.zigva.io.FailedToCloseException;
import com.google.zigva.io.Source;
import com.google.zigva.lang.NamedRunnable;
import com.google.zigva.lang.ZigvaInterruptedException;

import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.ThreadFactory;

//TODO: this is not thread safe. I'm not sure I want to make it thread safe or not
/**
 * Implementation of {@link Source} backed by a {@link Reader}.
 * 
 * @author Luiz-Otavio Zorzella
 */
public class ReaderSource implements Source<Character> {

  private final Thread producer;
  //TODO: can't have CircularBuffer<Character> because of "-1". Think 
  private final CircularBuffer<Integer> queue;
  private final int closeTimeout;
  private final Object lock;

  private boolean isClosed;
  private Integer nextDataPoint;

  public static final class Builder {
    
    private static final int DEFAULT_CAPACITY = 100;
    private static final int DEFAULT_CLOSE_TIMEOUT = 500;
    
    private final ZigvaThreadFactory threadFactory;
    private final int capacity;
    private final int closeTimeout;
    private final Object lock;
    
    public Builder(
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
    public Builder(ZigvaThreadFactory threadFactory) {
      this(threadFactory, DEFAULT_CAPACITY, DEFAULT_CLOSE_TIMEOUT, 
          new StringBuilder("LOCK"));
    }
    
    public ReaderSource create(Reader in) {
      if (in == null) {
        throw new NullPointerException();
      }
      return new ReaderSource(threadFactory, in, capacity, closeTimeout, lock);
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
  private ReaderSource(ZigvaThreadFactory threadFactory, 
      final Reader in, int capacity, int closeTimeout, Object lock) {
    this.closeTimeout = closeTimeout;
    this.lock = lock;
    this.queue = new CircularBuffer<Integer>(capacity, lock);
    this.producer = threadFactory.newDaemonThread(new NamedRunnable() {
      @Override
      public void run() {
        try {
          int dataPoint;
          do  {
            // TODO: simply use read(char[]) to avoid the cast
            dataPoint = in.read();
            synchronized(ReaderSource.this.lock) {
              queue.enq(dataPoint);
            }
          } while (dataPoint != -1 && !isClosed);
        } catch (ZigvaInterruptedException e) {
          if (!isClosed) {
            throw e;
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
        return "ReaderSource Thread";
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
      throw new ZigvaInterruptedException(e);
    }
  }

  @Override
  public boolean isEndOfStream() throws DataSourceClosedException {
    synchronized(this.lock) {
      throwIfClosed();
      try {
        while (queue.size() == 0 && nextDataPoint == null && !isClosed) {
          try {
            this.lock.wait();
          } catch (InterruptedException e) {
            throw new ZigvaInterruptedException(e);
          }
        }
        if (isClosed) {
          throw new DataSourceClosedException();
        }
        if (nextDataPoint == null) {
          nextDataPoint = queue.deq();
        }
      } catch (ZigvaInterruptedException e) {
        if (!isClosed) {
          throw e;
        }
        // We have closed this Source while a thread was blocked on "take"
        throw new DataSourceClosedException();
      }
      return nextDataPoint == -1;
    }
  }

  @Override
  public boolean isReady() throws DataSourceClosedException {
    throwIfClosed();
    return nextDataPoint != null || queue.size() > 0;
  }

  @Override
  public Character read() throws DataNotReadyException, DataSourceClosedException, EndOfDataException {
    synchronized(this.lock) {
      throwIfClosed();
      if (!isReady()) {
        throw new DataNotReadyException();
      }
      if (isEndOfStream()) {
        throw new EndOfDataException();
      }
      //TODO: ugly
      Character result = Character.toChars(nextDataPoint)[0];
      nextDataPoint = null;
      return result;
    }
  }

  private void throwIfClosed() {
    if (isClosed) {
      throw new DataSourceClosedException();
    }
  }
}
