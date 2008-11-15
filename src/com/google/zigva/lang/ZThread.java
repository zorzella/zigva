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

package com.google.zigva.lang;

import com.google.common.base.Join;

public class ZThread extends Thread {

  private StackTraceElement[] threadCreationStack;

  /**
   * Copy of the parent's runable, for visibility
   */
  private Runnable r;
  
  private String originalName;

  private RuntimeException exception;
  
  public ZThread(Runnable r) {
    super(r);
    this.r = r;
  }
  
  @Override
  public synchronized void start() {
    exception = null;
    threadCreationStack = new Exception().getStackTrace();
    //TODO: do this through reflection
//      if (r instanceof ThreadPoolExecutor.Worker) {
//        
//      }
      
    if (r instanceof NamedRunnable) {
      this.originalName = getName();
      setName(String.format("%s [%s]", ((NamedRunnable)r).getName(), originalName));
    }
    super.start();
  }
  
  @Override
  public void run() {
    try {
      super.run();
    } catch (RuntimeException e) {
      exception = e;
      e.printStackTrace();
    }
    if (originalName != null) {
      setName(originalName);
      originalName = null;
    }
  }
  
  public String details() {
    return String.format(
        "Thread %s started at:\n%s", 
        getName(),
        Join.join("\n", threadCreationStack));
  }
  
  public RuntimeException getException() {
    return exception;
  }
  
}
