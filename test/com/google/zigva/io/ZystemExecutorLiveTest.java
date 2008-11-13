package com.google.zigva.io;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3TestCase;
import com.google.zigva.ZivaEnvs;
import com.google.zigva.exec.SyncZivaTask;
import com.google.zigva.exec.ZivaTask;
import com.google.zigva.exec.CommandExecutor.Command;
import com.google.zigva.guice.ZystemSelfBuilder;
import com.google.zigva.lang.Waitable;
import com.google.zigva.lang.Zystem;

import java.util.Map;

@GuiceBerryEnv(ZivaEnvs.REGULAR)
public class ZystemExecutorLiveTest extends GuiceBerryJunit3TestCase {

  @Inject
  private Zystem zystem;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }
  
  @Override
  protected void runTest() throws Throwable {
    ThreadCountAsserter asserter = new ThreadCountAsserter();
    super.runTest();
    asserter.assertThreadCount();
  }
  
  public void testEnv() throws Exception {
    
    SinkToString out = new SinkToString();
    Map<String,String> fooBarBazIsZ = Maps.newHashMap();
    String expected = "z";
    String envName = "FOOBARBAZ";
    fooBarBazIsZ.put(envName, expected);
    
    Zystem localZystem = 
      new ZystemSelfBuilder(zystem)
        .withEnv(fooBarBazIsZ)
        .withOut(out);

    Waitable process = localZystem.cmdExecutor().command("printenv", envName).execute();
    process.waitFor();
    assertEquals(expected, out.toString().trim());
  }

  
  public void testIn() throws Exception {
    SinkToString out = new SinkToString();
    String expected = "ziva rules";
    
    Zystem localZystem = 
      new ZystemSelfBuilder(zystem)
        .withIn(new CharacterSource(expected))
        .withOut(out);

    Waitable process = localZystem.cmdExecutor().command("cat").execute();
    process.waitFor();
    assertEquals(expected, out.toString().trim());
  }

  public void testNonShell() throws Exception {
    MyCommand myCommand = new MyCommand();
    SinkToString actual = new SinkToString();
    Zystem localZystem =
      new ZystemSelfBuilder(zystem)
        .withOut(actual);
    ZivaTask task = localZystem.cmdExecutor().command(myCommand).execute();
    task.waitFor();
    assertEquals("z", actual.toString());
  }
  
  
  // $ echo foo | cat
  public void testPipe() throws Exception {
    SinkToString out = new SinkToString();
    
    Zystem localZystem = 
      new ZystemSelfBuilder(zystem)
        .withOut(out);

    String expected = "foo";
    Waitable process = localZystem.cmdExecutor()
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
    public ZivaTask execute(final Zystem zystem) {
      //TODO: SyncZivaTask?
      return new SyncZivaTask(new ZivaTask() {
        @Override
        public void waitFor() {
        }

        @Override
        public void kill() {
        }

        @Override
        public String getName() {
          return "MyCommand";
        }

        @Override
        public void run() {
          zystem.ioFactory().buildOut().write('z');
        }
      });
    }
  }
}
