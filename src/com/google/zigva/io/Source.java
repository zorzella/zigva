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

import com.google.zigva.lang.ZigvaInterruptedException;

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
 * @literal T the type of object returned by {@link #read()}
 * 
 * @author Luiz-Otavio Zorzella, John Thomas
 */
public interface Source<T> {

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
   * @throws DataSourceClosedException if this {@link Source} has been closed. 
   * This is also thrown... TODO
   */
  boolean isEndOfStream() throws DataSourceClosedException, ZigvaInterruptedException;
  
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
  T read() throws DataNotReadyException, DataSourceClosedException, EndOfDataException;

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
   * 
   * @throws FailedToCloseException if there were problems closing this 
   * {@link Source}.
   */
  void close() throws ZigvaInterruptedException;
  
  /**
   * Returns true if this {@link Source} has been closed, false otherwise.
   */
  boolean isClosed();
}
