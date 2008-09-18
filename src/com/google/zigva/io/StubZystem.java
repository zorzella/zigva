package com.google.zigva.io;

import com.google.inject.Provider;
import com.google.zigva.java.Propertiez;

import java.io.Reader;
import java.util.Map;


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
  public Appendable err() {
    return null;
  }

  @Override
  public Appendable out() {
    return null;
  }

  @Override
  public Reader in() {
    return null;
  }

  @Override
  public Source inAsSource() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Provider<Source> inProvider() {
    return null;
  }

//  @Override
//  public Appendable getAppendable() {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public Reader getReader() {
//    // TODO Auto-generated method stub
//    return null;
//  }
}
