package com.google.zigva.java.io;

import java.io.BufferedWriter;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.channels.Channels;

public class Writers {

  public static BufferedWriter buffered(OutputStream out) {
    return new BufferedWriter(new OutputStreamWriter(out));
  }

  public static BufferedWriter buffered(Appendable out) {
    if (out instanceof BufferedWriter) {
      return (BufferedWriter)out;
    } else {
      if (out instanceof Writer) {
        return new BufferedWriter((Writer)out);
      } else {
        return new BufferedWriter(new AppendableWriter(out));
      }
    }
  }
  
  public static BufferedWriter buffered(FileDescriptor out) {
    BufferedWriter result = new BufferedWriter(
        new OutputStreamWriter(
            Channels.newOutputStream(
                new FileOutputStream(out).getChannel())));
    return result;
  }

}
