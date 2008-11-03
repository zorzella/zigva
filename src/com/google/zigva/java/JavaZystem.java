// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.zigva.java;

import com.google.inject.Provider;
import com.google.zigva.guice.ZigvaThreadFactory;
import com.google.zigva.io.FilePath;
import com.google.zigva.io.RealFileSpec;
import com.google.zigva.io.Sink;
import com.google.zigva.io.Source;
import com.google.zigva.io.WriterSink;
import com.google.zigva.java.io.ReaderSource;
import com.google.zigva.java.io.Readers;
import com.google.zigva.java.io.Writers;
import com.google.zigva.lang.Zystem;

import java.io.File;
import java.io.FileDescriptor;

public final class JavaZystem {

  private static final Object IN_LOCK = "System in lock";
  private static final Object OUT_LOCK = "System out lock";
  private static final Object ERR_LOCK = "System err lock";

  private static final ReaderSource IN_READER_SOURCE = 
    new ReaderSource(Readers.buffered(FileDescriptor.in), 100, 500, IN_LOCK);

  private static final WriterSink OUT_WRITER_SINK = 
    new WriterSink(Writers.buffered(FileDescriptor.out), 100, 500, OUT_LOCK);

  private static final WriterSink ERR_WRITER_SINK = 
    new WriterSink(Writers.buffered(FileDescriptor.out), 100, 500, ERR_LOCK);
  
  public static Zystem get() {
    return new RealZystem(
        createIn(), 
        createOut(),
        createErr(), 
        getCurrentDir(), 
        getHomeDir(),
        System.getenv(), 
        new ZigvaThreadFactory()
        );
  }

  private static Provider<Sink<Character>> createOut() {
    return new Provider<Sink<Character>>() {
      @Override
      public Sink<Character> get() {
        return new SpecialSinkSink<Character>(OUT_WRITER_SINK);//, OUT_LOCK);
      }
    };
  }

  private static Provider<Sink<Character>> createErr() {
    return new Provider<Sink<Character>>() {
      @Override
      public Sink<Character> get() {
        return new SpecialSinkSink<Character>(ERR_WRITER_SINK);//, ERR_LOCK);
      }
    };
  }

  private static Provider<Source<Character>> createIn() {
    return new Provider<Source<Character>>() {
      @Override
      public Source<Character> get() {
        return new SpecialSourceSource<Character>(IN_READER_SOURCE, IN_LOCK);
      }
    };
  }

  private static RealFileSpec getCurrentDir() {
    return new RealFileSpec(new File("."));
  }
  
  private static FilePath getHomeDir() {
    return new RealFileSpec(new File(System.getProperty("user.home")));
  }
}
