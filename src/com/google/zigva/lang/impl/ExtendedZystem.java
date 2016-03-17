// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.lang.impl;

import com.google.inject.Inject;
import com.google.zigva.io.CharSequenceSource;
import com.google.zigva.io.Source;
import com.google.zigva.sys.Zystem;

public class ExtendedZystem extends DelegatingZystem {

  @Inject
  public ExtendedZystem(Zystem zystem) {
    super(zystem);
  }

  public void printOut(String message) {
    Source<Character> source = new CharSequenceSource(message);
    delegate.ioFactory().out().getPumpFor(source).run();
  }
  
  public void printOut(String message, Object... params) {
    printOut(String.format(message, params));
  }
}
