// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.zigva.sh;

import com.google.common.base.Join;
import com.google.inject.Inject;
import com.google.zigva.exec.ZivaTask;
import com.google.zigva.exec.CommandExecutor.Command;
import com.google.zigva.lang.Zystem;

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
    return new JavaProcessZivaTask(zystem, shellCommand, activePipeBuilder);
  }
  
  private static final class JavaProcessZivaTask implements ZivaTask {

    private final Zystem zystem;
    private final String[] shellCommand;
    private final ActivePipe.Builder activePipeBuilder;

    public JavaProcessZivaTask(
        Zystem zystem, 
        String[] shellCommand,
        ActivePipe.Builder activePipeBuilder) {
      this.zystem = zystem;
      this.shellCommand = shellCommand;
      this.activePipeBuilder = activePipeBuilder;
    }

    @Override
    public void kill() {
    }

    @Override
    public String getName() {
      return null;
    }

    @Override
    public void run() {
      buildZivaTask(zystem, shellCommand).waitFor();
    }

  private ZivaProcess buildZivaTask(Zystem zystem, String... shellCommand) {
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
    if (zystem.ioFactory().buildOut().equals(zystem.ioFactory().buildErr())) {
      processBuilder.redirectErrorStream(true);
    }
    return processBuilder;
  }

  private ZivaProcess buildZivaTask(Zystem zystem, ProcessBuilder processBuilder) {
    try {

      Process process = processBuilder.start();

      Thread outS = activePipeBuilder.comboCreate("ShellCommand - out", 
          process.getInputStream(), 
          zystem.ioFactory().buildOut())
            .start();
      Thread errS;
      if (!processBuilder.redirectErrorStream()) {
        errS = activePipeBuilder.comboCreate("ShellCommand - err", 
            process.getErrorStream(), 
            zystem.ioFactory().buildErr())
              .start();
      } else {
        errS = null;
      }
      Thread inS = activePipeBuilder.comboCreate("ShellCommand - in", 
          zystem.ioFactory().buildIn(), process.getOutputStream()).start();
      ZivaProcess temp = new ZivaProcess(process, inS, outS, errS);
      return temp;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  }
  
  @Override
  public String toString() {
    return Join.join(" ", this.shellCommand);
  }
}