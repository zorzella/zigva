package com.google.zigva.io;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3TestCase;
import com.google.zigva.ZivaEnvs;
import com.google.zigva.guice.ZystemSelfBuilder;
import com.google.zigva.io.Readers;
import com.google.zigva.io.ZivaTask;
import com.google.zigva.io.Zystem;
import com.google.zigva.io.Executor.Command;
import com.google.zigva.lang.Waitable;

import java.io.IOException;
import java.io.InputStreamReader;
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

  @Override
  protected void runTest() throws Throwable {
    int threadCount = Thread.activeCount();
    super.runTest();
    //TODO: assert thread count!
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
