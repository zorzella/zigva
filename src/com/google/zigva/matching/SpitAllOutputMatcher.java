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

package com.google.zigva.matching;

import com.google.zigva.io.LineGrepperCallBack;

import java.io.IOException;



public class SpitAllOutputMatcher implements LineGrepperCallBack {

  private String prefix;
  private Appendable out;

  public SpitAllOutputMatcher(Appendable out, String prefix) {
    this.out = out;
    this.prefix = prefix + ": ";
  }
  
  public boolean doWork(String lineToTest) {
    try {
      out.append(prefix).append(lineToTest).append("\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }
}
