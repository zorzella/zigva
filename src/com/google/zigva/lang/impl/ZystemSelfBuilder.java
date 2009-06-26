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
import com.google.zigva.io.IoFactory;
import com.google.zigva.io.IoFactorySelfBuilder;
import com.google.zigva.io.PumpFactory;
import com.google.zigva.io.Sink;
import com.google.zigva.io.Source;
import com.google.zigva.java.RealZystem;
import com.google.zigva.sys.Zystem;

import java.util.Map;

//TODO: make it also implement Provider<Zystem>?
public final class ZystemSelfBuilder extends ExtendedZystem {
  
  @Inject
  public ZystemSelfBuilder(Zystem zystem) {
    super(zystem);
  }

  public ZystemSelfBuilder withCurrentDir(FilePath dir) {
    return new ZystemSelfBuilder(
        new RealZystem(
            delegate.ioFactory(),
            dir, 
            delegate.getHomeDir(), 
            delegate.userInfo(), 
            delegate.env()));
  }
  
  public ZystemSelfBuilder withEnv(Map<String, String> otherEnv) {
    return new ZystemSelfBuilder(
        new RealZystem(
            delegate.ioFactory(),
            delegate.getCurrentDir(), 
            delegate.getHomeDir(), 
            delegate.userInfo(), 
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
      final Sink<Character> otherOut) {
    return new IoFactorySelfBuilder(
        ioFactory.in(), 
        IoFactorySelfBuilder.out(otherOut), 
        ioFactory.err(), 
        ioFactory);
  }

  private static IoFactory getForErr(
      final IoFactory ioFactory, 
      final Sink<Character> otherErr) {
    return new IoFactorySelfBuilder(
        ioFactory.in(), 
        ioFactory.out(), 
        IoFactorySelfBuilder.err(otherErr), 
        ioFactory);
  }
  
  public ZystemSelfBuilder withIn(Source<Character> otherIn) {
    return new ZystemSelfBuilder(
        new RealZystem(
            getForIn(delegate.ioFactory(), otherIn),
            delegate.getCurrentDir(), 
            delegate.getHomeDir(), 
            delegate.userInfo(), 
            delegate.env()));
  }

  public ZystemSelfBuilder withOut(PumpFactory<Character> otherOut) {
    return new ZystemSelfBuilder(
        new RealZystem(
            new IoFactorySelfBuilder(
                delegate.ioFactory().in(),
                otherOut,
                delegate.ioFactory().err(), 
                delegate.ioFactory()), 
            delegate.getCurrentDir(), 
            delegate.getHomeDir(), 
            delegate.userInfo(), 
            delegate.env()));
  }

  @Deprecated
  public ZystemSelfBuilder withOut(Sink<Character> otherOut) {
    return new ZystemSelfBuilder(
        new RealZystem(
            getForOut(delegate.ioFactory(), otherOut), 
            delegate.getCurrentDir(), 
            delegate.getHomeDir(), 
            delegate.userInfo(), 
            delegate.env()));
  }

  public ZystemSelfBuilder withErr(PumpFactory<Character> otherErr) {
    return new ZystemSelfBuilder(
        new RealZystem(
            new IoFactorySelfBuilder(
                delegate.ioFactory().in(), 
                delegate.ioFactory().out(), 
                otherErr, 
                delegate.ioFactory()), 
            delegate.getCurrentDir(), 
            delegate.getHomeDir(), 
            delegate.userInfo(), 
            delegate.env()));
  }
  
  @Deprecated
  public ZystemSelfBuilder withErr(Sink<Character> otherErr) {
    return new ZystemSelfBuilder(
        new RealZystem(
            getForErr(delegate.ioFactory(), otherErr), 
            delegate.getCurrentDir(), 
            delegate.getHomeDir(), 
            delegate.userInfo(), 
            delegate.env()));
  }
}