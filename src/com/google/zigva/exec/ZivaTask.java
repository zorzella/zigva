package com.google.zigva.exec;

import com.google.zigva.lang.NamedRunnable;

//TODO: think about the name
public interface ZivaTask extends NamedRunnable, Killable {

  void kill();
  
}
