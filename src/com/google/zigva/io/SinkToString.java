package com.google.zigva.io;

public class SinkToString implements Sink<Character> {

  private final StringBuilder data = new StringBuilder();

  @Override
  public void close() {
  }

  @Override
  public boolean isReady() throws DataSourceClosedException {
    return true;
  }

  @Override
  public void write(Character c) throws DataSourceClosedException {
    data.append(c);
  }
  
  @Override
  public String toString() {
    return data.toString();
  }

  public String asString() {
    return data.toString();
  }

  @Override
  public void flush() {
  }
  
  public PumpFactory<Character> asPumpFactory() {
    return new PumpFactory<Character>(){

      @Override
      public Pump getPumpFor(Source<Character> source) {
        return new PumpToSink<Character>(source, SinkToString.this);
      }
    };
  }
  
  //TODO: think -- should equals to its content return true?
}
