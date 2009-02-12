/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zigva.lang.impl;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.zigva.io.FilePath;
import com.google.zigva.io.PassiveSink;
import com.google.zigva.io.Source;
import com.google.zigva.java.RealZystem;
import com.google.zigva.lang.IoFactory;
import com.google.zigva.lang.IoFactorySelfBuilder;
import com.google.zigva.lang.SinkFactory;
import com.google.zigva.lang.Propertiez;
import com.google.zigva.lang.UserInfo;
import com.google.zigva.lang.Zystem;

import java.util.Map;

//TODO: make it also implement Provider<Zystem>?
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
  public Map<String, String> env() {
    return zystem.env();
  }
  
  public ZystemSelfBuilder withCurrentDir(FilePath dir) {
    return new ZystemSelfBuilder(
        new RealZystem(
            zystem.ioFactory(),
            dir, 
            zystem.getHomeDir(), 
            zystem.userInfo(), 
            zystem.env()));
  }
  
  public ZystemSelfBuilder withEnv(Map<String, String> otherEnv) {
    return new ZystemSelfBuilder(
        new RealZystem(
            zystem.ioFactory(),
            zystem.getCurrentDir(), 
            zystem.getHomeDir(), 
            zystem.userInfo(), 
            otherEnv));
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
    
    return new IoFactorySelfBuilder(
        IoFactorySelfBuilder.in(otherIn), 
        ioFactory.out(), 
        ioFactory.err(), 
        ioFactory);
  }

  private static IoFactory getForOut(
      final IoFactory ioFactory, 
      final PassiveSink<Character> otherOut) {
    return new IoFactorySelfBuilder(
        ioFactory.in(), 
        IoFactorySelfBuilder.out(otherOut), 
        ioFactory.err(), 
        ioFactory);
  }

  private static IoFactory getForErr(
      final IoFactory ioFactory, 
      final PassiveSink<Character> otherErr) {
    return new IoFactorySelfBuilder(
        ioFactory.in(), 
        ioFactory.out(), 
        IoFactorySelfBuilder.err(otherErr), 
        ioFactory);
  }
  
  public ZystemSelfBuilder withIn(Source<Character> otherIn) {
    return new ZystemSelfBuilder(
        new RealZystem(
            getForIn(zystem.ioFactory(), otherIn),
            zystem.getCurrentDir(), 
            zystem.getHomeDir(), 
            zystem.userInfo(), 
            zystem.env()));
  }

  public ZystemSelfBuilder withOut(SinkFactory<Character> otherOut) {
    return new ZystemSelfBuilder(
        new RealZystem(
            new IoFactorySelfBuilder(
                zystem.ioFactory().in(),
                otherOut,
                zystem.ioFactory().err(), 
                zystem.ioFactory()), 
            zystem.getCurrentDir(), 
            zystem.getHomeDir(), 
            zystem.userInfo(), 
            zystem.env()));
  }

  @Deprecated
  public ZystemSelfBuilder withOut(PassiveSink<Character> otherOut) {
    return new ZystemSelfBuilder(
        new RealZystem(
            getForOut(zystem.ioFactory(), otherOut), 
            zystem.getCurrentDir(), 
            zystem.getHomeDir(), 
            zystem.userInfo(), 
            zystem.env()));
  }

  public ZystemSelfBuilder withErr(SinkFactory<Character> otherErr) {
    return new ZystemSelfBuilder(
        new RealZystem(
            new IoFactorySelfBuilder(
                zystem.ioFactory().in(), 
                zystem.ioFactory().out(), 
                otherErr, 
                zystem.ioFactory()), 
            zystem.getCurrentDir(), 
            zystem.getHomeDir(), 
            zystem.userInfo(), 
            zystem.env()));
  }
  
  @Deprecated
  public ZystemSelfBuilder withErr(PassiveSink<Character> otherErr) {
    return new ZystemSelfBuilder(
        new RealZystem(
            getForErr(zystem.ioFactory(), otherErr), 
            zystem.getCurrentDir(), 
            zystem.getHomeDir(), 
            zystem.userInfo(), 
            zystem.env()));
  }

  @Override
  public String toString() {
    return zystem.toString();
  }

  @Override
  public UserInfo userInfo() {
    return zystem.userInfo();
  }
}