// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.zigva.java;

import com.google.inject.Provider;
import com.google.zigva.io.FilePath;
import com.google.zigva.io.Readers;
import com.google.zigva.io.RealFileSpec;
import com.google.zigva.io.Source;
import com.google.zigva.io.Zystem;
import com.google.zigva.sh.ReaderSource;

import java.io.File;

public final class JavaZystem {

  public static Zystem get() {
    return new RealZystem(
        createIn(), 
        System.out, 
        System.err,
        getCurrentDir(), 
        getHomeDir(), System.getenv()
        );
  }

  private static final ReaderSource READER_SOURCE = 
    new ReaderSource(Readers.buffered(System.in));
  
  private static Provider<Source<Character>> createIn() {
    return new Provider<Source<Character>>() {
      @Override
      public Source<Character> get() {
        return new SourceSource<Character>(READER_SOURCE, true);
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
