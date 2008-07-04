package com.google.zigva.util;

public class Pair<T1, T2> {

  T1 a;
  T2 b;
  
  public Pair(T1 a, T2 b) {
    this.a = a;
    this.b = b;
  }
  
  public T1 getA() {
    return this.a;
  }
  
  public T2 getB() {
    return this.b;
  }

  public static <T1,T2> Pair<T1,T2> getInstace(T1 a, T2 b) {
    return new Pair<T1,T2>(a,b);
  }

}
