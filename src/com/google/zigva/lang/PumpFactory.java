// Copyright 2008 Google Inc.  All Rights Reserved.
// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.zigva.lang;

import com.google.zigva.io.Pump;
import com.google.zigva.io.Source;

public interface PumpFactory<T> {
  
  Pump getPumpFor(Source<T> source);
  
}