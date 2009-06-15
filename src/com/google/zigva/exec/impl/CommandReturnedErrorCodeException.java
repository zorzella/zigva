// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.exec.impl;

public class CommandReturnedErrorCodeException extends RuntimeException {

  public CommandReturnedErrorCodeException(
      String message, RuntimeException cause) {
    super(message, cause);
  }

  public CommandReturnedErrorCodeException(
      RuntimeException cause) {
    super(cause);
  }
}
