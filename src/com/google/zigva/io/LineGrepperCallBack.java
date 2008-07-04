package com.google.zigva.io;

public interface LineGrepperCallBack {

  /*
   * Returns true if {@code lineToTest} matched
   */
  public boolean doWork(String lineToTest);

}
