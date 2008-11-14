/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zigva.io;

import com.google.common.testing.junit3.TearDownTestCase;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.zigva.exec.Cat;
import com.google.zigva.exec.CommandExecutor;
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
    private CommandExecutor.Builder commandExecutorBuilder;
    
    public void run() {
      Waitable process = 
        commandExecutorBuilder.create().command("echo", "foo").execute();
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
    private ZystemSelfBuilder zystem;
    
    @Inject
    private CommandExecutor.Builder commandExecutorBuilder;
    
    public String go() {
      SinkToString out = new SinkToString();
      Zystem modifiedZystem = zystem.withOut(out);

      Waitable process =
        commandExecutorBuilder
          .with(modifiedZystem).create()
          .command("echo", "foo").execute();
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
      private ZystemSelfBuilder zystem;

      @Inject
      private CommandExecutor.Builder commandExecutorBuilder;
      
      @Override
      public String getName() {
        return "NamedRunnable";
      }

      @Override
      public void run() {
        SinkToString sink = new SinkToString();
        Source<Character> source = new CharacterSource("foo");
        ZystemSelfBuilder modifiedZystem = new ZystemSelfBuilder(zystem)
          .withIn(source)
          .withOut(sink);
        
        commandExecutorBuilder.with(modifiedZystem).create()
          .command(new Cat())
          .execute()
          .waitFor();
      }
    }
  }
}
