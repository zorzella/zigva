// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.exec;

import com.google.common.collect.Lists;
import com.google.zigva.io.Sink;
import com.google.zigva.io.Source;

import java.util.List;

public class KillableCollector {

  private final List<Killable> toKill = Lists.newArrayList();

  public <T extends Killable> T add(T killable) {
    toKill.add(killable);
    return killable;
  }

  public <T> Source<T> add(Source<T> source) {
    toKill.add(Killables.of(source));
    return source;
  }

  public <T> Sink<T> add(Sink<T> sink) {
    toKill.add(Killables.of(sink));
    return sink;
  }
  
  public Killable killable() {
    return Killables.of(toKill);
  }
}
