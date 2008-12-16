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

import com.google.zigva.lang.ErrFactory;
import com.google.zigva.lang.OutFactory;

public class SinkToString implements Sink<Character> {

  private final StringBuilder data = new StringBuilder();

  @Override
  public void close() {
  }

  @Override
  public boolean isReady() throws DataSourceClosedException {
    return true;
  }

  @Override
  public void write(Character c) throws DataSourceClosedException {
    data.append(c);
  }
  
  @Override
  public String toString() {
    return data.toString();
  }

  @Override
  public void flush() {
  }
  
  public ErrFactory asErrFactory() {
    return new ErrFactory(){
      @Override
      public Sink<Character> buildErr(Source<Character> source) {
        return SinkToString.this;
      }
    };
  }

  public OutFactory asOutFactory() {
    return new OutFactory(){
      @Override
      public Sink<Character> buildOut(Source<Character> source) {
        return SinkToString.this;
      }
    };
  }

}
