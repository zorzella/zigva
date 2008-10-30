package com.google.zigva.java;

import com.google.inject.Provider;
import com.google.zigva.exec.Executor;
import com.google.zigva.io.FilePath;
import com.google.zigva.io.Sink;
import com.google.zigva.io.Source;
import com.google.zigva.lang.Zystem;

import java.net.UnknownHostException;
import java.util.Map;

public class RealZystem implements Zystem {

  private final Provider<Source<Character>> inProvider;
  private final Appendable out;
  private final Appendable err;
  private FilePath currentDir;
  private final FilePath homeDir;
  private final Map<String, String> env;
  private final Provider<Sink<Character>> outAsSinkProvider;
  private final Provider<Sink<Character>> errAsSinkProvider;
  
  public RealZystem(
      Provider<Source<Character>> inProvider,
      Provider<Sink<Character>> outAsSinkProvider,
      Provider<Sink<Character>> errAsSinkProvider,
      Appendable out,
      Appendable err,
      FilePath currentDir, 
      FilePath homeDir, 
      Map<String, String> env) {
    this.inProvider = inProvider;
    this.out = out;
    this.outAsSinkProvider = outAsSinkProvider;
    this.errAsSinkProvider = errAsSinkProvider;
    this.err = err;
    this.currentDir = currentDir;
    this.homeDir = homeDir;
    this.env = env;
  }
  
//  public RealZystem(
//      Source<Character> inAsSource,
//      Sink<Character> outAsSink,
//      Appendable out,
//      Appendable err,
//      FilePath currentDir, 
//      FilePath homeDir, 
//      Map<String, String> env) {
//    this.inProvider = getProvider(inAsSource);
//    this.out = out;
//    this.outAsSinkProvider = getProvider(outAsSink);
//    this.err = err;
//    this.currentDir = currentDir;
//    this.homeDir = homeDir;
//    this.env = env;
//  }
//
//  private static <T> Provider<T> getProvider(final T inAsSource2) {
//    return new Provider<T> () {
//      @Override
//      public T get() {
//        return inAsSource2;
//      }
//    };
//  }

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
  public Appendable err() {
    return err;
  }

  @Override
  public Appendable out() {
    return out;
  }

  @Override
  public Provider<Source<Character>> in() {
    return inProvider;
  }

  @Override
  public Provider<Sink<Character>> outAsSink() {
    return outAsSinkProvider;
  }

  @Override
  public Provider<Sink<Character>> errAsSink() {
    return errAsSinkProvider;
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
