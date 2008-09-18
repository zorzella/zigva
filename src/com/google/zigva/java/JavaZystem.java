// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.zigva.java;

import com.google.inject.Provider;
import com.google.zigva.io.FilePath;
import com.google.zigva.io.Readers;
import com.google.zigva.io.RealFileSpec;
import com.google.zigva.io.Source;
import com.google.zigva.io.Zystem;

import java.io.File;

public final class JavaZystem {

  public static Zystem get() {
    return new RealZystem(
        createIn(), 
        Readers.from(System.in), 
        System.out, 
        System.out,
        getCurrentDir(), 
        getHomeDir(), System.getenv()
        );
  }

  private static final InputStreamSource INPUT_STREAM_SOURCE = 
    new InputStreamSource(System.in);
  
  private static Provider<Source> createIn() {
    return new Provider<Source>() {
      @Override
      public Source get() {
        return new SourceSource(INPUT_STREAM_SOURCE, true);
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
