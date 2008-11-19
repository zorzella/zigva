// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.io;

import com.google.zigva.exec.ZigvaTask;

public class StubZigvaTask implements ZigvaTask {

  @Override
  public void kill() {
  }

  @Override
  public void run() throws RuntimeException {
  }

  @Override
  public String getName() {
    return "StubZigvaTask";
  }
}
