// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.java;

import com.google.zigva.io.Source;

import junit.framework.TestCase;

import org.easymock.EasyMock;

public class SourceSourceTest extends TestCase {

  public void testClose() {
    Source inner = EasyMock.createMock(Source.class);
    EasyMock.replay(inner);
    
    Source outer = new SourceSource(inner, true);
    outer.close();
    
    // Makes sure close was never called
    EasyMock.verify(inner);
  }
  
}
