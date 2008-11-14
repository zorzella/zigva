// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.io;

public class SinkToString implements Sink<Character> {

  private final StringBuilder data = new StringBuilder();

  @Override
  public void close() {
  }

  @Override
  public boolean isReady() throws DataSourceClosedException {
    return true;
  }

  @Override
  public void write(Character c) throws DataSourceClosedException {
    data.append(c);
  }
  
  @Override
  public String toString() {
    return data.toString();
  }

  @Override
  public void flush() {
  }

}
