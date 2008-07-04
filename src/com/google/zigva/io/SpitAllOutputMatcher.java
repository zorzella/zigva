package com.google.zigva.io;

import java.io.IOException;



public class SpitAllOutputMatcher implements LineGrepperCallBack {

  private String prefix;
  private Appendable out;

  public SpitAllOutputMatcher(Appendable out, String prefix) {
    this.out = out;
    this.prefix = prefix + ": ";
  }
  
  public boolean doWork(String lineToTest) {
    try {
      out.append(prefix).append(lineToTest).append("\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }
}
