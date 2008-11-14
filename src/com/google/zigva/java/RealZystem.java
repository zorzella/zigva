package com.google.zigva.java;

import com.google.zigva.io.FilePath;
import com.google.zigva.lang.IoFactory;
import com.google.zigva.lang.Zystem;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

public class RealZystem implements Zystem {

  private final IoFactory ioFactory;
  private final FilePath currentDir;
  private final FilePath homeDir;
  private final Map<String, String> env;
  private final ThreadFactory threadFactory;
  
  public RealZystem(
      IoFactory ioFactory,
      FilePath currentDir,
      FilePath homeDir,
      Map<String, String> env, 
      ThreadFactory threadFactory) {
    this.ioFactory = ioFactory;
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
  public Map<String, String> env() {
    return env;
  }

  @Override
  public ThreadFactory getThreadFactory() {
    return threadFactory;
  }

  @Override
  public IoFactory ioFactory() {
    return ioFactory;
  }
}
