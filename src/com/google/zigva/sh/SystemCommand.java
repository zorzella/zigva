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
import com.google.zigva.exec.CommandExecutor;
import com.google.zigva.exec.Killable;
import com.google.zigva.exec.ZigvaTask;
import com.google.zigva.exec.CommandExecutor.Command;
import com.google.zigva.guice.ZigvaThreadFactory;
import com.google.zigva.io.OutputStreamPassiveSink;
import com.google.zigva.io.SimpleSink;
import com.google.zigva.java.JavaProcessStarter;
import com.google.zigva.java.io.ReaderSource;
import com.google.zigva.java.io.Readers;
import com.google.zigva.lang.IoFactory;
import com.google.zigva.lang.ZThread;
import com.google.zigva.lang.ZigvaInterruptedException;
import com.google.zigva.lang.Zystem;

import java.io.IOException;
import java.util.Map;

class SystemCommand implements Command {
  
  private final ZigvaThreadFactory zigvaThreadFactory;
  private final OutputStreamPassiveSink.Builder outputStreamPassiveSinkBuilder;
  private final JavaProcessStarter javaProcessStarter;
  private final String[] command;

  SystemCommand(
      ZigvaThreadFactory zigvaThreadFactory, 
      OutputStreamPassiveSink.Builder outputStreamPassiveSinkBuilder,
      JavaProcessStarter javaProcessStarter,
      String... command) {
    this.command = command;
    this.zigvaThreadFactory = zigvaThreadFactory;
    this.outputStreamPassiveSinkBuilder = outputStreamPassiveSinkBuilder;
    this.javaProcessStarter = javaProcessStarter;
  }

  @Override
  public ZigvaTask buildTask(
      CommandExecutor.Builder cmdExecutorBuilder, 
      Zystem zystem) {
    return new JavaProcessZivaTask(
        zystem, 
        command, 
        zigvaThreadFactory, 
        outputStreamPassiveSinkBuilder, 
        javaProcessStarter);
  }
  
  private static final class JavaProcessZivaTask implements ZigvaTask {

    private final Zystem zystem;
    private final String[] command;
    private final ZigvaThreadFactory zigvaThreadFactory;
    private final OutputStreamPassiveSink.Builder outputStreamPassiveSinkBuilder;
    private final IoFactory ioFactory;
    private final JavaProcessStarter javaProcessStarter;

    private JavaProcess process;
    
    public JavaProcessZivaTask(
        Zystem zystem, 
        String[] shellCommand,
        ZigvaThreadFactory zigvaThreadFactory,
        OutputStreamPassiveSink.Builder outputStreamPassiveSinkBuilder, JavaProcessStarter javaProcessStarter) {
      this.zystem = zystem;
      this.command = shellCommand;
      this.zigvaThreadFactory = zigvaThreadFactory;
      this.outputStreamPassiveSinkBuilder = outputStreamPassiveSinkBuilder;
      this.ioFactory = zystem.ioFactory();
      this.javaProcessStarter = javaProcessStarter;
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
      process = startProcess();
      process.waitFor();
      if (process.exitValue() != 0) {
        throw new RuntimeException(String.format(
            "Process '%s' exited with status '%d'.", 
            getName(), process.exitValue()));
      }
    }

    private ProcessBuilder createJavaProcessBuilder() {
      ProcessBuilder processBuilder = new ProcessBuilder();
      
      // Stupid ProcessBuilder needs us to get its default env and monkey with it
      Map<String, String> environment = processBuilder.environment();
      environment.clear();
      environment.putAll(zystem.env());
      
      processBuilder.directory(zystem.getCurrentDir().toFile());
      processBuilder.command(command);
      if (ioFactory.redirectErrToOut()) {
        processBuilder.redirectErrorStream(true);
      }
      return processBuilder;
    }
  
    private JavaProcess startProcess() throws RuntimeException {
      ProcessBuilder processBuilder = createJavaProcessBuilder();
      
      Process process = javaProcessStarter.start(processBuilder);
  
      ReaderSource outSource = 
        new ReaderSource.Builder(new ZigvaThreadFactory())
      .create(Readers.buffered(process.getInputStream()));

      Thread processStdOutThread = 
        zigvaThreadFactory.newDaemonThread(
            ioFactory.out().build(outSource))
              .ztart();

      Thread processStdErrThread = null;
      if (!ioFactory.redirectErrToOut()) {
        ReaderSource errSource = 
          new ReaderSource.Builder(new ZigvaThreadFactory())
            .create(Readers.buffered(process.getErrorStream()));
        processStdErrThread = 
          zigvaThreadFactory.newDaemonThread(
              ioFactory.err().build(errSource))
              .ztart();
      }

      OutputStreamPassiveSink stdInPassiveSink = 
        outputStreamPassiveSinkBuilder.create(process.getOutputStream());

      ZThread processStdInThread = 
        zigvaThreadFactory.newDaemonThread(
          new SimpleSink<Character>(ioFactory.in().build(), stdInPassiveSink))
            .ztart();

      JavaProcess result = new JavaProcess(
          process, 
          processStdInThread, 
          processStdOutThread, 
          processStdErrThread);
      return result;
    }
  }
  
  @Override
  public String toString() {
    return Join.join(" ", this.command);
  }

  private static class JavaProcess {

    private final Process process;
    private final Thread in;
    private final Thread out;
    private final Thread err;

    public JavaProcess(Process process, Thread in, Thread out, Thread err) {
      this.process = process;
      this.in = in;
      this.out = out;
      this.err = err;
    }
      
    public void waitFor() {
      try {
        process.waitFor();
        out.join();
        if (err != null) {
          err.join();
        }
        if (in != null) {
          if (in.getState() != Thread.State.TERMINATED) {
            in.interrupt();
          }
          in.join();
        }
      } catch (InterruptedException e) {
        throw new ZigvaInterruptedException(e);
      }
    }

    public int exitValue(){
      return this.process.exitValue();
    }
    
    public Process process() {
      return this.process;
    }

    //TODO: Close input stream also?
    public void kill() {
      try {
        //TODO: why do we need to do this?
        this.process.getErrorStream().close();
        this.process.getOutputStream().close();
      } catch (IOException e) {
        throw new Killable.FailedToKillException(e);
      }
      this.process.destroy();
//      out.interrupt();
//      err.interrupt();
    }
  }
}