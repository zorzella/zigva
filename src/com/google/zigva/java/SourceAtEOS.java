package com.google.zigva.java;

import com.google.zigva.io.DataNotReadyException;
import com.google.zigva.io.DataSourceClosedException;
import com.google.zigva.io.EndOfDataException;
import com.google.zigva.io.Source;

/**
 * A {@link Source} that is already at its end of stream.
 * @author zorzella
 *
 * @param <T>
 */
public final class SourceAtEOS<T> implements Source<T> {
 
  private boolean isClosed;

  @Override
  public void close() {
    isClosed = true;
  }

  @Override
  public boolean isEndOfStream() throws DataSourceClosedException {
    throwIfClosed();
    return true;
  }

  private void throwIfClosed() {
    if (isClosed) {
      throw new DataSourceClosedException();
    }
  }

  @Override
  public boolean isReady() throws DataSourceClosedException {
    throwIfClosed();
    return true;
  }

  @Override
  public T read() throws DataNotReadyException,
      DataSourceClosedException, EndOfDataException {
    throwIfClosed();
    throw new EndOfDataException();
  }

  @Override
  public boolean isClosed() {
    return isClosed;
  }
}