package com.google.zigva.io;

/**
 * TODO (document)
 * 
 * <pre>
 * 
 *    Source s = ...;
 *    while (!s.isEndOfStream()) {
 *      int dataPoint = s.read();
 *    }
 *   s.close();
 *
 * </pre>
 * 
 * @author Luiz-Otavio Zorzella, John Thomas
 */
public interface Source {

  /**
   * Returns true if this Source has more data to be read, or has encountered 
   * an end of stream
   *
   * @throws DataSourceClosedException if this {@link Source} has been closed
   */
  boolean isReady() throws DataSourceClosedException;

  /**
   * Returns true when there is more data in this Source, i.e. it has not reached
   * the end of stream.
   * 
   * <p>This method assumes you know there's data available.
   * 
   * @throws DataNotReadyException if you read a Source where {@link #isEndOfStream()}
   *   returns true
   *
   * @throws DataSourceClosedException if this {@link Source} has been closed. 
   * This is also thrown... TODO
   */
  boolean isEndOfStream() throws DataNotReadyException, DataSourceClosedException;
  
  /**
   * Reads the next character from the stream. 
   * 
   * <p>This method assumes you know there's data available.
   * 
   * @throws DataNotReadyException if you read a Source where {@link #isReady()} 
   *   returns false
   * 
   * @throws EndOfDataException if you read a Source where {@link #isEndOfStream()}
   *   returns true

   * @throws DataSourceClosedException if this {@link Source} has been closed
   */
  int read() throws DataNotReadyException, DataSourceClosedException, EndOfDataException;

  /**
   * Closes this source, and releases all resources it holds.
   * 
   * <p>If a thread is currently blocked on {@link #isEndOfStream()} it gets 
   * a {@link DataSourceClosedException}.
   * 
   * <p>Any further attempt to call any method in this class will get a 
   * {@link DataSourceClosedException}.
   *
   * @throws DataSourceClosedException if this {@link Source} has been closed
   */
  void close();
  
}
