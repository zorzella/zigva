// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.exec;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3TestCase;
import com.google.zigva.ZivaEnvs;
import com.google.zigva.guice.ZivaModule;
import com.google.zigva.guice.ZystemSelfBuilder;
import com.google.zigva.io.CharacterSource;
import com.google.zigva.io.SinkToString;
import com.google.zigva.io.Source;
import com.google.zigva.lang.Zystem;

@GuiceBerryEnv(ZivaEnvs.REGULAR)
public class CatLiveTest extends GuiceBerryJunit3TestCase {

  @Inject
  private CommandExecutor.Builder commandExecutorBuilder;
  
  @Inject 
  private ZystemSelfBuilder zystem;
  
  public void testSunnycase() {
    Injector injector = Guice.createInjector(new ZivaModule());
    Cat cat = new Cat();
    SinkToString sink = new SinkToString();
    Source<Character> source = new CharacterSource("foo");
    Zystem modifiedZystem = 
      zystem
        .withIn(source)
        .withOut(sink);

    commandExecutorBuilder.with(modifiedZystem).create()
      .command(cat)
      .execute()
      .waitFor();
    assertEquals("foo", sink.toString());
  }

  public void testPipe() {
    Injector injector = Guice.createInjector(new ZivaModule());
    Cat cat = new Cat();
    SinkToString sink = new SinkToString();
    Source<Character> source = new CharacterSource("foo");
    Zystem modifiedZystem = 
      zystem
        .withIn(source)
        .withOut(sink);
    commandExecutorBuilder.with(modifiedZystem).create()
      .command(cat)
      .pipe(cat)
      .execute()
      .waitFor();
    assertEquals("foo", sink.toString());
  }

  public void testMultipePipes() throws InterruptedException {
    Injector injector = Guice.createInjector(new ZivaModule());
    Cat cat = new Cat();
    SinkToString sink = new SinkToString();
    Source<Character> source = new CharacterSource("foo");
    Zystem modifiedZystem = zystem
      .withIn(source)
      .withOut(sink);
    commandExecutorBuilder.with(modifiedZystem).create()
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
    Thread.sleep(300);
    assertEquals("foo", sink.toString());
  }
}
