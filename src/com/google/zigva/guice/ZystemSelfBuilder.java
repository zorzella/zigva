// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.zigva.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.zigva.exec.Executor;
import com.google.zigva.io.FilePath;
import com.google.zigva.io.Sink;
import com.google.zigva.io.Source;
import com.google.zigva.java.Propertiez;
import com.google.zigva.java.RealZystem;
import com.google.zigva.lang.Zystem;

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
            zystem.outAsSink(),
            zystem.errAsSink(), 
            otherOut, 
            err(), 
            getCurrentDir(), getHomeDir(), zystem.env()));
  }

  public ZystemSelfBuilder withErr(Appendable otherErr) {
    return new ZystemSelfBuilder(
        new RealZystem(
            zystem.in(),
            zystem.outAsSink(), 
            zystem.errAsSink(), 
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
            zystem.outAsSink(),
            zystem.errAsSink(),
            out(), 
            err(), 
            getCurrentDir(), 
            getHomeDir(), otherEnv));
  }

  //TODO: use Providers
  private static <T> Provider<T> getProvider(final T provided) {
    return new Provider<T> () {
      @Override
      public T get() {
        return provided;
      }
    };
  }

  public ZystemSelfBuilder withIn(Source<Character> otherIn) {
    return new ZystemSelfBuilder(
        new RealZystem(
            getProvider(otherIn),
            zystem.outAsSink(), 
            zystem.errAsSink(), 
            out(), 
            err(), 
            getCurrentDir(), 
            getHomeDir(), 
            zystem.env()));
  }

  public ZystemSelfBuilder withOut(Sink<Character> otherOut) {
    return new ZystemSelfBuilder(
        new RealZystem(
            zystem.in(),
            getProvider(otherOut), 
            zystem.errAsSink(), 
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
  @Override
  public String toString() {
    return zystem.toString();
  }

  @Override
  public Provider<Sink<Character>> outAsSink() {
    return zystem.outAsSink();
  }

  @Override
  public Provider<Sink<Character>> errAsSink() {
    return zystem.errAsSink();
  }
}