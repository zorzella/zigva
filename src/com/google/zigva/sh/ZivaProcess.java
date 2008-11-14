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

import com.google.zigva.exec.ZigvaTask;
import com.google.zigva.lang.ZigvaInterruptedException;

import java.io.IOException;

public class ZivaProcess implements ZigvaTask {

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
      throw new FailedToKillException(e);
    }
    this.process.destroy();
//    out.interrupt();
//    err.interrupt();
  }

  @Override
  public String getName() {
    return "ZivaProcess";
  }

  /**
   * Does nothing: the process has been started outside this class
   */
  //TODO: consider starting the process inside this method -- not sure it's very
  //viable, given we need to start the process to get the stdin/out/err
  @Override
  public void run() {
  }
  
}
