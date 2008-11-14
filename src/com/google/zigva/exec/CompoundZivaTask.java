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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadFactory;

class CompoundZivaTask implements ZigvaTask {

  private final ThreadFactory threadFactory;
  private final List<ZigvaTask> zivaTasks;

  public CompoundZivaTask(ThreadFactory threadFactory, ZigvaTask... zivaTasks) {
    this.threadFactory = threadFactory;
    this.zivaTasks = Lists.newArrayList(zivaTasks);
  }
  
  public CompoundZivaTask(ThreadFactory threadFactory, List<ZigvaTask> zivaTasks) {
    this.threadFactory = threadFactory;
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

  private final Collection<Thread> allThreads = Lists.newArrayList();
  
  @Override
  public void run() {
    for (ZigvaTask zivaTask : zivaTasks) {
      Thread thread = this.threadFactory.newThread(zivaTask);
      allThreads.add(thread);
      thread.start();
    }
    for (Thread t : allThreads) {
      try {
        t.join();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
        
        
      }
    }
  }
}
