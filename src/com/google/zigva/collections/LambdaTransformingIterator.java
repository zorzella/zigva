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
