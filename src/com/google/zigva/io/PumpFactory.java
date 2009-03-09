// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.zigva.io;


public interface PumpFactory<T> {
  
  Pump getPumpFor(Source<T> source);
  
}