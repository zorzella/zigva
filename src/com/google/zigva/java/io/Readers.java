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

package com.google.zigva.java.io;

import com.google.common.base.Preconditions;
import com.google.zigva.io.FilePath;
import com.google.zigva.lang.ZigvaInterruptedException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.channels.Channels;
import java.util.concurrent.BlockingQueue;

public class Readers {

//  public static Reader from(CharSequence in) {
//    return from(new String(in));
//  }
  
  public static Reader from(String in) {
    return new StringReader(in);
  }

  public static Reader from(InputStream in) {
    return new InputStreamReader(in);
  }

  public static Reader from(FilePath in) {
    return from(new File(in.getCanonicalPath()));
  }

  public static Reader from(FileInputStream in) {
    Reader result = 
      new InputStreamReader(
          Channels.newInputStream(
              in.getChannel()));
    return result;
  }

  public static Reader from(File in) {
    Reader result;
    result = new InputStreamReader(InputStreams.from(in));
    return result;
  }

//  private static InputStream is(FileDescriptor in) {
//    FileInputStream result = new FileInputStream(in);
//    if (true) {
//      return result;
//    } else {
//      // TODO: see above
//      return Channels.newInputStream(
//          result.getChannel());
//    }
//  }
  
  public static Reader from(FileDescriptor in) {
    return new InputStreamReader(InputStreams.from(in));
  }

  public static BufferedReader buffered(InputStream in) {
    return new BufferedReader(new InputStreamReader(in));
  }
  
  public static BufferedReader buffered(FilePath in) {
    return buffered(new File(in.getCanonicalPath()));
  }
  
  public static BufferedReader buffered(File in) {
    try {
      return buffered(new FileInputStream(in));
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
  
  public static BufferedReader buffered(FileInputStream in) {
    BufferedReader result = new BufferedReader(
      new InputStreamReader(
          Channels.newInputStream(
              in.getChannel())));
    return result;
  }

  public static BufferedReader buffered(FileDescriptor in) {
    BufferedReader result = new BufferedReader(
        new InputStreamReader(InputStreams.from(in)));
    return result;
  }

  public static BufferedReader buffered(Reader in) {
    if (in instanceof BufferedReader) {
      return (BufferedReader)in;
    } else {
      return new BufferedReader(in);
    }
  }
  
  public static Reader fromQueue(final BlockingQueue<Character> q) {
    Reader result = new Reader() {

      @Override
      public void close() {
      }

      @Override
      public int read(char[] cbuf, int off, int len) {
        Preconditions.checkArgument(off + len <= cbuf.length);
        while (off < cbuf.length) {
          try {
            cbuf[off] = q.take();
          } catch (InterruptedException e) {
            throw new ZigvaInterruptedException(e);
          }
          off++;
        }
        return 0;
      }
      
    };
    return result;
    
  }
  
}
