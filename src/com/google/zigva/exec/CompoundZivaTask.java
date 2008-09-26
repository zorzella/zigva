package com.google.zigva.exec;

import com.google.common.collect.Lists;

import java.util.List;

public class CompoundZivaTask implements ZivaTask {

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
}
