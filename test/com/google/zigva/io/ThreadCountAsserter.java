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

package com.google.zigva.io;

import com.google.common.base.Joiner;

import java.util.Map;

public final class ThreadCountAsserter {
  
  private final Map<Thread, StackTraceElement[]>  allOriginalStackTraces;
  private final int expectedNoThreads;

  public ThreadCountAsserter() {
    this.allOriginalStackTraces = Thread.getAllStackTraces();
    this.expectedNoThreads = allOriginalStackTraces.keySet().size();
  }

  public void assertThreadCount() throws InterruptedException {
    long failAt = System.currentTimeMillis() + 1000;
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
          Joiner.on("\n").join(allStackTraces.get(thread))));
    }
  }
}