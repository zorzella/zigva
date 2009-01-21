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

package com.google.zigva.java;

import com.google.inject.Provider;
import com.google.zigva.guice.ZigvaThreadFactory;
import com.google.zigva.io.AppendablePassiveSink;
import com.google.zigva.io.FilePath;
import com.google.zigva.io.Sink;
import com.google.zigva.io.RealFileSpec;
import com.google.zigva.io.SimpleSink;
import com.google.zigva.io.PassiveSink;
import com.google.zigva.io.Source;
import com.google.zigva.java.io.ReaderSource;
import com.google.zigva.java.io.Readers;
import com.google.zigva.java.io.Writers;
import com.google.zigva.lang.SourceFactory;
import com.google.zigva.lang.IoFactory;
import com.google.zigva.lang.SinkFactory;
import com.google.zigva.lang.UserInfo;
import com.google.zigva.lang.Zystem;

import java.io.File;
import java.io.FileDescriptor;

public final class RootZystemProvider implements Provider<Zystem> {

	private static final ZigvaThreadFactory ROOT_THREAD_FACTORY = new ZigvaThreadFactory();

  private static final class RootIoFactory implements IoFactory {
    
    private final SourceFactory<Character> in = new SourceFactory<Character>() {
    
      @Override
      public Source<Character> build() {
        if (false) {
          return new SpecialSourceSource<Character>(IN_READER_SOURCE, IN_LOCK);
        }
        return new SourceAtEOS<Character>();
      }
    };

    private final SinkFactory<Character> out = new SinkFactory<Character>() {
    
      @Override
      public Sink build(Source<Character> source) {
        return new SimpleSink<Character>(source, new SpecialPassiveSinkSink<Character>(OUT_WRITER_SINK));
      }
    };

    private final SinkFactory<Character> err = new SinkFactory<Character>() {
      
      @Override
      public Sink build(Source<Character> source) {
        return new SimpleSink<Character>(source, new SpecialPassiveSinkSink<Character>(ERR_WRITER_SINK));
      }
    };
    
//    @Override
//    public boolean redirectErrToOut() {
//      return false;
//    }

    @Override
    public SourceFactory<Character> in() {
      return in;
    }

    @Override
    public SinkFactory<Character> out() {
      return out;
    }
    
    @Override
    public SinkFactory<Character> err() {
      return err;
    }
  }

  private static final Object IN_LOCK = new StringBuilder("System in lock");
  private static final Object OUT_LOCK = new StringBuilder("System out lock");
  private static final Object ERR_LOCK = new StringBuilder("System err lock");

  private static final ReaderSource IN_READER_SOURCE = 
    new ReaderSource.Builder(ROOT_THREAD_FACTORY).withCombo(100, 500, IN_LOCK)
      .create(Readers.buffered(FileDescriptor.in));

  private static final AppendablePassiveSink OUT_WRITER_SINK = 
    new AppendablePassiveSink.Builder(ROOT_THREAD_FACTORY).withCombo( 
        100, 500, OUT_LOCK).create(Writers.buffered(FileDescriptor.out));

  private static final AppendablePassiveSink ERR_WRITER_SINK = 
    new AppendablePassiveSink.Builder(ROOT_THREAD_FACTORY).withCombo( 
        100, 500, ERR_LOCK).create(Writers.buffered(FileDescriptor.out));
  
  //TODO: package private constructor?
  
  public Zystem get() {
    return new RealZystem(
        buildIoFactory(), 
        getCurrentDir(), 
        getHomeDir(),
        new UserInfo(System.getProperty("user.name")), 
        System.getenv()
        );
  }
  
  private static IoFactory buildIoFactory() {
    return new RootIoFactory();
  }

  private static RealFileSpec getCurrentDir() {
    return new RealFileSpec(new File("."));
  }
  
  private static FilePath getHomeDir() {
    return new RealFileSpec(new File(System.getProperty("user.home")));
  }
}
