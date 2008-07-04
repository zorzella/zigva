package com.google.zigva.io;

import java.io.IOException;
import java.io.Writer;

public class AppendableWriter extends Writer {

  private final Appendable out;

  public AppendableWriter(Appendable out) {
    this.out = out;
  }
  
  @Override
  public void close() {
  }

  @Override
  public void flush() {
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    for (int i=off; i<off+len; i++) {
      out.append(cbuf[i]);
    }
  }
}
