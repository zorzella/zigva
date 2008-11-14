package com.google.zigva.exec;

import com.google.common.collect.Lists;

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
  public void waitFor() {
    for (ZivaTask zivaTask : zivaTasks) {
      zivaTask.waitFor();
    }
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

  //TODO: will need to start each one in a diff thread!
  @Override
  public void run() {
    for (ZivaTask zivaTask : zivaTasks) {
      this.threadFactory.newThread(zivaTask).start();
    }
  }
}
