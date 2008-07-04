package com.google.zigva.java;

public class RealPropertiez implements Propertiez {

  @Override
  public String get(String key) {
    return System.getProperty(key);
  }

}
