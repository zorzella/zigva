// Copyright 2008 Google Inc.  All Rights Reserved.
// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.zigva.lang;

import com.google.zigva.io.Sink;
import com.google.zigva.io.Source;

public interface SinkFactory {
  
  Sink<Character> build(Source<Character> source);

}