// Copyright 2008 Google Inc.  All Rights Reserved.

package com.google.zigva.io;

/**
 * This is a {@link PumpFactory} that builds {@link Pump}s that read from their
 * sources and discard the data.
 * 
 * @author zorzella
 */
public class PumpToVoid<T> implements PumpFactory<T> {

  public PumpToVoid() {
  }
  
  @Override
  public Pump getPumpFor(final Source<T> source) {
    return new Pump() {
    
      @Override
      public void kill() {
      }
    
      @Override
      public void run() {
        while (!source.isEndOfStream()) {
          source.read();
        }
        source.close();
      }
    };
  }
}
