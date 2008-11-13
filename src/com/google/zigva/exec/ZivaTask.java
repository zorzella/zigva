package com.google.zigva.exec;

import com.google.zigva.lang.NamedRunnable;
import com.google.zigva.lang.Waitable;

//TODO: consider using "Runnable"/join semantics, rather than a separate "waitFor" method
//TODO: think about the name
public interface ZivaTask extends NamedRunnable, Waitable, Killable {

  void kill();
  
}
