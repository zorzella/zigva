package com.google.zigva.collections;

import com.google.zigva.lang.Lambda;

import java.util.Iterator;

public abstract class LambdaTransformingIterator<I,O> 
    implements TransformingIterator<I,O> {

  private Iterator<I> iterator;
  private Lambda<I, O> lambda;

  public static <I,O> LambdaTransformingIterator<I,O> newInstance(
      Iterator<I> toTransform, final Lambda<I, O> function) {
    return new LambdaTransformingIterator<I,O>(toTransform) {

      @Override
      protected Lambda<I, O> getLambda() {
        return function;
      }
    };
  }
  
  protected LambdaTransformingIterator(Iterator<I> toTransform) {
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

}
