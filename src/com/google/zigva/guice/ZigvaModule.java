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

package com.google.zigva.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.zigva.exec.CommandExecutor;
import com.google.zigva.exec.SimpleCommandExecutor;
import com.google.zigva.exec.SimpleThreadRunner;
import com.google.zigva.exec.ThreadRunner;
import com.google.zigva.io.FileRepository;
import com.google.zigva.java.JavaProcessStarter;
import com.google.zigva.java.RealFileRepository;
import com.google.zigva.java.RealJavaProcessStarter;
import com.google.zigva.java.RootZystemProvider;
import com.google.zigva.lang.Zystem;

public class ZigvaModule extends AbstractModule {

  private final Provider<Zystem> zystemProvider;

  public ZigvaModule() {
    zystemProvider = new ZystemProvider();
  }

  public ZigvaModule(Provider<Zystem> zystemProvider) {
    this.zystemProvider = zystemProvider;
  }

  @Override
  protected void configure() {
    install(new JavaModule());
//    ZystemScopeHelper zystemScopeHelper = new ZystemScopeHelper(rootZystem);
    bind(FileRepository.class).to(RealFileRepository.class);
    bind(Zystem.class).toProvider(zystemProvider);
    bind(JavaProcessStarter.class).to(RealJavaProcessStarter.class);
//    bind(ZystemScopeHelper.class).toInstance(zystemScopeHelper);
    bind(ThreadRunner.class).to(SimpleThreadRunner.class).in(Scopes.SINGLETON);
    bind(CommandExecutor.class).to(SimpleCommandExecutor.class);
  }
  
  public static final class ZystemProvider implements Provider<Zystem> {

    public ZystemProvider() {
      this(new RootZystemProvider().get());
    }
    
    public ZystemProvider(Zystem root) {
      threadLocal.set(root);
    }
    
    public final InheritableThreadLocal<Zystem> threadLocal = 
      new InheritableThreadLocal<Zystem>() {

      @Override
      protected Zystem initialValue() {
        throw new UnsupportedOperationException();
      }
    };
    
    @Override
    public Zystem get() {
      return threadLocal.get();
    }
  }
  
  
//  private static final class ZystemProvider implements Provider<Zystem> {
//
//    private final ZystemScopeHelper zystemScopeHelper;
//    
//    @Inject
//    public ZystemProvider(ZystemScopeHelper zystemScopeHelper) {
//      this.zystemScopeHelper = zystemScopeHelper;
//    }
//    
//    @Override
//    public Zystem get() {
//      return zystemScopeHelper.get();
//    }
//  }
//  
//  //TODO: do I use this?
//  
//  //TODO: make the threadlocal immutable (i.e. once something has been "set",
//  //it can't be re-set or removed
//  private static final class ZystemScopeHelper 
//    extends InheritableThreadLocal<Zystem> {
//
//    public ZystemScopeHelper(Provider<Zystem> rootZystem) {
//      set(rootZystem.get());
//    }
//  }
}
