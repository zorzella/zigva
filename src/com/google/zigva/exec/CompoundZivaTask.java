package com.google.zigva.exec;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadFactory;

class CompoundZivaTask implements ZivaTask {

  private final ThreadFactory threadFactory;
  private final List<ZivaTask> zivaTasks;

  public CompoundZivaTask(ThreadFactory threadFactory, ZivaTask... zivaTasks) {
    this.threadFactory = threadFactory;
    this.zivaTasks = Lists.newArrayList(zivaTasks);
  }
  
  public CompoundZivaTask(ThreadFactory threadFactory, List<ZivaTask> zivaTasks) {
    this.threadFactory = threadFactory;
    this.zivaTasks = zivaTasks;
  }

  @Override
  public void kill() {
    for (ZivaTask zivaTask : zivaTasks) {
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
    for (ZivaTask zivaTask : zivaTasks) {
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
