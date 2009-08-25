// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.io;

import com.google.common.base.Preconditions;
import com.google.zigva.util.KillableCollector;

public class PumpToSink<T> implements Pump {

  private final Sink<T> sink;
  private final Source<T> source;
  private final KillableCollector toKill = new KillableCollector();

  public PumpToSink(Source<T> source, Sink<T> sink) {
    Preconditions.checkNotNull(source);
    Preconditions.checkNotNull(sink);
    this.sink = sink;
    this.source = source;
  }
  
  @Override
  public void run() {
    Source<T> in = toKill.add(source);
    Sink<T> out = toKill.add(sink);
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
