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

package com.google.zigva.exec;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3TestCase;
import com.google.zigva.ZigvaEnvs;
import com.google.zigva.command.Cat;
import com.google.zigva.guice.ZigvaModule;
import com.google.zigva.io.CharSequenceSource;
import com.google.zigva.io.PumpToString;
import com.google.zigva.io.Source;
import com.google.zigva.lang.impl.ZystemSelfBuilder;
import com.google.zigva.sys.Zystem;

@GuiceBerryEnv(ZigvaEnvs.REGULAR)
public class CatLiveTest extends GuiceBerryJunit3TestCase {

  @Inject
  private CommandExecutor commandExecutor;
  
  @Inject 
  private ZystemSelfBuilder zystem;
  
  public void testSunnycase() {
    Injector injector = Guice.createInjector(new ZigvaModule());
    Cat cat = new Cat();
    PumpToString out = new PumpToString();
    Source<Character> source = new CharSequenceSource("foo");
    Zystem modifiedZystem = 
      zystem
        .withIn(source)
        .withOut(out);

    commandExecutor.with(modifiedZystem)
      .command(cat)
      .execute()
      .waitFor();
    assertEquals("foo", out.asString());
  }

  public void testPipe() {
    Injector injector = Guice.createInjector(new ZigvaModule());
    Cat cat = new Cat();
    PumpToString out = new PumpToString();
    Source<Character> source = new CharSequenceSource("foo");
    Zystem modifiedZystem = 
      zystem
        .withIn(source)
        .withOut(out);
    commandExecutor.with(modifiedZystem)
      .command(cat)
      .pipe(cat)
      .execute()
      .waitFor();
    assertEquals("foo", out.asString());
  }

  public void testMultipePipes() {
    Injector injector = Guice.createInjector(new ZigvaModule());
    Cat cat = new Cat();
    PumpToString out = new PumpToString();
    Source<Character> source = new CharSequenceSource("foo");
    Zystem modifiedZystem = zystem
      .withIn(source)
      .withOut(out);
    commandExecutor.with(modifiedZystem)
      .command(cat)
      .pipe(cat)
      .pipe(cat)
      .pipe(cat)
      .pipe(cat)
      .pipe(cat)
      .pipe(cat)
      .pipe(cat)
      .pipe(cat)
      .execute()
      .waitFor();
    assertEquals("foo", out.asString());
  }
}
