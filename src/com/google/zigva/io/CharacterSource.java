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

public class CharacterSource implements Source<Character> {

  private final CharSequence string;
  private int pos = 0;
  private boolean isClosed;

  public CharacterSource(CharSequence string) {
    this.string = string;
  }
  
  @Override
  public void close() {
    throwIfClosed();
    isClosed = true;
  }

  private void throwIfClosed() {
    if (isClosed) {
      throw new DataSourceClosedException();
    }
  }

  @Override
  public boolean isEndOfStream() throws DataSourceClosedException {
    throwIfClosed();
    if (pos == string.length()) {
      return true;
    }
    return false;
  }

  @Override
  public boolean isReady() throws DataSourceClosedException {
    throwIfClosed();
    return true;
  }

  @Override
  public Character read() throws DataNotReadyException, DataSourceClosedException, EndOfDataException {
    return string.charAt(pos++);
  }

}
