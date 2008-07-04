package com.google.zigva.io;

import com.google.inject.Injector;
import com.google.zigva.lang.Waitable;


public class ZystemStarter {

  private static final InheritableThreadLocal<Zystem> zystemThreadLocal =
    new InheritableThreadLocal<Zystem>();
  
  public Waitable bootstrap(
      final Injector injector,
      final Class<? extends Runnable> task, 
      final Zystem zystem) {
    final Thread thread = new Thread(new Runnable() {

      @Override
      public void run() {
        zystemThreadLocal.set(zystem);
        injector.getInstance(task).run();
      }
    });
    return new Waitable() {

      @Override
      public void waitFor() {
        try {
          thread.join();
        } catch (InterruptedException e) {
          // TODO choose exception wisely
          throw new RuntimeException(e);
        }
      }
    };
  }
}
