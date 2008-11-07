// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.zigva.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.zigva.exec.CommandExecutor;
import com.google.zigva.io.FilePath;
import com.google.zigva.io.Sink;
import com.google.zigva.io.Source;
import com.google.zigva.java.Propertiez;
import com.google.zigva.java.RealZystem;
import com.google.zigva.lang.IoFactory;
import com.google.zigva.lang.Zystem;

import java.util.Map;
import java.util.concurrent.ThreadFactory;

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
  public CommandExecutor cmdExecutor() {
    return new CommandExecutor(this);
  }

  @Override
  public Map<String, String> env() {
    return zystem.env();
  }
  
  public ZystemSelfBuilder withEnv(Map<String, String> otherEnv) {
    return new ZystemSelfBuilder(
        new RealZystem(
            zystem.ioFactory(),
            getCurrentDir(), 
            getHomeDir(), 
            otherEnv,
            zystem.getThreadFactory()));
  }

  //TODO: use Providers
  public static <T> Provider<T> getProvider(final T provided) {
    return new Provider<T> () {
      @Override
      public T get() {
        return provided;
      }
    };
  }

  @Override
  public IoFactory ioFactory() {
    return zystem.ioFactory();
  }
  
  private static IoFactory getForIn(
      final IoFactory ioFactory, 
      final Source<Character> otherIn) {
    return new IoFactory() {

      @Override
      public Sink<Character> buildErr() {
        return ioFactory.buildErr();
      }

      @Override
      public Source<Character> buildIn() {
        return otherIn;
      }

      @Override
      public Sink<Character> buildOut() {
        return ioFactory.buildOut();
      }
    };
  }

  private static IoFactory getForOut(
      final IoFactory ioFactory, 
      final Sink<Character> otherOut) {
    return new IoFactory() {

      @Override
      public Sink<Character> buildErr() {
        return ioFactory.buildErr();
      }

      @Override
      public Source<Character> buildIn() {
        return ioFactory.buildIn();
      }

      @Override
      public Sink<Character> buildOut() {
        return otherOut;
      }
    };
  }

  private static IoFactory getForErr(
      final IoFactory ioFactory, 
      final Sink<Character> otherErr) {
    return new IoFactory() {

      @Override
      public Sink<Character> buildErr() {
        return otherErr;
      }

      @Override
      public Source<Character> buildIn() {
        return ioFactory.buildIn();
      }

      @Override
      public Sink<Character> buildOut() {
        return ioFactory.buildOut();
      }
    };
  }
  
  public ZystemSelfBuilder withIn(Source<Character> otherIn) {
    return new ZystemSelfBuilder(
        new RealZystem(
            getForIn(zystem.ioFactory(), otherIn),
            getCurrentDir(), 
            getHomeDir(), 
            zystem.env(),
            zystem.getThreadFactory()));
  }

  public ZystemSelfBuilder withOut(Sink<Character> otherOut) {
    return new ZystemSelfBuilder(
        new RealZystem(
            getForOut(zystem.ioFactory(), otherOut), 
            getCurrentDir(), 
            getHomeDir(), 
            zystem.env(),
            zystem.getThreadFactory()));
  }

  public ZystemSelfBuilder withErr(Sink<Character> otherErr) {
    return new ZystemSelfBuilder(
        new RealZystem(
            getForErr(zystem.ioFactory(), otherErr), 
            getCurrentDir(), 
            getHomeDir(), 
            zystem.env(),
            zystem.getThreadFactory()));
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
  public ThreadFactory getThreadFactory() {
    return zystem.getThreadFactory();
  }
}