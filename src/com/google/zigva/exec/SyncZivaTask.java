/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zigva.exec;

import com.google.zigva.exec.ZigvaTask;
import com.google.zigva.lang.ZigvaInterruptedException;

public class SyncZivaTask implements WaitableZivaTask {

  private boolean done = false;
  private final ZigvaTask delegate;

  public SyncZivaTask(ZigvaTask delegate) {
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

  private RuntimeException exception = null;
  
  @Override
  public void run() {
    try {
      delegate.run();
    } catch (RuntimeException e) {
      exception = e;
    }
    synchronized(this) {
      done = true;
      notifyAll();
    }
    if (exception != null) {
      throw exception;
    }
  }

  @Override
  public void waitFor() {
    while(!done) {
      try {
        synchronized(this) {
          wait();
        }
      } catch (InterruptedException e) {
        throw new ZigvaInterruptedException(e);
      }
    }
    if (exception != null) {
      throw exception;
    }
  }
  
  public RuntimeException getException() {
    return exception;
  }

  @Override
  public boolean isFinished() {
    return done;
  }
}