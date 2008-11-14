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

import java.util.Collection;

public final class ExceptionCollection extends RuntimeException {

  public final Collection<RuntimeException> exceptions;
  
  public ExceptionCollection(Collection<RuntimeException> exceptions) {
    super(String.format(
        "%d exceptions thrown. The first exception is listed as a cause", 
        exceptions.size()), exceptions.iterator().next());
    this.exceptions = exceptions;
  }

  public static RuntimeException create(Collection<RuntimeException> exceptions) {
    if (exceptions.size() == 0) {
      throw new IllegalArgumentException(
          "Can't create an ExceptionCollection with no exceptions");
    }
    if (exceptions.size() == 1) {
      return exceptions.iterator().next();
    }
    return new ExceptionCollection(exceptions);
  }
}