// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.lang;

import com.google.zigva.io.Source;

public class OutErr {

  private final Source<Character> out;
  private final Source<Character> err;
  
  private OutErr(
      Source<Character> out, 
      Source<Character> err) {
    this.out = out;
    this.err = err;
  }

  public static OutErr forOut(Source<Character> out) {
    return new OutErr(out, null);
  }
  
  public static OutErr forErr(Source<Character> err) {
    return new OutErr(null, err);
  }

  public static OutErr forOutErr(Source<Character> out, Source<Character> err) {
    return new OutErr(out, err);
  }
  
  public Source<Character> out() {
    return out;
  }
  
  public Source<Character> err() {
    return err;
  }
  
}
