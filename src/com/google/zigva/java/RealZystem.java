package com.google.zigva.java;

import com.google.inject.Provider;
import com.google.zigva.io.Executor;
import com.google.zigva.io.FilePath;
import com.google.zigva.io.Source;
import com.google.zigva.io.Zystem;

import java.io.Reader;
import java.net.UnknownHostException;
import java.util.Map;

public class RealZystem implements Zystem {

  private final Provider<Source<Character>> in;
  private final Appendable out;
  private final Appendable err;
  private FilePath currentDir;
  private final FilePath homeDir;
  private final Map<String, String> env;
  
  public RealZystem(
      Provider<Source<Character>> in,
      Appendable out,
      Appendable err,
      FilePath currentDir, 
      FilePath homeDir, 
      Map<String, String> env) {
    this.in = in;
    this.out = out;
    this.err = err;
    this.currentDir = currentDir;
    this.homeDir = homeDir;
    this.env = env;
  }
  
  public RealZystem(
      Source<Character> inAsSource,
      Reader in,
      Appendable out,
      Appendable err,
      FilePath currentDir, 
      FilePath homeDir, 
      Map<String, String> env) {
    this.in = getProvider(inAsSource);
    this.out = out;
    this.err = err;
    this.currentDir = currentDir;
    this.homeDir = homeDir;
    this.env = env;
  }

  private static Provider<Source<Character>> getProvider(final Source<Character> inAsSource2) {
    return new Provider<Source<Character>> () {
      @Override
      public Source<Character> get() {
        return inAsSource2;
      }
    };
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
  public Provider<Source<Character>> in() {
    return in;
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
