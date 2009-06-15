// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.java.io;

public interface CallbackSource<T> {

  public interface Callback<T> {
    
    void dataPoint(T dataPoint);
    
    void endOfData();

    void exception(RuntimeException exception);
  }
  
  public interface CloseCallBack {
    void done(RuntimeException exception);
  }
  
  void readTo(Callback<T> callback);
  
  void close(CloseCallBack callback);
    
}
