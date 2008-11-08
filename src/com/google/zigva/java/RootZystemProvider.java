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
import com.google.zigva.lang.IoFactory;
import com.google.zigva.lang.Zystem;

import java.io.File;
import java.io.FileDescriptor;

public final class RootZystemProvider implements Provider<Zystem> {

  private static final Object IN_LOCK = "System in lock";
  private static final Object OUT_LOCK = "System out lock";
  private static final Object ERR_LOCK = "System err lock";

  private static final ReaderSource IN_READER_SOURCE = 
    new ReaderSource(Readers.buffered(FileDescriptor.in), 100, 500, IN_LOCK);

  private static final WriterSink OUT_WRITER_SINK = 
    new WriterSink(Writers.buffered(FileDescriptor.out), 100, 500, OUT_LOCK);

  private static final WriterSink ERR_WRITER_SINK = 
    new WriterSink(Writers.buffered(FileDescriptor.out), 100, 500, ERR_LOCK);
  
  //TODO: package private constructor?
  
  public Zystem get() {
    return new RealZystem(
        buildIoFactory(), 
        getCurrentDir(), 
        getHomeDir(),
        System.getenv(), 
        new ZigvaThreadFactory()
        );
  }
  
  private static IoFactory buildIoFactory() {
    return new IoFactory() {

      @Override
      public Sink<Character> buildErr() {
        return new SpecialSinkSink<Character>(ERR_WRITER_SINK);
      }

      @Override
      public Source<Character> buildIn() {
        return new SpecialSourceSource<Character>(IN_READER_SOURCE, IN_LOCK);
      }

      @Override
      public Sink<Character> buildOut() {
        return new SpecialSinkSink<Character>(OUT_WRITER_SINK);
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
