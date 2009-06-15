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
import com.google.zigva.io.DataSourceClosedException;
import com.google.zigva.io.Sink;
import com.google.zigva.io.Source;
import com.google.zigva.lang.ClusterException;
import com.google.zigva.lang.Killable;

import java.util.List;

public class Killables {

  public static Killable of(final Killable... killables) {
    return of(Lists.newArrayList(killables));
  }
  
  public static Killable of(final Sink<?> sink) {
    return new Killable() {
      @Override
      public void kill() {
        try {
          sink.close();
        } catch (DataSourceClosedException ignore) {
        } catch (RuntimeException e) {
          throw new FailedToKillException(e);
        }
      }
    };
  }

  public static Killable of(final Source<?> source) {
    return new Killable() {
      @Override
      public void kill() {
        try {
          source.close();
        } catch (DataSourceClosedException ignore) {
        } catch (RuntimeException e) {
          throw new FailedToKillException(e);
        }
      }
    };
  }

  public static Killable of(final Iterable<Killable> killables) {
    return new Killable() {
      @Override
      public void kill() {
        List<RuntimeException> exceptions = Lists.newArrayList();
        for (Killable killable : killables) {
          try {
            killable.kill();
          } catch (RuntimeException e) {
            exceptions.add(e);
          }
        }
        if (exceptions.size() > 0) {
          throw ClusterException.create(exceptions);
        }
      }
    };
  }
}
