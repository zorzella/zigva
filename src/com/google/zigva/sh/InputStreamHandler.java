package com.google.zigva.sh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class InputStreamHandler implements Runnable {

  private final BufferedReader in;
  private final Appendable out;
  
  public InputStreamHandler(InputStream in, Appendable appendable){ 
    this.out = appendable;
    this.in = new BufferedReader(new InputStreamReader(in));
  }
  
  public void run(){
    try {
      char[] cbuf = new char[1];
      while (in.read(cbuf) != -1){
        out.append(cbuf[0]);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
