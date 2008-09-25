package com.google.zigva.io;

import com.google.inject.Provider;
import com.google.zigva.java.Propertiez;

import java.io.Reader;
import java.util.Map;

public interface Zystem {

  Propertiez properties();
  
  Map<String,String> env();
  
  String getHostname();
  
  FilePath getCurrentDir();
  
  FilePath getHomeDir();
  
  Executor executor();

  Reader in();

  //TODO: maybe we want to do away with this method
  //TODO: rename to "in"
  Source<Character> inAsSource();

  Provider<Source<Character>> inProvider();
  
  Appendable out();
  
  Appendable err();
  
}
