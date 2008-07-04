package com.google.zigva.sh;

import com.google.zigva.io.Readers;
import com.google.zigva.io.Writers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.ClosedByInterruptException;

/**
 * Bad name for now -- contrary to a regular pipe, which _is_ both a Reader and
 * a Writer (and not a thread), this is an active thread that reads from a 
 * given "in" and dumps into a given "out".
 */
public class ActivePipe implements Runnable {

  private final BufferedReader in;
  private final BufferedWriter out;
  
  public ActivePipe(BufferedReader in, BufferedWriter out) {
    this.in = in;
    this.out = out;
  }
  
  public ActivePipe(InputStream in, OutputStream out) { 
    this.in = Readers.buffered(in);
    this.out = Writers.buffered(out);
  }
  
  public ActivePipe(Reader in, OutputStream out) { 
    this.in = Readers.buffered(in);
    this.out = Writers.buffered(out);
  }

  public ActivePipe(Reader in, Writer out) { 
    this.in = Readers.buffered(in);
    this.out = Writers.buffered(out);
  }

  public ActivePipe(Reader in, Appendable out) {
    this.in = Readers.buffered(in);
    this.out = Writers.buffered(out);
  }

  public ActivePipe(InputStream in, Appendable out) {
    this.in = Readers.buffered(in);
    this.out = Writers.buffered(out);
  }

  public void run(){
    try {
      char[] cbuf = new char[1];
//      while (!in.ready()) {
//        Thread.sleep(500);
//      }
      while (in.read(cbuf) != -1){
        out.append(cbuf[0]);
      }
      out.flush();
      out.close();
//    } catch (InterruptedException e) {
//      System.out.println("foo");
//      //ok, this was interrupted
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  public ActivePipe copyWith(BufferedReader in) {
    return new ActivePipe(in, this.out);
  }

  public ActivePipe copyWith(BufferedWriter out) {
    return new ActivePipe(this.in, out);
  }
  
  public ActivePipeThread start() {
    ActivePipeThread result = new ActivePipeThread(this);
    result.start();
    return result;
  }
  
  public static class ActivePipeThread extends Thread {

    private final ActivePipe activePipe;

    public ActivePipeThread(ActivePipe activePipe) {
      this.activePipe = activePipe;
    }
    
    public ActivePipe getActivePipe() {
      return activePipe;
    }
    
    @Override
    public void run() {
      activePipe.run();
    }
  }
}
