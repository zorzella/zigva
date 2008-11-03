package com.google.zigva.java;

import com.google.inject.Provider;
import com.google.zigva.exec.Executor;
import com.google.zigva.io.FilePath;
import com.google.zigva.io.Sink;
import com.google.zigva.io.Source;
import com.google.zigva.lang.Zystem;
import com.google.zigva.sh.ShellCommand;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

public class RealZystem implements Zystem {

  private final Provider<Source<Character>> inProvider;
  private final Provider<Sink<Character>> outProvider;
  private final Provider<Sink<Character>> errProvider;
  private FilePath currentDir;
  private final FilePath homeDir;
  private final Map<String, String> env;
  private final ThreadFactory threadFactory;
  
  public RealZystem(
      Provider<Source<Character>> inProvider,
      Provider<Sink<Character>> outProvider,
      Provider<Sink<Character>> errProvider,
      FilePath currentDir,
      FilePath homeDir,
      Map<String, String> env, 
      ThreadFactory threadFactory) {
    this.inProvider = inProvider;
    this.outProvider = outProvider;
    this.errProvider = errProvider;
    this.currentDir = currentDir;
    this.homeDir = homeDir;
    this.env = env;
    this.threadFactory = threadFactory;
  }
  
  @Override
  public String toString() {
    return String.format("[%s]", currentDir);
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
  public Provider<Source<Character>> in() {
    return inProvider;
  }

  @Override
  public Provider<Sink<Character>> out() {
    return outProvider;
  }

  @Override
  public Provider<Sink<Character>> err() {
    return errProvider;
  }

  @Override
  public ThreadFactory getThreadFactory() {
    return threadFactory;
  }
}
