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

import com.google.common.collect.Lists;
import com.google.zigva.guice.ZigvaThreadFactory;
import com.google.zigva.io.PassiveSinkToString;
import com.google.zigva.lang.ExceptionCollection;
import com.google.zigva.lang.ZThread;
import com.google.zigva.lang.ZigvaInterruptedException;

import java.util.Collection;
import java.util.List;

class CompoundZivaTask implements ZigvaTask {

  private final Collection<ZThread> allThreads = Lists.newArrayList();
  private final ZigvaThreadFactory threadFactory;
  private final List<ZigvaTask> zivaTasks;
  private final PassiveSinkToString errMonitor;

  public CompoundZivaTask(ZigvaThreadFactory threadFactory, PassiveSinkToString errMonitor, 
      ZigvaTask... zivaTasks) {
    this.threadFactory = threadFactory;
    this.errMonitor = errMonitor;
    this.zivaTasks = Lists.newArrayList(zivaTasks);
  }
  
  public CompoundZivaTask(ZigvaThreadFactory threadFactory, 
      PassiveSinkToString errMonitor, List<ZigvaTask> zivaTasks) {
    this.threadFactory = threadFactory;
    this.errMonitor = errMonitor;
    this.zivaTasks = zivaTasks;
  }

  @Override
  public void kill() {
    for (ZigvaTask zivaTask : zivaTasks) {
      zivaTask.kill();
    }
  }

  @Override
  public String getName() {
    return "CompoundZivaTask";
  }

  @Override
  public void run() {
    Collection<RuntimeException> exceptions = Lists.newArrayList();
    
    for (ZigvaTask zivaTask : zivaTasks) {
      ZThread thread = this.threadFactory.newDaemonThread(zivaTask);
      allThreads.add(thread);
      thread.start();
    }
    for (ZThread t : allThreads) {
      try {
        t.join();
        RuntimeException exception = t.getException();
        if (exception != null) {
          exceptions.add(exception);
        }
      } catch (InterruptedException e) {
        throw new ZigvaInterruptedException(e);
      }
    }
    if (exceptions.size() > 0) {
      throw new RuntimeException(String.format(
          "stderr of command was:\n" +
          "******************************************\n" +
          "%s \n" +
          "******************************************", errMonitor.asString()), 
          ExceptionCollection.create(exceptions));
    }
  }
}
