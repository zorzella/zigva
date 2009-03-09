// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.io;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * This is a {@link PumpFactory} that builds {@link Pump}s that read from their
 * sources and, each, write to a new StringBuilder in {@link #listOfData}.
 * 
 * @see PumpToString
 * 
 * @author zorzella
 */
public class PumpToStringList implements PumpFactory<Character> {

  public final List<StringBuilder> listOfData;

  public PumpToStringList() {
    this.listOfData = Lists.newArrayList();
  }

  public PumpToStringList(List<StringBuilder> listOfData) {
    this.listOfData = listOfData;
  }
  
  @Override
  public Pump getPumpFor(final Source<Character> source) {
    
    final StringBuilder data = new StringBuilder();
    listOfData.add(data);

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
