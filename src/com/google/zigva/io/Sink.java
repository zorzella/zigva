package com.google.zigva.io;

import java.io.Closeable;

public interface Sink extends Closeable {

  public int write(char[] buffer);
}
