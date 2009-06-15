// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.java.io;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public interface LockAndCondition {

  Lock getLock();
  
  Condition getCondition();
  
}
