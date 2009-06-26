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
import com.google.inject.util.Providers;
import com.google.zigva.exec.CommandExecutor;
import com.google.zigva.exec.CommandExecutor.Command;
import com.google.zigva.guice.ZigvaModule;
import com.google.zigva.java.RootZystemProvider;
import com.google.zigva.lang.ConvenienceWaitable;
import com.google.zigva.lang.impl.ZystemSelfBuilder;
import com.google.zigva.sh.OS;
import com.google.zigva.sys.Zystem;

public class BasicZystemExecutorTest extends TearDownTestCase {
  
  private static final class EchoFoo implements Runnable {
    
    @Inject
    private CommandExecutor commandExecutor;
    
    @Inject
    private OS os;
    
    public void run() {
      Command echoFoo = os.command("echo", "foo");
      ConvenienceWaitable process = 
        commandExecutor.command(echoFoo).execute();
      process.waitFor();
    }
  }

  public void testSwappedRootZystem() throws Exception {
    PumpToString out = new PumpToString();
    Provider<ZystemSelfBuilder> rootZystem = 
      Providers.of(
          new ZystemSelfBuilder(new RootZystemProvider().get())
          .withOut(out));
    Injector injector = Guice.createInjector(new ZigvaModule(rootZystem));
    EchoFoo task = injector.getInstance(EchoFoo.class);
    task.run();
    assertEquals("foo", out.asString().trim());
  }
  
  private static final class MyApp {
    @Inject
    private ZystemSelfBuilder zystem;
    
    @Inject
    private OS os;

    @Inject
    private CommandExecutor commandExecutor;
    
    public String go() {
      PumpToString out = new PumpToString();
      Zystem modifiedZystem = zystem.withOut(out);
      Command echoFoo = os.command("echo", "foo");
      
      ConvenienceWaitable process =
        commandExecutor
        .with(modifiedZystem)
          .command(echoFoo).execute();
      process.waitFor();
      return out.asString();
    }
  }

  public void testLocalRootZystem() throws Exception {
    Injector injector = Guice.createInjector(new ZigvaModule());
    MyApp myApp = injector.getInstance(MyApp.class);
    String result = myApp.go();
    assertEquals("foo", result.trim());
  }
}
