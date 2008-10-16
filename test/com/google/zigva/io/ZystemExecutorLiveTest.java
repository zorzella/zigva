package com.google.zigva.io;

import com.google.common.base.Join;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3TestCase;
import com.google.zigva.ZivaEnvs;
import com.google.zigva.exec.ZivaTask;
import com.google.zigva.exec.Executor.Command;
import com.google.zigva.guice.ZystemSelfBuilder;
import com.google.zigva.lang.Waitable;
import com.google.zigva.lang.Zystem;

import java.io.IOException;
import java.util.Map;

@GuiceBerryEnv(ZivaEnvs.REGULAR)
public class ZystemExecutorLiveTest extends GuiceBerryJunit3TestCase {

  @Inject
  private Zystem zystem;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
//    TearDown tearDown = new TearDown() {
//      @Override
//      public void tearDown() throws Exception {
//        assertEquals(threadCount, Thread.activeCount());
//      }
//    };
//    addTearDown(tearDown);
  }

  public static final class ThreadCountAsserter {
    
    private final Map<Thread, StackTraceElement[]>  allOriginalStackTraces;
    private final int expectedNoThreads;

    public ThreadCountAsserter() {
      this.allOriginalStackTraces = Thread.getAllStackTraces();
      this.expectedNoThreads = allOriginalStackTraces.keySet().size();
    }

    public void assertThreadCount() throws InterruptedException {
      long failAt = System.currentTimeMillis() + 100;
      int activeCount;
      while ((activeCount = Thread.activeCount()) != expectedNoThreads) {
        if (failAt < System.currentTimeMillis()) {
          Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
          System.out.println(String.format(
              "**********BEFORE (%d) **************", expectedNoThreads));
          printStackTraces(allOriginalStackTraces);
          System.out.println(String.format(
              "**********AFTER (%d) **************", activeCount));
          printStackTraces(allStackTraces);
          System.out.println("**********END**************");
          throw new AssertionError(String.format(
              "Thread leak (%d threads leaked). " +
          		"See system out for details", activeCount - expectedNoThreads));
        }
        Thread.sleep(10);
      }
    }

    private void printStackTraces(Map<Thread, StackTraceElement[]> allStackTraces) {
      for (Thread thread: allStackTraces.keySet()) {
        System.out.println(String.format(
            "*** Thread '%s': \n %s", thread.getName(), 
            Join.join("\n", allStackTraces.get(thread))));
      }
    }
  }
  
  @Override
  protected void runTest() throws Throwable {
    ThreadCountAsserter asserter = new ThreadCountAsserter();
    int threadCount = Thread.activeCount();
    super.runTest();
    asserter.assertThreadCount();
//    //TODO: assert thread count!
//    Thread.sleep(100);
//    assertEquals(threadCount, Thread.activeCount());
  }
  
//  public void testSystemInReady() throws Exception {
//    InputStreamReader foo = new InputStreamReader(System.in);
//    assertFalse(foo.ready());
//  }
  
  public void testEnv() throws Exception {
    
    
    StringBuilder out = new StringBuilder();
    Map<String,String> fooBarBazIsZ = Maps.newHashMap();
    String expected = "z";
    String envName = "FOOBARBAZ";
    fooBarBazIsZ.put(envName, expected);
    
    Zystem localZystem = 
      new ZystemSelfBuilder(zystem)
        .withEnv(fooBarBazIsZ)
        .withOut(out);

    Waitable process = localZystem.executor().command("printenv", envName).execute();
    process.waitFor();
    assertEquals(expected, out.toString().trim());
  }

  
  public void testIn() throws Exception {
    StringBuilder out = new StringBuilder();
    String expected = "ziva rules";
    
    Zystem localZystem = 
      new ZystemSelfBuilder(zystem)
        .withIn(new CharacterSource(expected))
        .withOut(out);

    Waitable process = localZystem.executor().command("cat").execute();
    process.waitFor();
    assertEquals(expected, out.toString().trim());
  }

  public void testNonShell() throws Exception {
    MyCommand myCommand = new MyCommand();
    Appendable actual = new StringBuilder();
    Zystem localZystem =
      new ZystemSelfBuilder(zystem)
        .withOut(actual);
    ZivaTask task = localZystem.executor().command(myCommand).execute();
    task.waitFor();
    assertEquals("z", actual.toString());
  }
  
  
  // $ echo foo | cat
  public void SUPPRESS_testPipe() throws Exception {
    StringBuilder out = new StringBuilder();
    
    Zystem localZystem = 
      new ZystemSelfBuilder(zystem)
        .withOut(out);

    String expected = "foo";
    Waitable process = localZystem.executor()
      .command("echo", expected)
      .pipe("cat")
      .execute();
    System.out.println(out);
    process.waitFor();
    assertEquals(expected, out.toString().trim());
  }

  // ls | cat > bar.txt
  // $ ls | (cd ../bar; cat > bar.txt)
  // executor().source("foo").sink(bar).execute();

  private static final class MyCommand implements Command {

    @Override
    public ZivaTask execute(Zystem zystem) {
      try {
        zystem.out().append('z');
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      //TODO: SyncZivaTask?
      return new ZivaTask() {
        @Override
        public void waitFor() {
        }
      };
    }
  }
}
