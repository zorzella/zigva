// Copyright 2008 Google Inc.  All Rights Reserved.
// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.zigva.io;

import com.google.common.base.Join;

import java.util.Map;

public final class ThreadCountAsserter {
  
  private final Map<Thread, StackTraceElement[]>  allOriginalStackTraces;
  private final int expectedNoThreads;

  public ThreadCountAsserter() {
    this.allOriginalStackTraces = Thread.getAllStackTraces();
    this.expectedNoThreads = allOriginalStackTraces.keySet().size();
  }

  public void assertThreadCount() throws InterruptedException {
    long failAt = System.currentTimeMillis() + 500;
    while(true) {
      Map<Thread, StackTraceElement[]> allStackTraces = 
        Thread.getAllStackTraces();
      int currentCount = allStackTraces.keySet().size();
      
      if (currentCount == expectedNoThreads) {
        break;
      }

      if (failAt < System.currentTimeMillis()) {
        System.out.println(String.format(
            "**********BEFORE (%d) **************", expectedNoThreads));
        printStackTraces(allOriginalStackTraces);
        System.out.println(String.format(
            "**********AFTER (%d) **************", currentCount));
        printStackTraces(allStackTraces);
        System.out.println("**********END**************");
        throw new AssertionError(String.format(
            "Thread leak (%d != %d # threads). " +
        		"See system out for details", currentCount, expectedNoThreads));
      }
      Thread.sleep(10);
    }
  }

  private void printStackTraces(Map<Thread, StackTraceElement[]> allStackTraces) {
    for (Thread thread: allStackTraces.keySet()) {
      System.out.println(String.format(
          "*** Thread '%s': \n %s", thread.getName(), 
          Join.join("\n", allStackTraces.get(thread))));
    }
  }
}