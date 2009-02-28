// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.io;

import com.google.zigva.lang.PumpFactory;

/**
 * This is a {@link PumpFactory} that builds {@link Pump}s that read from their
 * sources and write to a (fixed) StringBuilder.
 * 
 * @see PumpToStringList
 * 
 * @author zorzella
 */
public class PumpToString implements PumpFactory<Character> {

  private final StringBuilder data;

  public PumpToString() {
    this.data = new StringBuilder();
  }

  public PumpToString(StringBuilder data) {
    this.data = data;
  }
  
  public String asString() {
    return data.toString();
  }

  @Override
  public Pump getPumpFor(final Source<Character> source) {
    return new Pump() {
    
      @Override
      public void kill() {
      }
    
      @Override
      public void run() {
        while (!source.isEndOfStream()) {
          data.append(source.read());
        }
        source.close();
      }
    };
  }
}
