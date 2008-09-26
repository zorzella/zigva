package com.google.zigva.java.io;


import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

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
}
