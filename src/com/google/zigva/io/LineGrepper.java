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

public class LineGrepper implements Appendable {

  private StringBuilder incompleteLine = new StringBuilder();

  private LineGrepperCallBack[] callBacks;

  private boolean stopOnMatch = true;
  
  public LineGrepper(LineGrepperCallBack... callBacks) {
    this.callBacks = callBacks;
  }
  
  public Appendable append(CharSequence csq) {
    incompleteLine.append(csq);
    dealWithIncompleteLine();
    return this;
  }

  public Appendable append(CharSequence csq, int start, int end) {
    incompleteLine.append(csq, start, end);
    dealWithIncompleteLine();
    return this;
  }

  public Appendable append(char c) {
    incompleteLine.append(c);
    dealWithIncompleteLine();
    return this;
  }

  private void dealWithIncompleteLine() {
    int indexOfNewLine = incompleteLine.indexOf("\n");
    while (indexOfNewLine >= 0) {
      String lineToTest = incompleteLine.substring(0, indexOfNewLine);
      for (LineGrepperCallBack callBack: callBacks) {
        if (callBack.doWork(lineToTest)) {
          if (this.stopOnMatch) {
            break;
          }
        }
      }
      incompleteLine.delete(0, indexOfNewLine + 1);
      indexOfNewLine = incompleteLine.indexOf("\n");
    }
  }

}
