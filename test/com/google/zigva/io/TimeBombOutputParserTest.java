package com.google.zigva.io;

import com.google.zigva.io.TimeBombOutputParser;
import com.google.zigva.io.TimeBombOutputParser.TimeBombCallBack;

import junit.framework.TestCase;


public class TimeBombOutputParserTest extends TestCase {

  public static class MyCallable implements TimeBombCallBack {
    
    public boolean bombed = false;
    public boolean bombedTwice = false;

    public void call(TimeBombOutputParser parser) {
      if (bombed == true) {
        bombedTwice = true;
      }
      bombed = true;
    }
  }
  
  // It could be that on a slow system, with a weird scheduling policy, the 
  // tests will fail because this is too small.
  private static final long TIME_TO_BOMB = 25;
  private static final long INSUFFICIENT_TIME_TO_WAIT = 1;
  private static final long SUFFICIENT_TIME_TO_WAIT = TIME_TO_BOMB + 6;
  private static final long ALMOST_TIME_TO_BOMB = TIME_TO_BOMB - 5;
  @SuppressWarnings("unused")
  private static final long MINUTE = 1000 * 60;

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    if (myCallable != null) {
      assertFalse(myCallable.bombedTwice);
    }
  }
  private MyCallable myCallable;
  
  public void testItBombs() throws Exception {
    myCallable = new MyCallable();
    TimeBombOutputParser.forMillis(myCallable, TIME_TO_BOMB);
    sleep(INSUFFICIENT_TIME_TO_WAIT);
    assertFalse(myCallable.bombed);
    sleep(SUFFICIENT_TIME_TO_WAIT);
    assertTrue(myCallable.bombed);
  }

  public void testDeactivate() throws Exception {
    myCallable = new MyCallable();
    TimeBombOutputParser parser = TimeBombOutputParser.forMillis(myCallable, 
      TIME_TO_BOMB);
    sleep(INSUFFICIENT_TIME_TO_WAIT);
    assertFalse(myCallable.bombed);
    parser.deactivate();
    sleep(SUFFICIENT_TIME_TO_WAIT);
    assertFalse(myCallable.bombed);
  }

  public void testItBombsOnlyOnce() throws Exception {
    myCallable = new MyCallable();
    TimeBombOutputParser.forMillis(myCallable, TIME_TO_BOMB);
    sleep(INSUFFICIENT_TIME_TO_WAIT);
    assertFalse(myCallable.bombedTwice);
    sleep(10 * SUFFICIENT_TIME_TO_WAIT);
    assertFalse(myCallable.bombedTwice);
  }
  
  // THIS TEST IS FAILLING!
  public void testOutputResetsBomb() throws Exception {
    myCallable = new MyCallable();
    TimeBombOutputParser parser = TimeBombOutputParser.forMillis(myCallable, TIME_TO_BOMB);
    long lastWhenToBomb = 0;
    long whenToBomb;
    for (int i=0;i<1500;i++) {
      sleep(ALMOST_TIME_TO_BOMB);
      whenToBomb = parser.getWhenToBomb();
      long currentTimeMillis = System.currentTimeMillis();
      assertTrue(i + " " + whenToBomb + ", " + lastWhenToBomb, whenToBomb >= lastWhenToBomb + ALMOST_TIME_TO_BOMB);
      assertTrue(i + " " + whenToBomb + " > " + currentTimeMillis, whenToBomb > currentTimeMillis);
      assertFalse(i + "", myCallable.bombed);
      lastWhenToBomb = whenToBomb;
      parser.append('a');
    }
    sleep(SUFFICIENT_TIME_TO_WAIT);
    assertTrue(myCallable.bombed);
  }
  
  public void testDeactivateKillsThread() throws Exception {
    int activeThreadCount = Thread.activeCount();
    TimeBombCallBack t = new MyCallable();
    TimeBombOutputParser parser = TimeBombOutputParser.forSeconds(t, 10);
    assertTrue(Thread.activeCount() > activeThreadCount);
    parser.deactivate();
    sleep(100);
    assertEquals (activeThreadCount, Thread.activeCount());
  }  
  private void sleep(long millis) throws InterruptedException {
    Thread.sleep(millis);
  }

//  public void testItBombsForMinutes() throws Exception {
//    myCallable = new MyCallable();
//    TimeBombOutputParser.forMinutes(myCallable, TIME_TO_BOMB);
//    sleep(ALMOST_TIME_TO_BOMB * MINUTE);
//    assertFalse(myCallable.bombed);
//    sleep(SUFFICIENT_TIME_TO_WAIT * MINUTE);
//    assertTrue(myCallable.bombed);
//    sleep(3 * SUFFICIENT_TIME_TO_WAIT * MINUTE);
//  }


}
