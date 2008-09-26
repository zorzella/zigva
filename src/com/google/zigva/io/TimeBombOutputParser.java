package com.google.zigva.io;

import java.io.IOException;

public class TimeBombOutputParser implements Appendable {

  public interface TimeBombCallBack {

    public void call(TimeBombOutputParser parser);

  }
  
  private final class TimeBombThread implements Runnable {
    private final TimeBombCallBack trigger;
    private final TimeBombOutputParser parser;

    private TimeBombThread(TimeBombOutputParser parser, TimeBombCallBack trigger) {
      this.parser = parser;
      this.trigger = trigger;
    }

    public void run() {
      while (active) {
        long remainingTimeTilBomb = getRemainingTimeTilBomb();
        try {
          if (remainingTimeTilBomb < 0) {
            trigger.call(parser);
            deactivate();
          } else {
            Thread.sleep(remainingTimeTilBomb);
          }
        } catch (InterruptedException ignore) {
          // Ignore
        }
      }
    }

  }

  private static final int SECONDS = 1000; // millis
  private static final int MINUTES = 60 * SECONDS;

  private long lastAppendEventAt;
  private boolean active = true;
  private Thread thread;
  private final long millis;
  
  public long getLastAppendEventAt () {
    return lastAppendEventAt;
  }
  
  public long getWhenToBomb() {
    long result = lastAppendEventAt + millis;
    return result;
  }
  
  public long getRemainingTimeTilBomb() {
    long whenToBomb = lastAppendEventAt + millis;
    long now = System.currentTimeMillis();
    long result = whenToBomb - now;
    return result;
  }
  
  private void touch() {
    lastAppendEventAt = System.currentTimeMillis();
  }

  public void deactivate () {
    active = false;
    if (thread != null) {
      thread.interrupt();
    }
  }
  
  private TimeBombOutputParser(final TimeBombCallBack toTrigger, final long millis) {
    this.millis = millis;
    touch();
    thread = new Thread(new TimeBombThread(this, toTrigger));
    thread.start();
  }

  public static TimeBombOutputParser forMinutes(TimeBombCallBack toTrigger,
      long minutes) {
    long millis = minutes * MINUTES;
    return new TimeBombOutputParser(toTrigger, millis);
  }

  public static TimeBombOutputParser forSeconds(TimeBombCallBack toTrigger,
      long seconds) {
    long millis = seconds * SECONDS;
    return new TimeBombOutputParser(toTrigger, millis);
  }

  public static TimeBombOutputParser forMillis(TimeBombCallBack toTrigger,
      long millis) {
    return new TimeBombOutputParser(toTrigger, millis);
  }

  public Appendable append(CharSequence csq) throws IOException {
    touch();
    return this;
  }

  public Appendable append(char c) throws IOException {
    touch();
    return this;
  }

  public Appendable append(CharSequence csq, int start, int end) throws IOException {
    touch();
    return this;
  }
  
}
