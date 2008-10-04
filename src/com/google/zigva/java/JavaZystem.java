// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.zigva.java;

import com.google.inject.Provider;
import com.google.zigva.io.FilePath;
import com.google.zigva.io.RealFileSpec;
import com.google.zigva.io.Source;
import com.google.zigva.java.io.ReaderSource;
import com.google.zigva.java.io.Readers;
import com.google.zigva.lang.Zystem;

import java.io.File;
import java.io.FileDescriptor;

public final class JavaZystem {

  private static final Object LOCK = "System in lock";

  private static final ReaderSource READER_SOURCE = 
    new ReaderSource(Readers.buffered(FileDescriptor.in), 100, 500, LOCK);
  
  public static Zystem get() {
    return new RealZystem(
        createIn(), 
        System.out, 
        System.err,
        getCurrentDir(), 
        getHomeDir(), System.getenv()
        );
  }

  private static Provider<Source<Character>> createIn() {
    return new Provider<Source<Character>>() {
      @Override
      public Source<Character> get() {
        return new SpecialSourceSource<Character>(READER_SOURCE, LOCK);
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
