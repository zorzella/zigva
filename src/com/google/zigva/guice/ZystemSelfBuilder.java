// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.zigva.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.zigva.io.Executor;
import com.google.zigva.io.FilePath;
import com.google.zigva.io.Source;
import com.google.zigva.io.Zystem;
import com.google.zigva.java.Propertiez;
import com.google.zigva.java.RealZystem;

import java.util.Map;

public final class ZystemSelfBuilder implements Zystem {
  
  private final Zystem zystem;

  @Inject
  public ZystemSelfBuilder(Zystem zystem) {
    this.zystem = zystem;
  }

  @Override
  public FilePath getCurrentDir() {
    return zystem.getCurrentDir();
  }

  @Override
  public FilePath getHomeDir() {
    return zystem.getHomeDir();
  }

  @Override
  public String getHostname() {
    return zystem.getHostname();
  }

  @Override
  public Propertiez properties() {
    return zystem.properties();
  }

  @Override
  public Executor executor() {
    return new Executor(this);
  }

  @Override
  public Map<String, String> env() {
    return zystem.env();
  }
  
  public ZystemSelfBuilder withOut(Appendable otherOut) {
    return new ZystemSelfBuilder(
        new RealZystem(
            zystem.in(), 
            otherOut, 
            err(), 
            getCurrentDir(), getHomeDir(), zystem.env()));
  }

  public ZystemSelfBuilder withErr(Appendable otherErr) {
    return new ZystemSelfBuilder(
        new RealZystem(
            zystem.in(), 
            out(), 
            otherErr, 
            getCurrentDir(), getHomeDir(), zystem.env()));
  }

  @Override
  public Appendable err() {
    return zystem.err();
  }

  @Override
  public Appendable out() {
    return zystem.out();
  }

  public ZystemSelfBuilder withEnv(Map<String, String> otherEnv) {
    return new ZystemSelfBuilder(
        new RealZystem(
            zystem.in(), 
            out(), 
            err(), 
            getCurrentDir(), 
            getHomeDir(), otherEnv));
  }

  public ZystemSelfBuilder withIn(Source<Character> otherIn) {
    return new ZystemSelfBuilder(
        new RealZystem(
            otherIn,
            null, 
            out(), 
            err(), 
            getCurrentDir(), 
            getHomeDir(), 
            zystem.env()));
  }

  @Override
  public Provider<Source<Character>> in() {
    return zystem.in();
  }

//  @Override
//  public Appendable getAppendable() {
//    return zystem.out();
//  }
//
//  @Override
//  public Reader getReader() {
//    return zystem.in();
//  }      
}