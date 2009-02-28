/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zigva.util;

import com.google.common.collect.Lists;
import com.google.zigva.io.Sink;
import com.google.zigva.io.Source;
import com.google.zigva.lang.Killable;

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
