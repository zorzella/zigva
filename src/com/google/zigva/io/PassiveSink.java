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

import java.io.Closeable;

/**
 *   Source source = null;
 *    Sink sink = null;
 *    while (!source.isEndOfStream()) {
 *      sink.write(source.read());
 *    }
 *    source.flush();
 *    source.close();
 *
 * @author Luiz-Otavio Zorzella, John Thomas
 *
 */
public interface PassiveSink<T> extends Closeable {

  /**
   * Returns true if this {@link PassiveSink} is ready to receive more data -- i.e., 
   * calling {@link #write(Object)} will not block.
   *
   * @throws DataSourceClosedException if this {@link PassiveSink} has been closed
   */
  boolean isReady() throws DataSourceClosedException;

  /**
   * Writes {@code data} to this stream. This is a blocking operation. I.e.,
   * it will block your thread until it {@link #isReady()}.
   * 
   * @throws DataSourceClosedException if this {@link PassiveSink} has been closed
   */
  void write(T data) throws DataSourceClosedException, ZigvaInterruptedException;

  /**
   * Closes this {@link PassiveSink}, and releases all resources it holds.
   * 
   * <p>If a thread is currently blocked on {@link #write(Object)} it gets 
   * a {@link DataSourceClosedException}.
   * 
   * <p>Any further attempt to call any method in this class will get a 
   * {@link DataSourceClosedException}.
   *
   * @throws DataSourceClosedException if this {@link PassiveSink} has been closed
   * 
   * @throws FailedToCloseException if there were problems closing this 
   * {@link Source}.
   */
  void close();
  
  /**
   * Blocks until any and all the data written to it and buffered is sent to the
   * underlying resource.
   * 
   * <p>Calling {@link #close()} before/without flushing is liable to cause data
   * loss.
   */
  void flush() throws ZigvaInterruptedException;
}
