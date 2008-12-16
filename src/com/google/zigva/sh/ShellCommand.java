/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zigva.sh;

import com.google.common.base.Join;
import com.google.inject.Inject;
import com.google.zigva.exec.CommandExecutor;
import com.google.zigva.exec.ZigvaTask;
import com.google.zigva.exec.CommandExecutor.Command;
import com.google.zigva.guice.ZigvaThreadFactory;
import com.google.zigva.java.io.ReaderSource;
import com.google.zigva.java.io.Readers;
import com.google.zigva.lang.Zystem;

import java.io.IOException;
import java.util.Map;

public class ShellCommand implements Command {
  
  private final String[] shellCommand;
  private final ActivePipe.Builder activePipeBuilder;

  public static class Builder {
    
    private final ActivePipe.Builder activePipeBuilder;

    @Inject
    public Builder (ActivePipe.Builder activePipeBuilder) {
      this.activePipeBuilder = activePipeBuilder;
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
  public ZigvaTask buildTask(CommandExecutor.Builder cmdExecutorBuilder, Zystem zystem) {
    return new JavaProcessZivaTask(zystem, shellCommand, activePipeBuilder);
  }
  
  private static final class JavaProcessZivaTask implements ZigvaTask {

    private final Zystem zystem;
    private final String[] shellCommand;
    private final ActivePipe.Builder activePipeBuilder;

    private JavaProcess process;
    
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
      process.kill();
    }

    @Override
    public String getName() {
      return "JavaProcess";
    }

    @Override
    public void run() {
      process = startProcess(zystem, shellCommand);
      process.waitFor();
      if (process.exitValue() != 0) {
        throw new RuntimeException(String.format(
            "Process '%s' exited with status '%d'.", 
            getName(), process.exitValue()));
      }
    }

  private JavaProcess startProcess(Zystem zystem, String... shellCommand) {
    return startProcess(zystem, buildProcessBuilder(zystem, shellCommand));
  }

  private static ProcessBuilder buildProcessBuilder(Zystem zystem, String... shellCommand) {
    ProcessBuilder processBuilder = new ProcessBuilder();
    
    // Stupid ProcessBuilder needs us to get its default env and monkey with it
    Map<String, String> environment = processBuilder.environment();
    environment.clear();
    environment.putAll(zystem.env());
    
    processBuilder.directory(zystem.getCurrentDir().toFile());
    processBuilder.command(shellCommand);
    if (zystem.ioFactory().redirectErrToOut()) {
      processBuilder.redirectErrorStream(true);
    }
    return processBuilder;
  }

  private JavaProcess startProcess(Zystem zystem, ProcessBuilder processBuilder) {
    try {

      Process process = processBuilder.start();

      ReaderSource outSource = 
        new ReaderSource.Builder(new ZigvaThreadFactory())
        .create(Readers.buffered(process.getInputStream()));
      Thread outS = activePipeBuilder.comboCreate("ShellCommand - out", 
          outSource, 
          zystem.ioFactory().out().buildOut(
              outSource))
            .start();
      Thread errS;
      if (!processBuilder.redirectErrorStream()) {
        ReaderSource errSource = new ReaderSource.Builder(new ZigvaThreadFactory())
          .create(Readers.buffered(process.getErrorStream()));
        errS = activePipeBuilder.comboCreate("ShellCommand - err", 
            errSource, 
            zystem.ioFactory().err().buildErr(
                errSource
                ))
              .start();
      } else {
        errS = null;
      }
      Thread inS = activePipeBuilder.comboCreate("ShellCommand - in", 
          zystem.ioFactory().in().buildIn(), process.getOutputStream()).start();
      JavaProcess temp = new JavaProcess(process, inS, outS, errS);
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

  private static class JavaProcess extends ZivaProcess {
    public JavaProcess(Process process, Thread in, Thread out, Thread err) {
      super(process, in, out, err);
    }
  }
}