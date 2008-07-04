package com.google.zigva.sh;

import java.io.IOException;

public class RealJavaProcessExecutor implements JavaProcessExecutor {

  @Override
  public Process start(ProcessBuilder processBuilder) {
    try {
      return processBuilder.start();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
