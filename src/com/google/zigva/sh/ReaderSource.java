// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.sh;

import com.google.zigva.io.DataNotReadyException;
import com.google.zigva.io.DataSourceClosedException;
import com.google.zigva.io.EndOfDataException;
import com.google.zigva.io.FailedToCloseException;
import com.google.zigva.io.Source;

import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ReaderSource implements Source<Character> {

  private static final int DEFAULT_CAPACITY = 100;
  private static final int DEFAULT_CLOSE_TIMEOUT = 500;

  private final Reader in;
  private final int closeTimeout;
  //TODO: can't have BlockingQueue<Character> because of "-1". Think 
  private final BlockingQueue<Integer> queue;
  private final Thread producer;

  private boolean isClosed;
  private Integer nextDataPoint;

  public ReaderSource(Reader in) {
    this(in, DEFAULT_CAPACITY, DEFAULT_CLOSE_TIMEOUT);
  }

  public ReaderSource(Reader in, int capacity) {
    this(in, capacity, DEFAULT_CLOSE_TIMEOUT);
  }
  
  public ReaderSource(final Reader in, int capacity, int closeTimeout) {
    this.in = in;
    this.closeTimeout = closeTimeout;
    this.queue = new ArrayBlockingQueue<Integer>(capacity);
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
          //TODO: think!!!!
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
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
