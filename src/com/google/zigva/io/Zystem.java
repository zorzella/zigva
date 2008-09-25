package com.google.zigva.io;

import com.google.inject.Provider;
import com.google.zigva.java.Propertiez;

import java.util.Map;

public interface Zystem {

  Propertiez properties();
  
  Map<String,String> env();
  
  String getHostname();
  
  FilePath getCurrentDir();
  
  FilePath getHomeDir();
  
  Executor executor();

  Provider<Source<Character>> in();
  
  Appendable out();
  
  Appendable err();
  
}
