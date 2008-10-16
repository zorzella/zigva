// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.io;

import com.google.zigva.collections.CircularBuffer;

import java.io.IOException;
import java.io.Writer;

public class WriterSink implements Sink<Character> {

  private static final int DEFAULT_CAPACITY = 100;
  private static final int DEFAULT_CLOSE_TIMEOUT = 500;
  
  private final Thread consumer;
  private final CircularBuffer<Character> queue;
  private final int closeTimeout;
  private final Object lock;

  
  private boolean isClosed;

  public WriterSink(final Writer out) {
    this(out, DEFAULT_CAPACITY, DEFAULT_CLOSE_TIMEOUT, "LOCK");
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
  public WriterSink(final Writer out, int capacity, int closeTimeout, Object lock) {
    this.closeTimeout = closeTimeout;
    this.lock = lock;
    this.queue = new CircularBuffer<Character>(capacity, lock);
    this.consumer = new Thread (new Runnable() {
      @Override
      public void run() {
        try {
          int dataPoint;
          while (queue.size() > 0 || !isClosed)  {
            synchronized(WriterSink.this.lock) {
              out.append(queue.deq());
            }
          }
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
            out.flush();
            out.close();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }, "WriterSink Thread");
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
    try {
      this.queue.enq(data);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

}
