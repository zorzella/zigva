// Copyright 2009 Google Inc.  All Rights Reserved.
package com.google.zigva.lang;

import com.google.common.collect.Lists;
import com.google.common.testing.junit3.JUnitAsserts;

import junit.framework.TestCase;

import java.util.Collection;
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
    List<Exception> empty = Lists.newArrayList();
    try {
      ClusterException.create(empty);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testSingleRuntime() throws Exception {
    Exception fooException = new RuntimeException("foo");
    List<Exception> list = Lists.newArrayList(fooException);
    assertEquals(fooException, ClusterException.create(list));
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
    List<Exception> list = Lists.newArrayList(fooException, barException);
    RuntimeException created = ClusterException.create(list);
    assertTrue(created instanceof ClusterException);
    ClusterException cluster = (ClusterException)created;
    assertEquals(fooException, cluster.exceptions.iterator().next());
    assertEquals(2, cluster.exceptions.size());
  }

  public void testImmutability() throws Exception {
    Exception fooException = new Exception("foo");
    Exception barException = new Exception("bar");
    List<Exception> list = Lists.newArrayList(fooException, barException);
    ClusterException created = (ClusterException)ClusterException.create(list);
    assertEquals(2, created.exceptions.size());
    list.add(new Exception("baz"));
    assertEquals(2, created.exceptions.size());
    try {
      Collection<Throwable> exceptions = created.exceptions;
      exceptions.add(new Exception("bur"));
      fail();
    } catch (UnsupportedOperationException expected) {
    }
  }
}
