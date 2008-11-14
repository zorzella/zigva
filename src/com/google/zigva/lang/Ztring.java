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

import java.util.Arrays;
import java.util.Iterator;

public class Ztring {
  
  public static String join(CharSequence separator, Iterable<?> it){
    return join(separator, it.iterator());
  }
  
  public static String join(CharSequence separator, Iterator<?> it){
    if (it == null) {
      return null;
    }
    StringBuilder result = new StringBuilder();
    if (it.hasNext()) {
      result .append(it.next());
    }
    while(it.hasNext()) {
      result.append(separator);
      result.append(it.next());
    }
    return result.toString();
    
  }
  
  public static String join(CharSequence separator, Object[] args){
    return join(separator, Arrays.asList(args).iterator());
  }
  
  public static String joinVar(CharSequence separator, Object... args){
    return join(separator, args);
  }
  
}
