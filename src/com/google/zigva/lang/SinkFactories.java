// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.lang;

import com.google.zigva.io.AppendablePassiveSink;
import com.google.zigva.io.SimpleSink;
import com.google.zigva.io.Sink;
import com.google.zigva.io.Source;

public class SinkFactories {

  public static SinkFactory<Character> from(final Appendable out) {
    return new SinkFactory<Character>() {
    
      @Override
      public Sink build(Source<Character> source) {
        return new SimpleSink<Character> (source, new AppendablePassiveSink(out));
      }
    };
  }
}
