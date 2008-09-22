// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.io;

import junit.framework.TestCase;

public class StringSourceTest extends TestCase {

  public void testFoo() {
    StringSource source = new StringSource("foo");
    StringBuilder builder = new StringBuilder();
    while(!source.isEndOfStream()) {
      builder.append((char)source.read());
    }
    assertEquals("foo", builder.toString());
    
  }
  
}
