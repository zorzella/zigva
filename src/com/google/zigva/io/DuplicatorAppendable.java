package com.google.zigva.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DuplicatorAppendable implements Appendable {

  private List<Appendable> appendables = new ArrayList<Appendable>();

  public static DuplicatorAppendable make(Appendable... appendables) {
    return new DuplicatorAppendable(appendables);
  }
  
  private DuplicatorAppendable (Appendable... appendables) {
    for (Appendable appendable: appendables) {
      if (appendable != null) {
        this.appendables.add(appendable);
      }
    }
  }

  //FIXME: make these append in separate threads
  public Appendable append(CharSequence csq) throws IOException {
    for (Appendable appendable: appendables) {
      appendable.append(csq);
    }
    return this;
  }

  public Appendable append(CharSequence csq, int start, int end)
      throws IOException {
    for (Appendable appendable: appendables) {
      appendable.append(csq, start, end);
    }
    return this;
  }

  public Appendable append(char c) throws IOException {
    for (Appendable appendable: appendables) {
      appendable.append(c);
    }
    return this;
  }

}
