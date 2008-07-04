package com.google.zigva.sh;

import com.google.zigva.io.NullAppendable;
import com.google.zigva.io.ZFile;
import com.google.zigva.io.ZFile.FileCreationFailurePolicy;
import com.google.zigva.sh.OS;
import com.google.zigva.sh.ZivaProcess;

import junit.framework.TestCase;


import java.io.File;

public class OSTest extends TestCase {

  public class CountingAppendable implements Appendable {

    int count = 0;

    public Appendable append(CharSequence csq) {
      count++;
      return this;
    }

    public Appendable append(char c) {
      count++;
      return this;
    }

    public Appendable append(CharSequence csq, int start, int end) {
      count++;
      return this;
    }
  }

  static class ThrowsOnOutputAppendable implements Appendable {

    boolean anExceptionOccurred = false;

    public Appendable append(CharSequence csq) {
      anExceptionOccurred = true;
      throw new AssertionError();
    }

    public Appendable append(char c) {
      anExceptionOccurred = true;
      throw new AssertionError();
    }

    public Appendable append(CharSequence csq, int start, int end) {
      anExceptionOccurred = true;
      throw new AssertionError();
    }

  }

  private File dir = new File(".");

  @SuppressWarnings("unused")
  private static final Appendable NOP = NullAppendable.INSTANCE;
  private static ThrowsOnOutputAppendable THROWS;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    THROWS = new ThrowsOnOutputAppendable();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    assertFalse(THROWS.anExceptionOccurred);
  }

  public void testBuiltInPwd() throws Exception {
    StringBuilder out = new StringBuilder();
    Appendable err = THROWS;
    OS.run("pwd", dir, out, err).waitFor();

    assertTrue(out.length() > 0);
    assertTrue(out.toString().startsWith("/"));
  }

  public void testBuiltInEcho() throws Exception {
    StringBuilder out = new StringBuilder();
    Appendable err = THROWS;
    OS.run("echo test", dir, out, err).waitFor();
    assertTrue(out.length() == 5);
  }

  public void testBuiltInEchoDashN() throws Exception {
    StringBuilder out = new StringBuilder();
    Appendable err = THROWS;
    OS.run("echo -n test", dir, out, err).waitFor();
    assertTrue(out.length() == 4);
  }

  public void testTouch() throws Exception {
    Appendable out = THROWS;
    Appendable err = THROWS;
    File test = File.createTempFile("zivatest", "txt");
    long lastModified = test.lastModified();
    sleep(1000);
    OS.run("touch " + test.getAbsolutePath(), dir, out, err).waitFor();
    assertTrue(test.lastModified() > lastModified);
  }

  public void testCat() throws Exception {
    StringBuilder out = new StringBuilder();
    Appendable err = THROWS;
    File test = File.createTempFile("zivatest", "txt");
    String MYCONTENTS = "mycontents";
    ZFile.createFileWithContents(test, MYCONTENTS,
      FileCreationFailurePolicy.DELETE_DIR_IF_EXISTS_TO_CREATE_FILE, false);
    OS.run("cat " + test.getAbsolutePath(), dir, out, err).waitFor();
    assertTrue(out.toString().equals(MYCONTENTS));
  }

  public void testKill() throws Exception {
    CountingAppendable out = new CountingAppendable();
    Appendable err = THROWS;
    assertTrue (out.count == 0);
    ZivaProcess zp = OS.run("yes", dir, out, err);
    sleep(1);
    assertTrue (out.count > 0);
    zp.process().destroy();
    sleep(1);
    int lastCount = out.count;
    sleep(1);
    assertTrue (out.count == lastCount);
  }
  
  // public void testPipe() throws Exception {
  // StringBuilder out = new StringBuilder();
  // StringBuilder err = new StringBuilder();
  // OS.run("echo -n test | grep test", dir, out, err);
  // assertTrue (out.length() == 4);
  // }
  
  
  private void sleep(long millis) throws InterruptedException {
    Thread.sleep(millis);
  }
}
