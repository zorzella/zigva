// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.io;

import com.google.common.collect.Lists;
import com.google.zigva.collections.CircularBuffer;
import com.google.zigva.exec.ThreadRunner;
import com.google.zigva.lang.ConvenienceWaitable;
import com.google.zigva.lang.ZigvaInterruptedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ForkingSinkFactory<T> implements PumpFactory<T> {

  private final ThreadRunner threadRunner;
  private final PumpFactory<T>[] sinkFactories;

  public ForkingSinkFactory(
      ThreadRunner threadRunner,
      PumpFactory<T>... sinkFactories) {
    this.threadRunner = threadRunner;
    this.sinkFactories = sinkFactories;
  }
  
  @Override
  public Pump getPumpFor(final Source<T> source) {

    Collection<Source<T>> sources = 
      MultiplexedSource.sources(source, sinkFactories.length);
    Iterator<Source<T>> sourcesIt = sources.iterator();
    
    final List<Pump> sinks = new ArrayList<Pump>(sinkFactories.length);
    
    for (PumpFactory<T> sinkFactory : sinkFactories) {
      sinks.add(sinkFactory.getPumpFor(sourcesIt.next()));
    }
    
    return new ASink(sinkFactories, sinks, source);
  }
  
  private final class ASink implements Pump {
    
    private final List<Pump> sinks;
    private final Source<T> source;

    private ASink(PumpFactory<T>[] factories, List<Pump> sinks, Source<T> source) {
      this.sinks = sinks;
      this.source = source;
    }

    @Override
    public void kill() {
      for (Pump sink : sinks) {
        sink.kill();
      }
    }

    @Override
    public void run() {
      try {
        Collection<ConvenienceWaitable> waitables = 
          new ArrayList<ConvenienceWaitable>(sinks.size());
        
        for (Pump sink : sinks) {
          waitables.add(threadRunner.schedule(sink));
        }
        for (ConvenienceWaitable waitable : waitables) {
          waitable.waitFor();
        }
      } finally {
        source.close();
      }
    }
  }

  private static final class MultiplexedSource<T> {

    private final Collection<CircularBuffer<T>> buffers;
    private final Source<T> source;
    private final Object lock;

    public MultiplexedSource(
        Collection<CircularBuffer<T>> buffers, 
        Source<T> source, 
        Object lock) {
      this.buffers = buffers;
      this.source = source;
      this.lock = lock;
    }

    public static <T> Collection<Source<T>> sources(Source<T> source, int number) {
      Collection<CircularBuffer<T>> buffers = Lists.newArrayList();

      for (int i=0; i<number; i++) {
        buffers.add(new CircularBuffer<T>(1));
      }
      Object lock = new StringBuilder("ForkingSinkFactory lock");
      MultiplexedSource<T> multi = new MultiplexedSource<T>(buffers, source, lock);
      return multi.srcs();
    }
    
    private Collection<Source<T>> srcs() {
      List<Source<T>> result = Lists.newArrayList();
      Object lock = new StringBuilder("lock");
      for (CircularBuffer<T> buf : buffers) {
        result.add(new MySource<T>(
            source, 
            buffers, 
            buf, 
            lock));
      }
      return result;
    }

    private static final class MySource<T> implements Source<T> {
      
      private final Source<T> source;
      private final Collection<CircularBuffer<T>> allBuffers;
      private final CircularBuffer<T> thisBuffer;
      private final Object lock;
      private boolean isClosed;

      public MySource(
          Source<T> source, 
          Collection<CircularBuffer<T>> buffers, 
          CircularBuffer<T> thisBuffer, 
          Object lock) {
        this.source = source;
        this.allBuffers = buffers;
        this.thisBuffer = thisBuffer;
        this.lock = lock;
      }

      private enum State {
        HAS_DATA,
        HAS_NO_DATA_AND_SOURCE_NOT_READY,
        HAS_NO_DATA_BUT_SOME_BUFFERS_ARE_FULL,
        ALL_BUFFERS_READY_FOR_DATA;
      }

      @Override
      public void close() throws ZigvaInterruptedException {
        isClosed = true;
        synchronized (lock) {
          lock.notifyAll();
        }
      }

      @Override
      public boolean isEndOfStream() throws DataSourceClosedException, ZigvaInterruptedException {
        while (true) {
          throwIfClosed();
          synchronized (lock) {
            State state = state();
            if (state == State.HAS_DATA) {
              return false;
            }
            if (state == State.HAS_NO_DATA_BUT_SOME_BUFFERS_ARE_FULL) {
              try {
                lock.wait();
                continue;
              } catch (InterruptedException e) {
                throw new ZigvaInterruptedException(e);
              }
            }
          }
          if (source.isEndOfStream()) {
            return true;
          }
          synchronized (lock) {
            if (state() == State.ALL_BUFFERS_READY_FOR_DATA) {
              T data = source.read();
              for (CircularBuffer<T> buf : allBuffers) {
                buf.enq(data);
              }
            }
          }
        }
      }

      private void throwIfClosed() {
        if (isClosed) {
          throw new DataSourceClosedException();
        }
      }

      @Override
      public boolean isReady() throws DataSourceClosedException {
        throwIfClosed();
        return state() == State.HAS_DATA;
      }
      
      private State state () {
        if (!thisBuffer.isEmpty()) {
          return State.HAS_DATA;
        }
        if (!source.isReady()) {
          return State.HAS_NO_DATA_AND_SOURCE_NOT_READY;
        }
        for (CircularBuffer<T> buf : allBuffers) {
          if (buf.isFull()) {
            return State.HAS_NO_DATA_BUT_SOME_BUFFERS_ARE_FULL;
          }
        }
        return State.ALL_BUFFERS_READY_FOR_DATA;
      }

      @Override
      public T read() throws DataNotReadyException, DataSourceClosedException, EndOfDataException {
        if (isClosed) {
          throw new DataSourceClosedException();
        }
        if (!isReady()) {
          throw new DataNotReadyException();
        }
        synchronized (lock) {
          T result = thisBuffer.deq();
          lock.notifyAll();
          return result;
        }
      }

      @Override
      public boolean isClosed() {
        return isClosed;
      }
    }
    
    @Override
    public String toString() {
      return "multiplexed:" + source.toString();
    }
  }
}
