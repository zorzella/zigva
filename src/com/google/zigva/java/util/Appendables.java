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

package com.google.zigva.java.util;

import java.util.Formatter;
import java.util.Locale;

import com.google.zigva.io.PassiveSink;

public class Appendables {
  
  /**
   * Equivalent to {@link String#format(String, Object...)}, except that it
   * can be used on an Appendable, rather than a String.
   */
  public static void format(Appendable out, String format, Object... args) {
    Formatter formatter = new Formatter(out);
    formatter.format(Locale.getDefault(), format, args);
  }
  
  /**
   * Equivalent to {@link String#format(Locale, String, Object...)} except that it
   * can be used on an Appendable, rather than a String.
   */
  public static void format(Locale locale, Appendable out, String format, Object... args) {
    Formatter formatter = new Formatter(out);
    formatter.format(locale, format, args);
  }
  
  public static Appendable from(final PassiveSink<Character> sink) {
    return new AppendableFromLite(new AppendableLite() {

      @Override
      public AppendableLite append(char c) {
        sink.write(c);
        return this;
      }
    });
  }
  
  public static Appendable from(AppendableLite lite) {
    return new AppendableFromLite(lite);
  }
  
}
