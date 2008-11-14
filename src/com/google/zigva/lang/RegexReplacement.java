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

package com.google.zigva.lang;

public class RegexReplacement {

  private String regex;
  private String replacement;

  public RegexReplacement(String regex, String replacement) {
    if (regex == null) {
      throw new IllegalArgumentException();
    }
    if (replacement == null) {
      throw new IllegalArgumentException();
    }
    this.regex = regex;
    this.replacement = replacement;
  }

  public String getRegex() {
    return regex;
  }

  public String getReplacement() {
    return replacement;
  }

  public static RegexReplacement[] fromPairs(String... pairs) {
    if (pairs.length % 2 != 0) {
      throw new IllegalArgumentException("Must be called with even number of elements.");
    }
    RegexReplacement[] result = new RegexReplacement[pairs.length / 2];
    for (int i=0; i<result.length; i++) {
      result[i] = new RegexReplacement(pairs[2*i], pairs[2*i + 1]);
    }
    return result;
  }
}
