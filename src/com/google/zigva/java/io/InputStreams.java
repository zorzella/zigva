// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.java.io;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.channels.Channels;

public class InputStreams {

  public static InputStream from(FileInputStream in) {
    InputStream result = Channels.newInputStream(
        in.getChannel());
    return result;
  }

  public static InputStream from(FileDescriptor in) {
    InputStream result = Channels.newInputStream(
        new FileInputStream(in).getChannel());
    return result;
  }
  

}
