// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.lang;

import com.google.zigva.io.SinkToAppendable;
import com.google.zigva.io.PumpToSink;
import com.google.zigva.io.Pump;
import com.google.zigva.io.Source;

public class SinkFactories {

  public static PumpFactory<Character> from(final Appendable out) {
    return new PumpFactory<Character>() {
    
      @Override
      public Pump getPumpFor(Source<Character> source) {
        return new PumpToSink<Character> (source, new SinkToAppendable(out));
      }
    };
  }
}
