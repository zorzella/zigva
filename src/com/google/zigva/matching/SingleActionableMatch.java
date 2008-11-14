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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SingleActionableMatch implements ActionableMatch {

  private Pattern pattern;
  private List<String> results;
  
  public SingleActionableMatch(String regex) {
    this.pattern = Pattern.compile(regex);
  }

  public SingleActionableMatch(Pattern pattern) {
    this.pattern = pattern;
  }

  public Pattern pattern() {
    return pattern;
  }

  public boolean doWork(String lineToTest) {
    Matcher m = pattern().matcher(lineToTest);
    if (m.matches()) {
      if (results != null) {
        throw new IllegalStateException ("Matched pattern more than once.");
      }
      results = new ArrayList<String>(m.groupCount()); 
      for (int i=0; i<=m.groupCount();i++){
        results.add(m.group(i));
      }
      return true;
    }
    return false;
  }

  public String getResult(int index) {
    return this.results.get(index);
  }
  
}
