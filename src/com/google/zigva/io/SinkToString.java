// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.io;

public class SinkToString implements Sink<Character> {

  private final StringBuilder buffer = new StringBuilder();

  @Override
  public void close() {
  }

  @Override
  public boolean isReady() throws DataSourceClosedException {
    return true;
  }

  @Override
  public void write(Character data) throws DataSourceClosedException {
    buffer.append(data);
  }
  
  @Override
  public String toString() {
    return buffer.toString();
  }

}
