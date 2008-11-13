package com.google.zigva.exec;

import com.google.common.collect.Lists;

import java.util.List;

class CompoundZivaTask implements ZivaTask {

  private final List<ZivaTask> zivaTasks;

  public CompoundZivaTask(ZivaTask... zivaTasks) {
    this.zivaTasks = Lists.newArrayList(zivaTasks);
  }
  
  public CompoundZivaTask(List<ZivaTask> zivaTasks) {
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
      zivaTask.run();
    }
  }
}
