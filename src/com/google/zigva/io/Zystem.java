package com.google.zigva.io;

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
  
  Appendable out();
  
  Appendable err();
  
}
