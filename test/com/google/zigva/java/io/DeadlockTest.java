package com.google.zigva.java.io;

import junit.framework.TestCase;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DeadlockTest extends TestCase {

  public void testDeadlock() {
    new Deadlocker().deadlock();
  }
  
  private static class Deadlocker {

    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    private final Runnable runnable = new Runnable() {

      @Override
      public void run() {
        // 2. (might or not happen). Second thread tries to acquire lock, but can't
        // 4. this acquires "lock"
//        lock.lock();
        synchronized(lock) {
        try {
          // I'm avoiding a full deadlock by awating only 10 seconds. 
          synchronized(this) { 
            this.wait(10000); 
          }
        } catch (InterruptedException e) {
          throw new RuntimeException();
        } finally {
//          lock.unlock();
        }
      }
      }
    };
    
    private void deadlock() {

      // 1. Main thread acquired a new-style lock
//      lock.lock();
     
      synchronized(lock) {
      try {
        Thread result = new Thread(runnable);
        result.start();
        // 3. by "await"ing, main thread allows the secondary to enter 
//        condition.await(500, TimeUnit.MILLISECONDS);
        lock.wait(500);
        // 5. The "await" does not return within the 0.5 seconds it's supposed to
        // Instead, this will only finish after the "run" method unlocks
        System.out.println("FOO");
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      } finally {
//        lock.unlock();
      }
    }
    }
  }
}
