package com.google.zigva.java;

import com.google.zigva.io.Executor;
import com.google.zigva.io.FilePath;
import com.google.zigva.io.Readers;
import com.google.zigva.io.Source;
import com.google.zigva.io.Writers;
import com.google.zigva.io.Zystem;

import java.io.Reader;
import java.net.UnknownHostException;
import java.util.Map;

public class RealZystem implements Zystem {

  private final Source inAsSource;
  private final Reader in;
  private final Appendable out;
  private final Appendable err;
  private FilePath currentDir;
  private final FilePath homeDir;
  private final Map<String, String> env;
  
  public RealZystem(
      Source inAsSource,
      Reader in,
      Appendable out,
      Appendable err,
      FilePath currentDir, FilePath homeDir, Map<String, String> env) {
    this.inAsSource = inAsSource;
    this.in = in;
    this.out = out;
    this.err = err;
    this.currentDir = currentDir;
    this.homeDir = homeDir;
    this.env = env;
  }
  
  @Override
  public String getHostname() {
    String temp = null;
    try {
      return java.net.InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      return System.getenv("HOSTNAME");
    }
  }

  @Override
  public FilePath getCurrentDir() {
    return currentDir;
  }

  @Override
  public FilePath getHomeDir() {
    return homeDir;
  }

  @Override
  public Propertiez properties() {
    return new RealPropertiez();
  }

  @Override
  public Executor executor() {
    return new Executor(this);
  }

  @Override
  public Map<String, String> env() {
    return env;
  }

  @Override
  public Appendable err() {
    return err;
  }

  @Override
  public Appendable out() {
    return out;
  }

  @Override
  public Reader in() {
    return in;
  }

  @Override
  public Source inAsSource() {
    return inAsSource;
  }

//  @Override
//  public Appendable getAppendable() {
//    return Writers.buffered(out());
//  }
//
//  @Override
//  public Reader getReader() {
//    return Readers.buffered(in());
//  }
}
