package com.google.zigva.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class LazyCreateFileAppendable implements Appendable {

  private final String name;
  private boolean append = false;
  private Appendable appendable;
  private Appendable fallback;

  public LazyCreateFileAppendable(String name) {
    this.name = name;
  }

  public LazyCreateFileAppendable(File file) {
    this.name = file.getAbsolutePath();
  }

  public LazyCreateFileAppendable(String name, boolean append) {
    this (name);
    this.append = append;
  }

  public LazyCreateFileAppendable(Appendable fallback, String name, 
      boolean append) {
    this (name, append);
    this.fallback = fallback;
  }

  private synchronized void initializeFileAppendable() 
      throws FileNotFoundException {
    if (appendable == null) {
      try {
        appendable = new PrintStream (new FileOutputStream(this.name, this.append));
      } catch (FileNotFoundException e) {
        if (this.fallback == null) {
          throw e;
        }
        appendable = this.fallback;
      }
    }
  }

  public Appendable append(CharSequence csq) throws IOException {
    initializeFileAppendable();
    return appendable.append(csq);
  }

  public Appendable append(char c) throws IOException {
    initializeFileAppendable();
    return appendable.append(c);
  }

  public Appendable append(CharSequence csq, int start, int end) throws IOException {
    initializeFileAppendable();
    return appendable.append(csq, start, end);
  }
  
}
