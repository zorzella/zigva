package com.google.zigva.sh;

import com.google.zigva.exec.ZivaTask;

import java.io.IOException;

public class ZivaProcess implements ZivaTask {

  private final Process process;
  private final Thread in;
  private final Thread out;
  private final Thread err;

  public ZivaProcess(Process process, Thread in, Thread out, Thread err) {
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
      throw new RuntimeException(e);
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
      throw new RuntimeException(e);
    }
    this.process.destroy();
//    out.interrupt();
//    err.interrupt();
  }
  
}
