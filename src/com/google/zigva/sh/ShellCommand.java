// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.zigva.sh;

import com.google.common.base.Join;
import com.google.zigva.exec.Executor;
import com.google.zigva.exec.ZivaTask;
import com.google.zigva.exec.Executor.Command;
import com.google.zigva.lang.Zystem;


import java.io.IOException;
import java.util.Map;

public class ShellCommand implements Command {
  private final String[] shellCommand;

  public ShellCommand(String... shellCommand) {
    this.shellCommand = shellCommand;
  }

  @Override
  public ZivaTask execute(Zystem zystem) {
    return buildZivaTask(zystem, shellCommand);
  }
  private static ZivaTask buildZivaTask(Zystem zystem, String... shellCommand) {
    return buildZivaTask(zystem, buildProcessBuilder(zystem, shellCommand));
  }

  private static ProcessBuilder buildProcessBuilder(Zystem zystem, String... shellCommand) {
    ProcessBuilder processBuilder = new ProcessBuilder();
    
    // Stupid ProcessBuilder needs us to get its default env and monkey with it
    Map<String, String> environment = processBuilder.environment();
    environment.clear();
    environment.putAll(zystem.env());
    
    processBuilder.directory(zystem.getCurrentDir().toFile());
    processBuilder.command(shellCommand);
    if (zystem.out() == zystem.err()) {
      processBuilder.redirectErrorStream(true);
    }
    return processBuilder;
  }

  private static ZivaTask buildZivaTask(Zystem zystem, ProcessBuilder processBuilder) {
    try {

      Process process = processBuilder.start();

      Thread outS = new ActivePipe("ShellCommand - out", process.getInputStream(), zystem.out()).start();
      Thread errS;
      if (!processBuilder.redirectErrorStream()) {
        errS = new ActivePipe("ShellCommand - err", process.getErrorStream(), zystem.err()).start();
      } else {
        errS = null;
      }
      Thread inS = new ActivePipe("ShellCommand - in", zystem.in().get(), process.getOutputStream()).start();
      ZivaProcess temp = new ZivaProcess(process, inS, outS, errS);
      return temp;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public String toString() {
    return Join.join(" ", this.shellCommand);
  }
}