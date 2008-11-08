package com.google.zigva.io;

import com.google.common.testing.junit3.TearDownTestCase;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.zigva.guice.ZivaModule;
import com.google.zigva.guice.ZystemSelfBuilder;
import com.google.zigva.java.RootZystemProvider;
import com.google.zigva.lang.Waitable;
import com.google.zigva.lang.Zystem;

public class BasicZystemExecutorTest extends TearDownTestCase {
  
  private static final class EchoFoo implements Runnable {
    @Inject
    private Zystem zystem;
    
    public void run() {
      Waitable process = 
        zystem.cmdExecutor().command("echo", "foo").execute();
      process.waitFor();
    }
  }

  public void testSwappedRootZystem() throws Exception {
    SinkToString out = new SinkToString();
    Provider<Zystem> rootZystem = 
      new ZivaModule.ZystemProvider(
          new ZystemSelfBuilder(new RootZystemProvider().get())
          .withOut(out));
    Injector injector = Guice.createInjector(new ZivaModule(rootZystem));
    EchoFoo task = injector.getInstance(EchoFoo.class);
    task.run();
    assertEquals("foo", out.toString().trim());
  }
  
  private static final class MyApp {
    @Inject
    private Zystem zystem;
    
    public String go() {
      SinkToString out = new SinkToString();
      Zystem localZystem = 
        new ZystemSelfBuilder(zystem)
          .withOut(out);

      Waitable process = localZystem.cmdExecutor().command("echo", "foo").execute();
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
