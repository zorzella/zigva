package com.google.zigva.collections;

import com.google.zigva.lang.Lambda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Methods to transform a collection of elements into a list. Each element of 
 * the original collection will be transformed by a ZFunction into a collection
 * of elements in the transformed list. For the simpler case -- where the 
 * ZFunction returns a single element rather than a collection -- you may use
 * {@link Transformed}.
 */
public class AllTransformed {

  public static <I, O> List<O> list(
    I[] source, Lambda<I, ? extends Iterable<O>> function) {
    return list(Arrays.asList(source), function);
  }

  public static <I, O> List<O> list(
      List<? extends I> source, Lambda<I, ? extends Iterable<O>> function) {
    return list(source.iterator(), function);
  }

  public static <I, O> List<O> list(
      Iterable<? extends I> source, final Lambda<I, ? extends Iterable<O>> function) {
    List<O> result = new ArrayList<O>();
    for (I item: source) {
      for (O transformed : function.apply(item)) {
        result.add(transformed);
      }
    }
    return result;
  }
  
  public static <I, O> List<O> list(
      Iterator<? extends I> source, final Lambda<I, ? extends Iterable<O>> function) {
    List<O> result = new ArrayList<O>();
    while(source.hasNext()) {
      for (O transformed : function.apply(source.next())) {
        result.add(transformed);
      }
    }
    return result;
  }

}
