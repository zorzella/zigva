// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.java;

import com.google.zigva.io.DataNotReadyException;
import com.google.zigva.io.DataSourceClosedException;
import com.google.zigva.io.EndOfDataException;
import com.google.zigva.io.Source;

public class SourceSource<T> implements Source<T> {

  private final Source<T> source;
  private final boolean stopClose;

  public SourceSource(Source<T> source) {
    this(source, false);
  }

  public SourceSource(Source<T> source, boolean stopClose) {
    this.source = source;
    this.stopClose = stopClose;
  }
  
  @Override
  public void close() {
    if (!stopClose) {
      source.close();
    }
  }

  @Override
  public boolean isEndOfStream() throws DataSourceClosedException {
    return source.isEndOfStream();
  }

  @Override
  public boolean isReady() throws DataSourceClosedException {
    return source.isReady();
  }

  @Override
  public T read() throws DataNotReadyException, DataSourceClosedException, EndOfDataException {
    return source.read();
  }
  
  

}
