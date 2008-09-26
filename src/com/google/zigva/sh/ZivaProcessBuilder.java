package com.google.zigva.sh;

import com.google.common.base.Join;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.zigva.io.FilePath;
import com.google.zigva.io.FileRepository;
import com.google.zigva.io.Zystem;
import com.google.zigva.java.io.ReaderSource;
import com.google.zigva.java.io.Readers;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class ZivaProcessBuilder {

  private final Zystem zystem;
  private final JavaProcessExecutor javaProcessExecutor;
  private final FileRepository fileRepository;

  @Inject
  public ZivaProcessBuilder(
      Zystem zystem,
      JavaProcessExecutor javaProcessExecutor,
      FileRepository fileRepository) {
    this.zystem = zystem;
    this.javaProcessExecutor = javaProcessExecutor;
    this.fileRepository = fileRepository;
  }

  private InputStream in;
  private Appendable out = null;
  private Appendable err = null;
  private String[] command = null;
  private FilePath workingDir = null;
  
  public ZivaProcessBuilder parseCommand(String cmd) {
    this.command = cmd.split(" ");
    return this;
  }

  public ZivaProcessBuilder commandArray(String... cmd) {
    this.command = cmd;
    return this;
  }

  public ZivaProcessBuilder commandArray(Collection<String> cmd) {
    this.command = toArray(cmd);
    return this;
  }
  
  public String[] getCommand() {
    return command;
  }

  public String getCommandString() {
    return Join.join(" ", command);
  }
  
  public ZivaProcessBuilder workingDir(File workingDir) {
    this.workingDir = fileRepository.get(workingDir);
    return this;
  }

  public FilePath getWorkingDir() {
    return workingDir;
  }
  
  public ZivaProcessBuilder workingDir(FilePath workingDir) {
    this.workingDir = workingDir;
    return this;
  }

  public ZivaProcessBuilder setIn(InputStream in) {
    this.in = in;
    return this;
  }
  
  public ZivaProcessBuilder setOut(Appendable out) {
    this.out = out;
    return this;
  }
  
  public ZivaProcessBuilder setErr(Appendable err) {
    this.err = err;
    return this;
  }

  public ZivaProcess run() {
    Appendable localOut = out;
    if (localOut == null) {
      out = System.out;
    }
    
    Appendable localErr = err;
    if ((localErr == null) && (redirectStdErrToStdOut == false)) {
      localErr = System.err;
    }
    
    try {
      Process process = javaProcessExecutor.start(getProcessBuilder());
      Thread outS = new ActivePipe("ZivaProcessBuilder - out", 
          process.getInputStream(), localOut).start();
      Thread errS = new ActivePipe("ZivaProcessBuilder - err", 
          process.getErrorStream(), localErr).start();
      if (in == null) {
        process.getOutputStream().close();
      } else {
        Thread inS = new ActivePipe(
            "ZivaProcessBuilder - in", 
            new ReaderSource(Readers.buffered(in)), 
            process.getOutputStream()).start();
      }
      return new ZivaProcess(process, outS, errS);
    } catch (IOException e) {
      // TODO 
      throw new RuntimeException(e);
    }
  }

  private ProcessBuilder processBuilder = null;
  private boolean redirectStdErrToStdOut;
  
  public ZivaProcessBuilder setRedirectStdErrToStdOut(boolean redirectStdErrToStdOut) {
    this.redirectStdErrToStdOut = redirectStdErrToStdOut;
    return this;
  }
  
  private ProcessBuilder getProcessBuilder() {
    if (processBuilder == null) {
      if (command == null) {
        throw new IllegalArgumentException(
        "Call 'cmd' (or 'processBuilder') to set the command to execute before trying to run a ZivaProcess");
      }
      ProcessBuilder result = new ProcessBuilder(command);
      if (workingDir == null) {
        result.directory(zystem.getCurrentDir().toFile());
      } else {
        result.directory(workingDir.toFile());
      }
      if (redirectStdErrToStdOut) {
        Preconditions.checkState(err == null, "Only one of 'redirectStdErrToStdOut or 'stderr' can be used.");
        result.redirectErrorStream(true);
      }
      return result;
    } else {
      Preconditions.checkState(command == null, "Only one of 'processBuilder' or 'cmd' can be used");
      Preconditions.checkState(workingDir == null, "Only one of 'processBuilder' or 'workingDir' can be used");
      return processBuilder;
    }
  }

  private static String[] toArray(Collection<String> cmdArray) {
    String[] result = new String[cmdArray.size()];
    cmdArray.toArray(result);
    return result;
  }

}
