// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.guice;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

public class InheritableThreadLocalScope implements Scope {

  public InheritableThreadLocalScope() {}
  
  @Override
  public <T> Provider<T> scope(Key<T> arg0, Provider<T> arg1) {
    return new Provider<T> () {
      InheritableThreadLocal<T> threadLocal = new InheritableThreadLocal<T>();
      @Override
      public T get() {
        return threadLocal.get();
      }
    };
  }
}
