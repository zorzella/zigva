// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.guice;

import com.google.inject.Provider;
import com.google.zigva.lang.Zystem;

//TODO: kill this class
public class Providers {

  public static Provider<Zystem> of(final Zystem zystem) {
    return new Provider<Zystem> () {
      @Override
      public Zystem get() {
        return zystem;
      }
    };
  }

}
