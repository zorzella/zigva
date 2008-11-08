package com.google.zigva.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.zigva.io.FileRepository;
import com.google.zigva.java.RealFileRepository;
import com.google.zigva.java.RootZystemProvider;
import com.google.zigva.lang.Zystem;
import com.google.zigva.sh.JavaProcessExecutor;
import com.google.zigva.sh.RealJavaProcessExecutor;

public class ZivaModule extends AbstractModule {

  private final Provider<Zystem> zystemProvider;

  public ZivaModule() {
    zystemProvider = new ZystemProvider();
  }

  public ZivaModule(Provider<Zystem> zystemProvider) {
    this.zystemProvider = zystemProvider;
  }

  @Override
  protected void configure() {
    install(new JavaModule());
//    ZystemScopeHelper zystemScopeHelper = new ZystemScopeHelper(rootZystem);
    bind(FileRepository.class).to(RealFileRepository.class);
    bind(Zystem.class).toProvider(zystemProvider).in(Scopes.SINGLETON);
    bind(JavaProcessExecutor.class).to(RealJavaProcessExecutor.class);
//    bind(ZystemScopeHelper.class).toInstance(zystemScopeHelper);
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
