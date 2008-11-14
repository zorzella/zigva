/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
