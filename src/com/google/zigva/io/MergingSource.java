// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.io;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.zigva.lang.ClusterException;
import com.google.zigva.lang.ZigvaInterruptedException;

import java.util.List;

//TODO: do or bust
@Deprecated
public class MergingSource<T> implements Source<T> {

  private final ImmutableList<Source<T>> sources;
  private final Object lock = new StringBuilder("lock");
  private boolean isClosed;
  private final List<Source<T>> eofSources = Lists.newArrayList();

  public MergingSource(Source<T>... sources) {
    this.sources = ImmutableList.of(sources);
  }
  
  public MergingSource(List<Source<T>> sources) {
    this.sources = ImmutableList.copyOf(sources);
  }

  @Override
  public void close() throws ZigvaInterruptedException {
    synchronized (lock) {
      List<RuntimeException> exceptions = Lists.newArrayList();
      for(Source<T> source: sources) {
        try {
          source.close();
        } catch (RuntimeException e) {
          exceptions.add(e);
        }
      }
      if (exceptions.size() > 0) {
        throw ClusterException.create(exceptions);
      }
      isClosed = true;
    }    
  }

  @Override
  public boolean isClosed() {
    return isClosed;
  }

  @Override
  public boolean isEndOfStream() throws DataSourceClosedException, ZigvaInterruptedException {
    return false;
  }

  @Override
  public boolean isReady() throws DataSourceClosedException {
    return false;
  }

  @Override
  public T read() throws DataNotReadyException, DataSourceClosedException, EndOfDataException {
    return null;
  }
}
