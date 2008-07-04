package com.google.zigva.io;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LazyCreateFileOutputStream extends OutputStream {

  private String name;
  private boolean append = false;
  private OutputStream outputStream;
  private OutputStream fallback;

  public LazyCreateFileOutputStream(String name) {
    this.name = name;
  }

  public LazyCreateFileOutputStream(String name, boolean append) {
    this (name);
    this.append = append;
  }

  public LazyCreateFileOutputStream(OutputStream fallback, String name, 
      boolean append) {
    this (name, append);
    this.fallback = fallback;
  }

  @Override
  public void write(int b) throws IOException {
    initializeFileOutputStream();
    outputStream.write(b);    
  }

  private synchronized void initializeFileOutputStream() 
      throws FileNotFoundException {
    if (outputStream == null) {
      try {
        outputStream = new FileOutputStream(this.name, this.append);
      } catch (FileNotFoundException e) {
        if (this.fallback == null) {
          throw e;
        }
        outputStream = this.fallback;
      }
    }
  }
  
}
