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

package com.google.zigva.java;

import com.google.zigva.io.DataSourceClosedException;
import com.google.zigva.io.PassiveSink;

public class SpecialPassiveSinkSink<T> implements PassiveSink<T> {

  private final PassiveSink<T> sink;
//  private final Object lock;
  
  public SpecialPassiveSinkSink(PassiveSink<T> sink) { //, Object lock) {
    this.sink = sink;
//    this.lock = lock;
  }
  
  @Override
  public void close() {
//    synchronized (lock) {
//      lock.notifyAll();
//    }
  }

  @Override
  public boolean isReady() throws DataSourceClosedException {
    return sink.isReady();
  }

  @Override
  public void write(T data) throws DataSourceClosedException {
    sink.write(data);
  }

  @Override
  public void flush() {
    sink.flush();
  }

}