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

package com.google.zigva.io;

import com.google.inject.Inject;
import com.google.zigva.collections.CircularBuffer;
import com.google.zigva.lang.NamedRunnable;
import com.google.zigva.lang.ZigvaInterruptedException;
import com.google.zigva.lang.ZigvaThreadFactory;

import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamPassiveSink implements PassiveSink<Character> {

  private static final int DEFAULT_CAPACITY = 100;
  private static final int DEFAULT_CLOSE_TIMEOUT = 500;
  
  private final OutputStream out;
  private final Thread consumer;
  private final CircularBuffer<Character> queue;
  private final int closeTimeout;
  private final Object lock;

  private boolean isClosed;

  public static final class Builder {
    
    private final ZigvaThreadFactory threadFactory;
    private final int capacity;
    private final int closeTimeout;
    private final Object lock;

    public OutputStreamPassiveSink create(OutputStream out) {
      return new OutputStreamPassiveSink(
          threadFactory, 
          out, 
          capacity, 
          closeTimeout, 
          lock);
    }
    
    @Inject
    public Builder(ZigvaThreadFactory threadFactory) {
      this(threadFactory, DEFAULT_CLOSE_TIMEOUT, 
          DEFAULT_CAPACITY, 
          new StringBuilder("LOCK"));
    }

    public Builder(
        ZigvaThreadFactory threadFactory, 
        int capacity,
        int closeTimeout, 
        Object lock) {
      this.threadFactory = threadFactory;
      this.capacity = capacity;
      this.closeTimeout = closeTimeout;
      this.lock = lock;
    }

    public Builder withCombo(
        int capacity, 
        int closeTimeout, 
        Object lock) {
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
  private OutputStreamPassiveSink(
      final ZigvaThreadFactory threadFactory,
      final OutputStream out, int capacity, int closeTimeout, Object lock) {
    if (out == null) {
      throw new IllegalArgumentException("The appendable should not be 'null'");
    }
    this.out = out;
    this.closeTimeout = closeTimeout;
    this.lock = lock;
    this.queue = new CircularBuffer<Character>(capacity, lock);
    this.consumer = threadFactory.newDaemonThread (new NamedRunnable() {
      @Override
      public void run() {
        try {
          int dataPoint;
          while (queue.size() > 0 || !isClosed)  {
            synchronized(OutputStreamPassiveSink.this.lock) {
              out.write(queue.deq());
            }
          }
        } catch (ZigvaInterruptedException e) {
          if (!isClosed) {
            throw e;
          }
          // "Normal" code path -- we have either interrupted a blocked read or 
          // put by closing this Source (cases "b" and "d" above).
        } catch (IOException e) {
          //TODO: does this make sense? Test!
          if (!isClosed) {
            close();
          }
          throw new RuntimeException(e);
        } catch (Exception e) {
          //TODO: test!
          if (!isClosed) {
            close();
          }
        } finally {
          try {
              out.flush();
              out.close();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }

      @Override
      public String getName() {
        return "AppendablePassiveSink Thread";
      }
    });
    this.consumer.start();
  }

  @Override
  public void close() {
    /* TODO
     * out.flush
     * out.close
     */
    this.isClosed = true;
    //TODO: test
    this.consumer.interrupt();
    try {
      consumer.join(closeTimeout);
    } catch (InterruptedException e) {
      throw new FailedToCloseException(e);
    }
  }

  @Override
  public boolean isReady() throws DataSourceClosedException {
    return !this.queue.isFull();
  }

  @Override
  public void write(Character data) throws DataSourceClosedException {
    this.queue.enq(data);
  }

  @Override
  public void flush() {
    this.queue.blockUntilEmpty();
    //TODO:test
    try {
      out.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
