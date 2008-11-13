// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.exec;


public interface Killable {

  public static class FailedToKillException extends RuntimeException {

    public FailedToKillException(Throwable e) {
      super(e);
    }
  }
  
  /**
   * @throws FailedToKillException if
   */
  void kill();
  
}
