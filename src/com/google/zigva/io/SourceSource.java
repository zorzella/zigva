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


public class SourceSource<T> implements Source<T> {

  private final Source<T> source;

  public SourceSource(Source<T> source) {
    this.source = source;
  }

  @Override
  public void close() {
    source.close();
  }

  @Override
  public boolean isEndOfStream() throws DataSourceClosedException {
    return source.isEndOfStream();
  }

  @Override
  public boolean isReady() throws DataSourceClosedException {
    return source.isReady();
  }

  @Override
  public T read() throws DataNotReadyException, DataSourceClosedException, EndOfDataException {
    return source.read();
  }
  
  @Override
  public boolean isClosed() {
    return source.isClosed();
  }
}
