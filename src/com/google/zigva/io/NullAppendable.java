package com.google.zigva.io;

import java.io.IOException;

public class NullAppendable implements Appendable{

  public static final Appendable INSTANCE = new NullAppendable();

  private NullAppendable(){}
  
  public Appendable append(CharSequence csq) throws IOException {
    return this;
  }

  public Appendable append(CharSequence csq, int start, int end) throws IOException {
    return this;
  }

  public Appendable append(char c) throws IOException {
    return this;
  }

}
