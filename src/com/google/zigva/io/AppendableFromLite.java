package com.google.zigva.io;

public class AppendableFromLite implements Appendable {

  private final AppendableLite appendableLite;

  public AppendableFromLite(AppendableLite appendableLite) {
    this.appendableLite = appendableLite;
  }
  
  @Override
  public Appendable append(CharSequence csq) {
    if (csq == null) {
      appendNullChars();
    } else {
      for (char chr : csq.toString().toCharArray()) {
        appendableLite.append(chr);
      }
    }
    return this;
  }

  @Override
  public Appendable append(char c) {
    appendableLite.append(c);
    return this;
  }

  @Override
  public Appendable append(CharSequence csq, int start, int end) {
    if (csq == null) {
      appendNullChars();
    } else {
      append(csq.subSequence(start, end));
    }
    return this;
  }

  private AppendableLite appendNullChars() {
    return appendableLite.append('n').append('u').append('l').append('l');
  }
}
