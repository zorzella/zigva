// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.exec;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.zigva.guice.ZivaModule;
import com.google.zigva.guice.ZystemSelfBuilder;
import com.google.zigva.io.CharacterSource;
import com.google.zigva.io.SinkToString;
import com.google.zigva.io.Source;
import com.google.zigva.lang.Zystem;

import junit.framework.TestCase;

public class CatLiveTest extends TestCase {

  public void testSunnycase() {
    Injector injector = Guice.createInjector(new ZivaModule());
    Cat cat = new Cat();
    Zystem zystem = injector.getInstance(Zystem.class);
    SinkToString sink = new SinkToString();
    Source<Character> source = new CharacterSource("foo");
    new ZystemSelfBuilder(zystem)
      .withIn(source)
      .withOut(sink)
      .cmdExecutor()
      .command(cat)
      .execute()
      .waitFor();
    assertEquals("foo", sink.toString());
  }

  public void testPipe() {
    Injector injector = Guice.createInjector(new ZivaModule());
    Cat cat = new Cat();
    Zystem zystem = injector.getInstance(Zystem.class);
    SinkToString sink = new SinkToString();
    Source<Character> source = new CharacterSource("foo");
    new ZystemSelfBuilder(zystem)
      .withIn(source)
      .withOut(sink)
      .cmdExecutor()
      .command(cat)
      .pipe(cat)
      .execute()
      .waitFor();
    assertEquals("foo", sink.toString());
  }

  public void testMultipePipes() throws InterruptedException {
    Injector injector = Guice.createInjector(new ZivaModule());
    Cat cat = new Cat();
    Zystem zystem = injector.getInstance(Zystem.class);
    SinkToString sink = new SinkToString();
    Source<Character> source = new CharacterSource("foo");
    new ZystemSelfBuilder(zystem)
      .withIn(source)
      .withOut(sink)
      .cmdExecutor()
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
