// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.java.io;

import com.google.zigva.lang.ZigvaInterruptedException;

public class Result<T> {

  private T result;
  private boolean hasResult = false;
  
  public void set(T result) {
    synchronized (this) {
      this.result = result;
      this.hasResult = true;
      this.notify();
    }
  }
  
  public T clear() {
    synchronized (this) {
      T temp = result;
      this.hasResult = false;
      this.result = null;
      return temp;
    }
  }
  
  public static class NoResultException extends RuntimeException {
    
  }
  
  public T get() {
    synchronized(this) {
      if (!hasResult) {
        throw new NoResultException();
      }
      return result;
    }
  }

  public T getAndClear() {
    synchronized(this) {
      if (!hasResult) {
        throw new NoResultException();
      }
      return result;
    }
  }
  
  public boolean hasResult () {
    return hasResult;
  }
  
  public T blockingGet() {
    synchronized (this) {
      while(!hasResult) {
        try {
          this.wait();
        } catch (InterruptedException e) {
          throw new ZigvaInterruptedException(e);
        }
      }
      return result;
    }
  }
  
  @Override
  public String toString() {
    if (!hasResult) {
      return "[NO RESULT]";
    }
    return result.toString();
  }
}
