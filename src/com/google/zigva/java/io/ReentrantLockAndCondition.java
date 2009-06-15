// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.java.io;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockAndCondition implements LockAndCondition {

  private final Lock lock;
  private final Condition condition;

  public ReentrantLockAndCondition() {
    this.lock = new ReentrantLock();
    this.condition = lock.newCondition();
  }
    
  @Override
  public Condition getCondition() {
    return condition;
  }

  @Override
  public Lock getLock() {
    return lock;
  }
}
