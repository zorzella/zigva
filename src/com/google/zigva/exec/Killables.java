// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.exec;

import com.google.common.collect.Lists;
import com.google.zigva.io.DataSourceClosedException;
import com.google.zigva.io.Sink;
import com.google.zigva.io.Source;
import com.google.zigva.lang.ExceptionCollection;

import java.util.List;

public class Killables {

  public static Killable of(final Killable... killables) {
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
          throw ExceptionCollection.create(exceptions);
        }
      }
    };
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
}
