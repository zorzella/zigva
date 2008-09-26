package com.google.zigva.java.io;

import com.google.common.base.Preconditions;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileInputStream;
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

  public static Reader from(FileInputStream in) {
    Reader result = 
      new InputStreamReader(
          Channels.newInputStream(
              in.getChannel()));
    return result;
  }

  public static Reader from(FileDescriptor in) {
    Reader result = 
        new InputStreamReader(
            Channels.newInputStream(
                new FileInputStream(in).getChannel()));
    return result;
  }
  
  public static BufferedReader buffered(InputStream in) {
    return new BufferedReader(new InputStreamReader(in));
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
        new InputStreamReader(
            Channels.newInputStream(
                new FileInputStream(in).getChannel())));
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
            throw new RuntimeException(e);
          }
          off++;
        }
        return 0;
      }
      
    };
    return result;
    
  }
  
}
