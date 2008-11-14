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

package com.google.zigva.io.util;

public class AppendableFromLite implements Appendable {

  private final AppendableLite appendableLite;

  public AppendableFromLite(AppendableLite appendableLite) {
    this.appendableLite = appendableLite;
  }
  
  @Override
  public Appendable append(CharSequence csq) {
    if (csq == null) {
      appendNullChars();
    } else {
      for (char chr : csq.toString().toCharArray()) {
        appendableLite.append(chr);
      }
    }
    return this;
  }

  @Override
  public Appendable append(char c) {
    appendableLite.append(c);
    return this;
  }

  @Override
  public Appendable append(CharSequence csq, int start, int end) {
    if (csq == null) {
      appendNullChars();
    } else {
      append(csq.subSequence(start, end));
    }
    return this;
  }

  private AppendableLite appendNullChars() {
    return appendableLite.append('n').append('u').append('l').append('l');
  }
}
