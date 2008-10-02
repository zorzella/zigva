// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.java.io;

import com.google.zigva.io.DataNotReadyException;
import com.google.zigva.io.DataSourceClosedException;
import com.google.zigva.io.EndOfDataException;
import com.google.zigva.io.FailedToCloseException;
import com.google.zigva.io.Source;

import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Implementation of {@link Source} backed by a {@link Reader}.
 * 
 * @author Luiz-Otavio Zorzella
 */
public class ReaderSource implements Source<Character> {

  private static final int DEFAULT_CAPACITY = 100;
  private static final int DEFAULT_CLOSE_TIMEOUT = 500;

  private final Thread producer;
  //TODO: can't have BlockingQueue<Character> because of "-1". Think 
  private final BlockingQueue<Integer> queue;
  //  private final CircularBuffer<Integer> queue;
  private final int closeTimeout;
  private final Reader in;

  private boolean isClosed;
  private Integer nextDataPoint;

  public ReaderSource(Reader in) {
    this(in, DEFAULT_CAPACITY, DEFAULT_CLOSE_TIMEOUT);
  }

  public ReaderSource(Reader in, int capacity) {
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
  public ReaderSource(final Reader in, int capacity, int closeTimeout) {
    this.in = in;
    this.closeTimeout = closeTimeout;
    this.queue = new ArrayBlockingQueue<Integer>(capacity);
    //    this.queue = new CircularBuffer<Integer>(capacity);
    this.producer = new Thread (new Runnable() {
      @Override
      public void run() {
        try {
          int dataPoint;
          do  {
            //TODO: might make better use of "isReady"
            // TODO: simply use read(char[]) to avoid the cast
            dataPoint = in.read();
            queue.put(dataPoint);
          } while (dataPoint != -1 && !isClosed);
        } catch (InterruptedException e) {
          if (!isClosed) {
            throw new RuntimeException(e);
          }
          // "Normal" code path -- we have either interrupted a blocked read or 
          // put by closing this Source (cases "b" and "d" above).
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }, "ReaderSource Thread");
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
  public Character read() throws DataNotReadyException, DataSourceClosedException, EndOfDataException {
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

  private void throwIfClosed() {
    if (isClosed) {
      throw new DataSourceClosedException();
    }
  }
}
