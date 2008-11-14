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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public abstract class LambdaTransformingList<O> implements List<O> {

  public static <O> LambdaTransformingList<O> newInstance(
      final Lambda<O, O> function) {

    return new LambdaTransformingList<O>() {
      @Override
      protected Lambda<O, O> getLambda() {
        return function;
      }
    };
  }

  protected LambdaTransformingList() {
    this.l = getLambda();
  }

  protected abstract Lambda<O, O> getLambda();
  private Lambda<O, O> l;

  private List<O> list = new ArrayList<O>();

  public boolean add(O o) {
    return list.add(l.apply(o));
  }

  public void add(int index, O element) {
    list.add(index, l.apply(element));
  }

  public boolean addAll(Collection<? extends O> c) {
    List<O> transformed = Transformed.list(c, l);
    return list.addAll(transformed);
  }

  public boolean addAll(int index, Collection<? extends O> c) {
    List<O> transformed = Transformed.list(c, l);
    return list.addAll(index, transformed);
  }

  public void clear() {
    list.clear();
  }

  public boolean contains(Object o) {
    return list.contains(o);
  }

  public boolean containsAll(Collection<?> c) {
    return list.containsAll(c);
  }

  public O get(int index) {
    return list.get(index);
  }

  public int indexOf(Object o) {
    return list.indexOf(o);
  }

  public boolean isEmpty() {
    return list.isEmpty();
  }

  public Iterator<O> iterator() {
    return list.iterator();
  }

  public int lastIndexOf(Object o) {
    return list.lastIndexOf(o);
  }

  public ListIterator<O> listIterator() {
    return list.listIterator();
  }

  public ListIterator<O> listIterator(int index) {
    return list.listIterator(index);
  }

  public boolean remove(Object o) {
    // FIXME -- implement for BiLambdas
    throw new UnsupportedOperationException();
//    return list.remove(o);
  }

  public O remove(int index) {
    // FIXME -- implement for BiLambdas
    throw new UnsupportedOperationException();
//    return list.remove(index);
  }

  public boolean removeAll(Collection<?> c) {
    // FIXME -- implement for BiLambdas
    throw new UnsupportedOperationException();
//    return list.removeAll(c);
  }

  public boolean retainAll(Collection<?> c) {
    // FIXME -- implement for BiLambdas
    throw new UnsupportedOperationException();
//    return list.retainAll(c);
  }

  public O set(int index, O element) {
    // FIXME -- implement for BiLambdas
    throw new UnsupportedOperationException();
//    return list.set(index, element);
  }

  public int size() {
    return list.size();
  }

  public List<O> subList(int fromIndex, int toIndex) {
    List<O> result = LambdaTransformingList.newInstance(getLambda());
    result.addAll(list.subList(fromIndex, toIndex));
    return result;
  }

  public Object[] toArray() {
    return list.toArray();
  }

  public <T> T[] toArray(T[] a) {
    return list.toArray(a);
  }
}
