// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.io;

import com.google.zigva.exec.impl.SimpleThreadRunner;
import com.google.zigva.lang.ZigvaThreadFactory;

import junit.framework.TestCase;

public class ForkingSinkTest extends TestCase {

  public void testSimple() throws Exception {
    String expected = "foo bar baz";
    Source<Character> source = new CharacterSource(expected);

    PumpToString sink1 = new PumpToString();
    PumpToString sink2 = new PumpToString();
    
    @SuppressWarnings("unchecked")
    Pump sink = new ForkingSinkFactory<Character>(
        new SimpleThreadRunner(new ZigvaThreadFactory()),
        sink1,
        sink2).getPumpFor(source);
    
    sink.run();
    
    assertEquals(expected, sink1.asString());
    assertEquals(expected, sink2.asString());
  }
}
