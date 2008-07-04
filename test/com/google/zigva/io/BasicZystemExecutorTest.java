package com.google.zigva.io;

import com.google.common.testing.TearDown;
import com.google.common.testing.junit3.TearDownTestCase;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.zigva.guice.ZivaModule;
import com.google.zigva.guice.ZystemSelfBuilder;
import com.google.zigva.io.Zystem;
import com.google.zigva.java.JavaZystem;
import com.google.zigva.lang.Waitable;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class BasicZystemExecutorTest extends TearDownTestCase {
  
  private static final class EchoFoo implements Runnable {
    @Inject
    private Zystem zystem;
    
    public void run() {
      Waitable process = zystem.executor().command("echo", "foo").execute();
      process.waitFor();
    }
  }

  public void testSystemStdout() throws Exception {
    final PrintStream oldOut = System.out;
    TearDown tearDown = new TearDown(){
      @Override
      public void tearDown() throws Exception {
        System.setOut(oldOut);
      }
    };
    addTearDown(tearDown);
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(byteArrayOutputStream);
    System.setOut(out);
    Injector injector = Guice.createInjector(new ZivaModule());
    EchoFoo task = injector.getInstance(EchoFoo.class);
    task.run();
    assertEquals("foo", byteArrayOutputStream.toString().trim());
  }

  public void testSwappedRootZystem() throws Exception {
    StringBuilder out = new StringBuilder();
    Zystem rootZystem = 
      new ZystemSelfBuilder(JavaZystem.get())
        .withOut(out);
    Injector injector = Guice.createInjector(new ZivaModule(rootZystem));
    EchoFoo task = injector.getInstance(EchoFoo.class);
    task.run();
    assertEquals("foo", out.toString().trim());
  }
  
  private static final class MyApp {
    @Inject
    private Zystem zystem;
    
    public String go() {
      StringBuilder out = new StringBuilder();
      Zystem localZystem = 
        new ZystemSelfBuilder(zystem)
          .withOut(out);

      Waitable process = localZystem.executor().command("echo", "foo").execute();
      process.waitFor();
      return out.toString();
    }
  }

  public void testLocalRootZystem() throws Exception {
    Injector injector = Guice.createInjector(new ZivaModule());
    MyApp myApp = injector.getInstance(MyApp.class);
    String result = myApp.go();
    assertEquals("foo", result.trim());
  }
  
}
