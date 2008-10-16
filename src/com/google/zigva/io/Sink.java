package com.google.zigva.io;

import java.io.Closeable;

/**
 *   Source source = null;
 *    Sink sink = null;
 *    while (!source.isEndOfStream()) {
 *      sink.write(source.read());
 *    }
 *    source.close();
 *
 * @author Luiz-Otavio Zorzella, John Thomas
 *
 */
public interface Sink<T> extends Closeable {

  /**
   * Returns true if this {@link Sink} is ready to receive more data -- i.e., 
   * calling {@link #write(Object)} will not block.
   *
   * @throws DataSourceClosedException if this {@link Sink} has been closed
   */
  boolean isReady() throws DataSourceClosedException;

  /**
   * Writes {@code data} to this stream. This is a blocking operation. I.e.,
   * it will block your thread until it {@link #isReady()}.
   * 
   * @throws DataSourceClosedException if this {@link Sink} has been closed
   */
  void write(T data) throws DataSourceClosedException;

  /**
   * Closes this {@link Sink}, and releases all resources it holds.
   * 
   * <p>If a thread is currently blocked on {@link #write(Object)} it gets 
   * a {@link DataSourceClosedException}.
   * 
   * <p>Any further attempt to call any method in this class will get a 
   * {@link DataSourceClosedException}.
   *
   * @throws DataSourceClosedException if this {@link Sink} has been closed
   * 
   * @throws FailedToCloseException if there were problems closing this 
   * {@link Source}.
   */
  void close();
}
