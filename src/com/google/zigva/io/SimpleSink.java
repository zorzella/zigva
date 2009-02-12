// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.io;

import com.google.zigva.util.KillableCollector;

public class SimpleSink<T> implements Sink {

  private final PassiveSink<T> sink;
  private final Source<T> source;
  private final KillableCollector toKill = new KillableCollector();

  public SimpleSink(Source<T> source, PassiveSink<T> sink) {
    this.sink = sink;
    this.source = source;
  }
  
  @Override
  public void run() {
    Source<T> in = toKill.add(source);
    PassiveSink<T> out = toKill.add(sink);
    while (!in.isEndOfStream()) {
      out.write(in.read());
    }
    out.flush();
    out.close();
    in.close();
  }

  @Override
  public void kill() {
    toKill.killable().kill();
  }
  
  @Override
  public String toString() {
    return source + "->" + sink;
  }
}
