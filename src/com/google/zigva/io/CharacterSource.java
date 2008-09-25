// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.io;

public class CharacterSource implements Source<Character> {

  private final CharSequence string;
  private int pos = 0;
  private boolean isClosed;

  public CharacterSource(CharSequence string) {
    this.string = string;
  }
  
  @Override
  public void close() {
    throwIfClosed();
    isClosed = true;
  }

  private void throwIfClosed() {
    if (isClosed) {
      throw new DataSourceClosedException();
    }
  }

  @Override
  public boolean isEndOfStream() throws DataSourceClosedException {
    throwIfClosed();
    if (pos == string.length()) {
      return true;
    }
    return false;
  }

  @Override
  public boolean isReady() throws DataSourceClosedException {
    throwIfClosed();
    return true;
  }

  @Override
  public Character read() throws DataNotReadyException, DataSourceClosedException, EndOfDataException {
    return string.charAt(pos++);
  }

}
