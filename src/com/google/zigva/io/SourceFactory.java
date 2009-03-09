// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.zigva.io;

public interface SourceFactory<T> {
  
  Source<T> build();

}