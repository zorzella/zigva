package com.google.zigva.io;

import com.google.zigva.io.AppendableWriter;

import junit.framework.TestCase;

public class AppendableWriterTest extends TestCase {

  public void testSunny() throws Exception {
    StringBuilder out = new StringBuilder();
    AppendableWriter writer = new AppendableWriter(out);
    CharSequence expected = "foobar";
    writer.append(expected);
    assertEquals(expected, out.toString());
  }
}
