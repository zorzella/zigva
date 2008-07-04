package com.google.zigva.collections;

import com.google.zigva.lang.Lambda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Methods to transform a collection of elements into a list. Each element of 
 * the original collection will be transformed by a ZFunction into an element 
 * of the transformed list. If you need each element to be transformed to 
 * multiple entries in the result, use {@link AllTransformed}.
 */
public class Transformed {

  public static <I, O> List<O> list(
    I[] source, Lambda<I, O> function) {
    return list(Arrays.asList(source), function);
  }

  public static <I, O> List<O> list(
      List<? extends I> source, Lambda<I, O> function) {
    return list(source.iterator(), function);
  }

  public static <I, O> List<O> list(
      Iterable<? extends I> source, final Lambda<I, O> function) {
    List<O> result = new ArrayList<O>();
    for (I item: source) {
      result.add(function.apply(item));
    }
    return result;
  }
  
  public static <I, O> List<O> list(
      Iterator<? extends I> source, final Lambda<I, O> function) {
    List<O> result = new ArrayList<O>();
    while(source.hasNext()) {
      result.add(function.apply(source.next()));
    }
    return result;
  }

}
