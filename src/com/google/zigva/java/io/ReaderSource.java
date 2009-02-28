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

import com.google.zigva.collections.CircularBuffer;
import com.google.zigva.io.DataNotReadyException;
import com.google.zigva.io.DataSourceClosedException;
import com.google.zigva.io.EndOfDataException;
import com.google.zigva.io.FailedToCloseException;
import com.google.zigva.io.Source;
import com.google.zigva.lang.NamedRunnable;
import com.google.zigva.lang.ZigvaInterruptedException;
import com.google.zigva.lang.ZigvaThreadFactory;

import java.io.IOException;
import java.io.Reader;

//TODO: this is not thread safe. I'm not sure I want to make it thread safe or not
/**
 * Implementation of {@link Source} backed by a {@link Reader}.
 * 
 * @author Luiz-Otavio Zorzella
 */
class ReaderSource implements Source<Character> {

  private final Thread producer;
  //TODO: can't have CircularBuffer<Character> because of "-1". Think 
  private final CircularBuffer<Integer> queue;
  private final int closeTimeout;
  private final Object lock;
  private final Reader in;

  private boolean isClosed;
  private Integer nextDataPoint;

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
  ReaderSource(ZigvaThreadFactory threadFactory, 
      final Reader in, int capacity, int closeTimeout, Object lock) {
    this.in = in;
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
          // "Normal" code path -- we have interrupted a blocked "enq" by 
          // closing this Source (case "d" above).
        } catch (IOException e) {
          if (!isClosed) {
            throw new RuntimeException(e);
          }
          //TODO: test
          // "Normal" code path -- we have interrupted a blocked "read" by
          // closing this Source (case "b" above).
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

  @Override
  public boolean isClosed() {
    return isClosed;
  }

  private void throwIfClosed() {
    if (isClosed) {
      throw new DataSourceClosedException();
    }
  }
  
  @Override
  public String toString() {
    return String.format(
        "ReaderSource for [%s]", in);
  }
}
