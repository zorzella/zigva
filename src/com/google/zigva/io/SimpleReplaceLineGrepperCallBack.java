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

import java.util.regex.Matcher;

public class SimpleReplaceLineGrepperCallBack extends RegexLineCatcherCallBack {

  private String replacement;

  public SimpleReplaceLineGrepperCallBack(String replacement, String regex) {
    super (regex);
    this.replacement = replacement;
  }
  
//  public Set<String> getTransformedMatches(){
//    return this.transformedMatches;
//  }
  
  @Override
  protected String transform(String lineToTest, Matcher m) {
    return m.replaceFirst(replacement);
  }


}
