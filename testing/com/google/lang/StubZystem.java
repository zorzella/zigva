package com.google.lang;

import com.google.inject.Provider;
import com.google.zigva.exec.Executor;
import com.google.zigva.io.FilePath;
import com.google.zigva.io.Sink;
import com.google.zigva.io.Source;
import com.google.zigva.java.Propertiez;
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
  public Executor executor() {
    return new Executor(this);
  }

  @Override
  public Map<String, String> env() {
    return null;
  }

  @Override
  public Provider<Source<Character>> in() {
    return null;
  }

  @Override
  public Provider<Sink<Character>> out() {
    return null;
  }

  @Override
  public Provider<Sink<Character>> err() {
    return null;
  }

  @Override
  public ThreadFactory getThreadFactory() {
    return null;
  }
}
