package com.google.zigva.exec;

import com.google.zigva.lang.Waitable;

//TODO: think about the name
public interface ZivaTask extends Waitable {

  void kill();
  
}
