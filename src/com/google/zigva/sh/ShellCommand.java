// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.zigva.sh;

import com.google.common.base.Join;
import com.google.inject.Inject;
import com.google.zigva.exec.ZivaTask;
import com.google.zigva.exec.Executor.Command;
import com.google.zigva.lang.Zystem;
import com.google.zigva.sh.ActivePipe.Builder;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

public class ShellCommand implements Command {
  private final String[] shellCommand;
  private final ActivePipe.Builder activePipeBuilder;

  public static class Builder {
    
    private final ActivePipe.Builder activePipeBuilder;

    @Inject
    public Builder (ThreadFactory threadFactory) {
      this.activePipeBuilder = new ActivePipe.Builder(threadFactory);
    }
    
    public ShellCommand build(String... shellCommand) {
      return new ShellCommand(activePipeBuilder, shellCommand);
    }
    
  }
  
  private ShellCommand(ActivePipe.Builder activePipeBuilder, String... shellCommand) {
    this.activePipeBuilder = activePipeBuilder;
    this.shellCommand = shellCommand;
  }

  @Override
  public ZivaTask execute(Zystem zystem) {
    return buildZivaTask(zystem, shellCommand);
  }
  
  private ZivaTask buildZivaTask(Zystem zystem, String... shellCommand) {
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
    //TODO: implement a reasonable "equals" method here
    if (zystem.out().get().equals(zystem.err().get())) {
      processBuilder.redirectErrorStream(true);
    }
    return processBuilder;
  }

  private ZivaTask buildZivaTask(Zystem zystem, ProcessBuilder processBuilder) {
    try {

      Process process = processBuilder.start();

      Thread outS = new ActivePipe("ShellCommand - out", 
          process.getInputStream(), 
          zystem.out().get())
            .start();
      Thread errS;
      if (!processBuilder.redirectErrorStream()) {
        errS = new ActivePipe("ShellCommand - err", 
            process.getErrorStream(), 
            zystem.err().get())
              .start();
      } else {
        errS = null;
      }
      Thread inS = activePipeBuilder.comboCreate("ShellCommand - in", 
          zystem.in().get(), process.getOutputStream()).start();
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