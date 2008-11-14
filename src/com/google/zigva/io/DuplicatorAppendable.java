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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DuplicatorAppendable implements Appendable {

  private List<Appendable> appendables = new ArrayList<Appendable>();

  public static DuplicatorAppendable make(Appendable... appendables) {
    return new DuplicatorAppendable(appendables);
  }
  
  private DuplicatorAppendable (Appendable... appendables) {
    for (Appendable appendable: appendables) {
      if (appendable != null) {
        this.appendables.add(appendable);
      }
    }
  }

  //FIXME: make these append in separate threads
  public Appendable append(CharSequence csq) throws IOException {
    for (Appendable appendable: appendables) {
      appendable.append(csq);
    }
    return this;
  }

  public Appendable append(CharSequence csq, int start, int end)
      throws IOException {
    for (Appendable appendable: appendables) {
      appendable.append(csq, start, end);
    }
    return this;
  }

  public Appendable append(char c) throws IOException {
    for (Appendable appendable: appendables) {
      appendable.append(c);
    }
    return this;
  }

}
