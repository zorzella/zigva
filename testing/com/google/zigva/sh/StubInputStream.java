package com.google.zigva.sh;

import java.io.InputStream;

public class StubInputStream extends InputStream {

  @Override
  public int read() {
    return 0;
  }
}
