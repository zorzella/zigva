package com.google.zigva.lang;

import com.google.zigva.io.FilePath;
import com.google.zigva.java.Propertiez;

import java.util.Map;

public interface Zystem {

  Propertiez properties();
  
  Map<String,String> env();
  
  String getHostname();
  
  FilePath getCurrentDir();
  
  FilePath getHomeDir();
  
  IoFactory ioFactory();
}
