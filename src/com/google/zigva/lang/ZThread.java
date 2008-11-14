// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.lang;

import com.google.common.base.Join;

public class ZThread extends Thread {

  private StackTraceElement[] threadCreationStack;

  /**
   * Copy of the parent's runable, for visibility
   */
  private Runnable r;
  
  private String originalName;
  
  public ZThread(Runnable r) {
    super(r);
    this.r = r;
  }
  
  @Override
  public synchronized void start() {
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
  
}
