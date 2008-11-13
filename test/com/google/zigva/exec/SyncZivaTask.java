// Copyright 2008 Google Inc.  All Rights Reserved.
// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.zigva.exec;

import com.google.zigva.exec.ZivaTask;

public class SyncZivaTask implements ZivaTask {

  private boolean done = false;
  private final ZivaTask delegate;

  public SyncZivaTask(ZivaTask delegate) {
    this.delegate = delegate;
  }
  
  @Override
  public void kill() {
    delegate.kill();
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public void run() {
    delegate.run();
    synchronized(this) {
      done = true;
      notifyAll();
    }
  }

  @Override
  public void waitFor() {
    delegate.waitFor();
    while(!done) {
      try {
        synchronized(this) {
          wait();
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }
  
}