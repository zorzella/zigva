// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CharacterPumpFactories {

  public static PumpFactory<Character> forFile(final File f) {
    final Sink<Character> sink;
    try {
      sink = new SinkToAppendable(new FileWriter(f));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return new PumpFactory<Character>() {
    
      @Override
      public Pump getPumpFor(Source<Character> source) {
        return new PumpToSink<Character>(source, sink);
      }
    };
  }
  
}
