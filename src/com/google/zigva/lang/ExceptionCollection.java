// Copyright 2008 Google Inc.  All Rights Reserved.
// Copyright 2008 Google Inc. All Rights Reserved.

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