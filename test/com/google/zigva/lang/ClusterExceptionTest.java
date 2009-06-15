// Copyright 2009 Google Inc.  All Rights Reserved.
package com.google.zigva.lang;

import com.google.common.collect.Lists;
import com.google.common.testing.junit3.JUnitAsserts;

import junit.framework.TestCase;

import java.util.List;

public class ClusterExceptionTest extends TestCase {

  public void testNull() throws Exception {
    List<Exception> aNull = null;
    try {
      ClusterException.create(aNull);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testEmpty() throws Exception {
    List<Exception> aNull = Lists.newArrayList();
    try {
      ClusterException.create(aNull);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testSingleRuntime() throws Exception {
    Exception fooException = new RuntimeException("foo");
    List<Exception> aNull = Lists.newArrayList(fooException);
    assertEquals(fooException, ClusterException.create(aNull));
  }
  
  public void testSingleChecked() throws Exception {
    Exception fooException = new Exception("foo");
    List<Exception> aNull = Lists.newArrayList(fooException);
    assertEquals(fooException, ClusterException.create(aNull).getCause());
    JUnitAsserts.assertNotEqual(fooException, ClusterException.create(aNull));
  }

  public void testTwo() throws Exception {
    Exception fooException = new Exception("foo");
    Exception barException = new Exception("bar");
    List<Exception> aNull = Lists.newArrayList(fooException, barException);
    RuntimeException created = ClusterException.create(aNull);
    assertTrue(created instanceof ClusterException);
    ClusterException cluster = (ClusterException)created;
    assertEquals(fooException, cluster.exceptions.iterator().next());
    assertEquals(2, cluster.exceptions.size());
  }
}
