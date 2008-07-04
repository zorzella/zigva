package com.google.zigva.sh;

import com.google.zigva.sh.JavaProcessExecutor;

public class StubJavaProcessExecutor implements JavaProcessExecutor {

  public Process result = new StubProcess();
  
  @Override
  public Process start(ProcessBuilder processBuilder) {
    return result;
  }
}
