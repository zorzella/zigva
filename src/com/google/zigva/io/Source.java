package com.google.zigva.io;

import java.io.Closeable;

public interface Source extends Closeable {

  public int read(char[] buffer);
}
