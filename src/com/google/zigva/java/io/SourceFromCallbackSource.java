// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.java.io;

import com.google.zigva.exec.ThreadRunner;
import com.google.zigva.io.DataNotReadyException;
import com.google.zigva.io.DataSourceClosedException;
import com.google.zigva.io.EndOfDataException;
import com.google.zigva.io.FailedToCloseException;
import com.google.zigva.io.Source;
import com.google.zigva.java.io.CallbackSource.Callback;
import com.google.zigva.java.io.CallbackSource.CloseCallBack;
import com.google.zigva.lang.Closure;
import com.google.zigva.lang.ZigvaInterruptedException;

import java.util.concurrent.TimeUnit;

//Not thread-safe
public class SourceFromCallbackSource<T> implements Source<T> {

  private final CallbackSource<T> source;
  private long timeout = 1000;
  private boolean isClosed;

  private final Result<RuntimeException> closeResult = new Result<RuntimeException>();
  private final LockAndCondition lock = new ReentrantLockAndCondition();

  private final Result<T> data = new Result<T>();
  private final Result<Boolean> isEod = new Result<Boolean>();
  private final Callback<T> readCallback = new Callback<T>() {
    
    @Override
    public void dataPoint(T dataPoint) {
      data.set(dataPoint);
    }
    
    @Override
    public void endOfData() {
      isEod.set(true);
    }
    
    @Override
    public void exception(RuntimeException exception) {
      // Not yet implemented!
      throw new UnsupportedOperationException();
    }
  };

  private final ThreadRunner threadRunner;

  public SourceFromCallbackSource(CallbackSource<T> source, ThreadRunner threadRunner) {
    this.source = source;
    this.threadRunner = threadRunner;
  }
  
  private final CloseCallBack closeCallback = new CloseCallBack() {
    @Override
    public void done(RuntimeException exception) {
      closeResult.set(exception);
    }
  };

  private final Closure<Void> sourceCloseClosure = new Closure<Void>() {
    
    @Override
    public Void run() {
      source.close(closeCallback);
      lock.getLock().lock();
      try {
        lock.getCondition().signal();
        return null;
      } finally {
        lock.getLock().unlock();
      }
    }
  };

  @Override
  public void close() throws ZigvaInterruptedException {
    if (isClosed) {
      throw new DataSourceClosedException();
    }
    isClosed = true;
    
    this.threadRunner.schedule(sourceCloseClosure);

    lock.getLock().lock();
    try {
      try {
        if (!closeResult.hasResult() && 
            (!lock.getCondition().await(timeout, TimeUnit.MILLISECONDS))) {
          throw new FailedToCloseException();
        }
        RuntimeException exception = closeResult.get();
        if (exception != null) {
          throw new FailedToCloseException(exception);
        }
      } catch (InterruptedException e) {
        throw new ZigvaInterruptedException(e);
      }
    } finally {
      lock.getLock().unlock();
    }
  }

  @Override
  public boolean isClosed() {
    if (isClosed) {
      throw new DataSourceClosedException();
    }
    return isClosed;
  }


  @Override
  public boolean isEndOfStream() throws DataSourceClosedException, ZigvaInterruptedException {
    if (data.hasResult()) {
      return false;
    }
    if (isEod.hasResult()) {
      return true;
    }
    source.readTo(readCallback);
    return isEod.hasResult();
  }

  @Override
  public boolean isReady() throws DataSourceClosedException {
    // return data.hasResult() || isEod.hasResult();
    return true;
  }

  @Override
  public T read() throws DataNotReadyException, DataSourceClosedException, EndOfDataException {
    if (!data.hasResult()) {
      if (isEod.hasResult()) {
        throw new EndOfDataException();
      }
      throw new DataNotReadyException();
    }
    T result = data.clear();
//    isEod.clear();
    return result;
  }
}
