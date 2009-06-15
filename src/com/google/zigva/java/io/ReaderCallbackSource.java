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

package com.google.zigva.java.io;

import com.google.zigva.io.DataSourceClosedException;

import java.io.IOException;
import java.io.Reader;

//TODO: this is not thread safe. I'm not sure I want to make it thread safe or not
/**
 * Implementation of {@link CallbackSource} backed by a {@link Reader}.
 * 
 * @author Luiz-Otavio Zorzella
 */
class ReaderCallbackSource implements CallbackSource<Character> {

  private final Reader in;

  private boolean isClosed;

  ReaderCallbackSource(Reader in) {
    this.in = in;
  }

  @Override
  public void readTo(CallbackSource.Callback<Character> callback) {
    try {
      int dp = in.read();
      if (dp == -1) {
        callback.endOfData();
      } else {
        callback.dataPoint(Character.toChars(dp)[0]);
      }
    } catch (IOException e) {
      callback.exception(new RuntimeException(e));
    }
  }

  @Override
  public void close(final CloseCallBack callBack) {
    if (isClosed) {
      throw new DataSourceClosedException();
    }
    isClosed = true;
    try {
      in.close();
      callBack.done(null);
    } catch (IOException e) {
      callBack.done(new RuntimeException(e));
    }
  }

  @Override
  public String toString() {
    return String.format(
        "ReaderCallbackSource for [%s]", in);
  }
}
