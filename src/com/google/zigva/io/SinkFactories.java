// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.io;


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
