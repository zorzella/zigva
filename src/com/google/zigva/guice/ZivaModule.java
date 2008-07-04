package com.google.zigva.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.zigva.io.FileRepository;
import com.google.zigva.io.Zystem;
import com.google.zigva.java.JavaZystem;
import com.google.zigva.java.RealFileRepository;
import com.google.zigva.sh.JavaProcessExecutor;
import com.google.zigva.sh.RealJavaProcessExecutor;



public class ZivaModule extends AbstractModule {

  private final Zystem rootZystem;

  public ZivaModule() {
    rootZystem = JavaZystem.get();
  }

  public ZivaModule(Zystem rootZystem) {
    this.rootZystem = rootZystem;
  }
  
  @Override
  protected void configure() {
    ZystemScopeHelper zystemScopeHelper = new ZystemScopeHelper(rootZystem);
    bind(FileRepository.class).to(RealFileRepository.class);
    bind(Zystem.class).toProvider(ZystemProvider.class).in(Scopes.SINGLETON);
    bind(JavaProcessExecutor.class).to(RealJavaProcessExecutor.class);
    bind(ZystemScopeHelper.class).toInstance(zystemScopeHelper);
  }
  
  private static final class ZystemProvider implements Provider<Zystem> {

    private final ZystemScopeHelper zystemScopeHelper;
    
    @Inject
    public ZystemProvider(ZystemScopeHelper zystemScopeHelper) {
      this.zystemScopeHelper = zystemScopeHelper;
    }
    
    @Override
    public Zystem get() {
      return zystemScopeHelper.get();
    }
  }
  
  //TODO: make the threadlocal immutable (i.e. once something has been "set",
  //it can't be re-set or removed
  private static final class ZystemScopeHelper 
    extends InheritableThreadLocal<Zystem> {

    public ZystemScopeHelper(Zystem rootZystem) {
      set(rootZystem);
    }
  }
}
