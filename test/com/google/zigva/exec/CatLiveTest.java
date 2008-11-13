// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.exec;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.zigva.exec.Cat.Builder;
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
    Builder catBuilder = injector.getInstance(Cat.Builder.class);
    Cat cat = catBuilder.create();
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
}
