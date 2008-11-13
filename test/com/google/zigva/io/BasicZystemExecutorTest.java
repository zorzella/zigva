package com.google.zigva.io;

import com.google.common.testing.junit3.TearDownTestCase;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.zigva.exec.Cat;
import com.google.zigva.guice.Providers;
import com.google.zigva.guice.ZivaModule;
import com.google.zigva.guice.ZystemSelfBuilder;
import com.google.zigva.java.RootZystemProvider;
import com.google.zigva.lang.NamedRunnable;
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
      Providers.of(
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
  
  public void testInheritingThreadLocalSemantics() {
    Injector injector = Guice.createInjector(new ZivaModule());
    
    injector.getInstance(OtherApp.class);
  }
  
  private static final class OtherApp {
    
    private static final class Task implements NamedRunnable {

      @Inject
      private Zystem zystem;

      @Inject
      private ZivaModule.ZystemProvider zystemProvider;

      @Override
      public String getName() {
        return "NamedRunnable";
      }

      @Override
      public void run() {
        
//        zystemProvider.threadLocal.set(value)
        
        SinkToString sink = new SinkToString();
        Source<Character> source = new CharacterSource("foo");
        new ZystemSelfBuilder(zystem)
          .withIn(source)
          .withOut(sink)
          .cmdExecutor()
          .command(new Cat())
          .execute()
          .waitFor();
      }
      
    }
    
    @Inject
    private ZivaModule.ZystemProvider zystemProvider;
    
    @Inject
    private Zystem zystem;
    
    @Inject
    private Task task;
    
    public String go() {
//      zystem.cmdExecutor().command(command)
//      SinkToString
      return null;
    }
  }
  
}
