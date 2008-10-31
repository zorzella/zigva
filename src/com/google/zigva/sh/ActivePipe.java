package com.google.zigva.sh;

import com.google.zigva.io.Sink;
import com.google.zigva.io.Source;
import com.google.zigva.io.WriterSink;
import com.google.zigva.java.io.ReaderSource;
import com.google.zigva.java.io.Readers;
import com.google.zigva.java.io.Writers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Bad name for now -- contrary to a regular pipe, which _is_ both a Reader and
 * a Writer (and not a thread), this is an active thread that reads from a 
 * given "in" and dumps into a given "out".
 */
public class ActivePipe implements Runnable {

  private final String name;
  private final Sink<Character> out;
  private final Source<Character> in;

  public ActivePipe(String name, Source<Character> in, Sink<Character> out) {
    this.name = name;
    this.in = in;
    this.out = out;
  }
  
  public ActivePipe(String name, Source<Character> in, BufferedWriter out) {
    this(name, in, new WriterSink(out));
  }
  
  public ActivePipe(String name, Source<Character> in, OutputStream out) {
    this(name, in, Writers.buffered(out));
  }

  public ActivePipe(String name, InputStream in, Appendable out) {
    this(name, new ReaderSource(Readers.buffered(in)), Writers.buffered(out));
  }

  public ActivePipe(String name, InputStream in, Sink<Character> out) {
    this(name, new ReaderSource(Readers.buffered(in)), out);
  }

  public void run(){
//    try {
//      char[] cbuf = new char[1];
//      while (!in.ready()) {
//        Thread.sleep(500);
//      }
//      while (in.read(cbuf) != -1){
//        out.append(cbuf[0]);
//      }
      while(!in.isEndOfStream()) {
        out.write(in.read());
      }
//      out.flush();
      out.close();
//    } catch (InterruptedException e) {
//      System.out.println("foo");
//      //ok, this was interrupted
//    } catch (IOException e) {
//      throw new RuntimeException(e);
//    }
  }
  
  public ActivePipe copyWith(Source<Character> in) {
    return new ActivePipe(name + " copy with in", in, this.out);
  }

  public ActivePipe copyWith(BufferedWriter out) {
    return new ActivePipe(name + " copy with out", this.in, out);
  }
  
  public ActivePipeThread start() {
    ActivePipeThread result = new ActivePipeThread(name, this);
    result.start();
    return result;
  }
  
  public static class ActivePipeThread extends Thread {

    private final ActivePipe activePipe;

    public ActivePipeThread(String name, ActivePipe activePipe) {
      super("ActivePipeThread: " + name);
      this.activePipe = activePipe;
    }
    
    public ActivePipe getActivePipe() {
      return activePipe;
    }
    
    @Override
    public void run() {
      activePipe.run();
    }
    
    @Override
    public void interrupt() {
      activePipe.in.close();
      super.interrupt();
    }
    
  }
}
