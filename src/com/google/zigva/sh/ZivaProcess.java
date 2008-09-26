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
    
  @Deprecated
  public ZivaProcess(Process process, Thread out, Thread err) {
    this.process = process;
    this.in = null;
    this.out = out;
    this.err = err;
  }

  public void waitFor() {
    try {
      process.waitFor();
      // TODO: think about this
//      if (in != null) {
//        in.join();
//      } // TODO: This is in the wrong place -- should be done even if "waitFor" is never called! 
      //TODO(zorzella): do not allow for null!
      out.join();
      if (err != null) {
        err.join();
      }
//      if (in != null) {
//        in.interrupt();
//      }
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
  
  public void kill() {
    try {
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
