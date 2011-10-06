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

import com.google.common.base.Joiner;
import com.google.zigva.exec.CommandExecutor.Command;
import com.google.zigva.io.PumpToSink;
import com.google.zigva.io.SinkToOutputStream;
import com.google.zigva.io.Source;
import com.google.zigva.java.JavaProcessStarter;
import com.google.zigva.java.io.Readers;
import com.google.zigva.java.io.SourceOfCharFromReader;
import com.google.zigva.lang.CommandResponse;
import com.google.zigva.lang.Killable;
import com.google.zigva.lang.NaiveWaitable;
import com.google.zigva.lang.Waitable;
import com.google.zigva.lang.Waitables;
import com.google.zigva.lang.ZigvaInterruptedException;
import com.google.zigva.lang.ZigvaThreadFactory;
import com.google.zigva.sys.Zystem;

import java.io.IOException;
import java.util.List;
import java.util.Map;

class SystemCommand implements Command {
  
  private final ZigvaThreadFactory zigvaThreadFactory;
  private final SinkToOutputStream.Builder outputStreamPassiveSinkBuilder;
  private final JavaProcessStarter javaProcessStarter;
  private final List<String> command;
  private final Waitables waitables;

  SystemCommand(
      ZigvaThreadFactory zigvaThreadFactory, 
      SinkToOutputStream.Builder outputStreamPassiveSinkBuilder,
      JavaProcessStarter javaProcessStarter,
      List<String> command, 
      Waitables waitables) {
    this.command = command;
    this.zigvaThreadFactory = zigvaThreadFactory;
    this.outputStreamPassiveSinkBuilder = outputStreamPassiveSinkBuilder;
    this.javaProcessStarter = javaProcessStarter;
    this.waitables = waitables;
  }

  //TODO: do away with this class
  private static final class Helper {

    private final Zystem zystem;
    private final List<String> command;
    private final JavaProcessStarter javaProcessStarter;

    private JavaProcess process;
    
    public Helper(
        Zystem zystem, 
        List<String> command,
        ZigvaThreadFactory zigvaThreadFactory,
        SinkToOutputStream.Builder outputStreamPassiveSinkBuilder, 
        JavaProcessStarter javaProcessStarter) {
      this.zystem = zystem;
      this.command = command;
      this.javaProcessStarter = javaProcessStarter;
    }

    public void kill() {
      process.kill();
    }

    public String getName() {
      return "JavaProcess";
    }

    private Process createAndStartJavaProcess() {
      ProcessBuilder processBuilder = new ProcessBuilder();
      
      // Stupid ProcessBuilder needs us to get its default env and monkey with it
      Map<String, String> environment = processBuilder.environment();
      environment.clear();
      environment.putAll(zystem.env());
      
      processBuilder.directory(zystem.getCurrentDir().toFile());
      processBuilder.command(command);
      processBuilder.redirectErrorStream(false);
      Process process = javaProcessStarter.start(processBuilder);
      return process;
    }
  }
  
  @Override
  public String toString() {
    return Joiner.on(" ").join(this.command);
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

  @Override
  public CommandResponse go(Zystem zystem, Source<Character> in) {
    Helper helper = 
      new Helper(
        zystem, 
        command, 
        zigvaThreadFactory, 
        outputStreamPassiveSinkBuilder, 
        javaProcessStarter);
    final Process process = helper.createAndStartJavaProcess();
 
    Source<Character> outSource = 
      new SourceOfCharFromReader(new ZigvaThreadFactory())
        .create(Readers.buffered(process.getInputStream()));

    Source<Character> errSource = 
      new SourceOfCharFromReader(new ZigvaThreadFactory())
    .create(Readers.buffered(process.getErrorStream()));
    
    SinkToOutputStream stdInPassiveSink = 
      outputStreamPassiveSinkBuilder.create(process.getOutputStream());

    zigvaThreadFactory.newDaemonThread(
      new PumpToSink<Character>(in, stdInPassiveSink))
        .ztart();
    
    final Waitable waitable = waitables.from(new NaiveWaitable() {
      @Override
      public void waitFor() {
        try {
          int exitValue = process.waitFor();
          if (exitValue != 0) {
            throw new Waitable.CommandFailedException(String.format(
                "Process exited with status '%d'.", 
                process.exitValue()));
          }
        } catch (InterruptedException e) {
          throw new ZigvaInterruptedException(e);
        }
      }
      
      @Override
      public String toString() {
        return String.format(
            "Waitable for running system process: [%s]", SystemCommand.this.toString());
      }
    });
    
    return CommandResponse.forOutErr(this, outSource, errSource, waitable);
  }
}