// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.sh;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.zigva.guice.ZivaModule;

public class Static {

  public static final Injector injector = Guice.createInjector(
      new ZivaModule ());
  
}
