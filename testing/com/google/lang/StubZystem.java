package com.google.lang;

import com.google.zigva.exec.CommandExecutor;
import com.google.zigva.io.FilePath;
import com.google.zigva.java.Propertiez;
import com.google.zigva.lang.IoFactory;
import com.google.zigva.lang.Zystem;

import java.util.Map;
import java.util.concurrent.ThreadFactory;

public class StubZystem implements Zystem {

  @Override
  public FilePath getCurrentDir() {
    return null;
  }

  @Override
  public String getHostname() {
    return null;
  }

  @Override
  public FilePath getHomeDir() {
    return null;
  }

  @Override
  public Propertiez properties() {
    return null;
  }

  @Override
  public CommandExecutor cmdExecutor() {
    return new CommandExecutor(this);
  }

  @Override
  public Map<String, String> env() {
    return null;
  }

  @Override
  public ThreadFactory getThreadFactory() {
    return null;
  }

  @Override
  public IoFactory ioFactory() {
    return null;
  }
}
