// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.io;

public class FailedToCloseException extends RuntimeException {

  public FailedToCloseException() {
    super();
  }

  public FailedToCloseException(String message) {
    super(message);
  }

  public FailedToCloseException(String message, Throwable cause) {
    super(message, cause);
  }

  public FailedToCloseException(Throwable cause) {
    super(cause);
  }
}
