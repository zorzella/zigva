package com.google.zigva.io;

import com.google.zigva.java.Propertiez;

import java.io.Reader;
import java.util.Map;

public interface Zystem {

  Propertiez properties();
  
  Map<String,String> env();
  
  String getHostname();
  
  FilePath getCurrentDir();
  
  //TODO(zorzella): make this class immutable again
  void setCurrentDir(FilePath newCurrentDir);

  FilePath getHomeDir();
  
  Executor executor();

//  Appendable out();

  Reader in();
  
  Appendable out();
  
  Appendable err();
  
}
