package com.google.zigva.lang;

public interface Lambda<I,O> {

  O apply(I object);
}
