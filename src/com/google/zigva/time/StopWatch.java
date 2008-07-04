package com.google.zigva.time;

public class StopWatch {

  private long startTime;
  private long stopTime;

  private StopWatch() {
    startTime = System.currentTimeMillis();
  }

  public static StopWatch start() {
    return new StopWatch();
  }

  
  public StopWatch stop() {
    stopTime = System.currentTimeMillis();
    return this;
  }

  public long millis() {
    return stopTime - startTime;
  }

  public long seconds() {
    return (stopTime - startTime) / 1000;    
  }
  
}
