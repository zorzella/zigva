// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.lang.impl;

import com.google.zigva.io.FilePath;
import com.google.zigva.io.IoFactory;
import com.google.zigva.lang.Propertiez;
import com.google.zigva.lang.UserInfo;
import com.google.zigva.sys.Zystem;

import java.util.Map;

public class DelegatingZystem implements Zystem {

  protected final Zystem delegate;

  public DelegatingZystem(Zystem zystem) {
    this.delegate = zystem;
  }

  @Override
  public Map<String, String> env() {
    return delegate.env();
  }

  @Override
  public FilePath getCurrentDir() {
    return delegate.getCurrentDir();
  }

  @Override
  public FilePath getHomeDir() {
    return delegate.getHomeDir();
  }

  @Override
  public String getHostname() {
    return delegate.getHostname();
  }

  @Override
  public IoFactory ioFactory() {
    return delegate.ioFactory();
  }

  @Override
  public Propertiez properties() {
    return delegate.properties();
  }

  @Override
  public UserInfo userInfo() {
    return delegate.userInfo();
  }
  
  @Override
  public String toString() {
    return delegate.toString();
  }
}
