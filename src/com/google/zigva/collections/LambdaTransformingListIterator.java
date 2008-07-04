package com.google.zigva.collections;

import com.google.zigva.lang.Lambda;

import java.util.ListIterator;

public abstract class LambdaTransformingListIterator<I,O> 
    implements TransformingListIterator<I,O> {

  private ListIterator<I> iterator;
  private Lambda<I, O> lambda;

  public static <I,O> LambdaTransformingListIterator<I,O> newInstance(
      ListIterator<I> toTransform, final Lambda<I, O> function) {
    return new LambdaTransformingListIterator<I,O>(toTransform) {

      @Override
      protected Lambda<I, O> getLambda() {
        return function;
      }
    };
  }
  
  protected LambdaTransformingListIterator(ListIterator<I> toTransform) {
    this.iterator = toTransform;
    this.lambda = getLambda();
  }
  
  protected abstract Lambda<I,O> getLambda();
  
  public boolean hasNext() {
    return iterator.hasNext();
  }

  public O next() {
    return lambda.apply(iterator.next());
  }

  public void remove() {
    iterator.remove();
  }

  public boolean hasPrevious() {
    return iterator.hasPrevious();
  }

  public int nextIndex() {
    return iterator.nextIndex();
  }

  public O previous() {
    return lambda.apply(iterator.previous());
  }

  public int previousIndex() {
    return iterator.previousIndex();
  }

  public void add(O o) {
    throw new UnsupportedOperationException();
  }
  
  public void set(O o) {
    throw new UnsupportedOperationException();
  }
}
