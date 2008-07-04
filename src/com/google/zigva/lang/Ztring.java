package com.google.zigva.lang;

import java.util.Arrays;
import java.util.Iterator;

public class Ztring {
  
  public static String join(CharSequence separator, Iterable<?> it){
    return join(separator, it.iterator());
  }
  
  public static String join(CharSequence separator, Iterator<?> it){
    if (it == null) {
      return null;
    }
    StringBuilder result = new StringBuilder();
    if (it.hasNext()) {
      result .append(it.next());
    }
    while(it.hasNext()) {
      result.append(separator);
      result.append(it.next());
    }
    return result.toString();
    
  }
  
  public static String join(CharSequence separator, Object[] args){
    return join(separator, Arrays.asList(args).iterator());
  }
  
  public static String joinVar(CharSequence separator, Object... args){
    return join(separator, args);
  }
  
}
