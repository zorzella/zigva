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

package com.google.zigva.lang;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;

/**
 * An {@link ClusterException} is data structure that allows for some code to
 * "throw multiple exceptions", or something close to it. The prototypical code
 * that calls for this class is presented below:
 * 
 * <pre>
 * void runManyThings(List&lt;ThingToRun&gt; thingsToRun) {
 *   for (ThingToRun thingToRun : thingsToRun) {
 *     thingToRun.run(); // <-- say this may throw an exception, but you want to
 *                       // always run all thingsToRun
 *   }
 * }
 * </pre>
 * 
 * This is what the code would become:
 * 
 * <pre>
 * void runManyThings(List&lt;ThingToRun&gt; thingsToRun) {
 *   List&lt;Exception&gt; exceptions = Lists.newArrayList();
 *   for (ThingToRun thingToRun : thingsToRun) {
 *     try {
 *       thingToRun.run();
 *     } catch (Exception e) {
 *       exceptions.add(e);
 *     }
 *   }
 *   if (exceptions.size() > 0) {
 *     throw ExceptionCluster.create(exceptions);
 *   }
 * }
 * </pre>
 * 
 * <p>See semantic details at {@link #create(Collection)}.
 * 
 * @author Luiz-Otavio Zorzella
 */
public final class ClusterException extends RuntimeException {

  public final Collection<Exception> exceptions;
  
  private ClusterException(Collection<? extends Exception> exceptions) {
    super(String.format(
        "%d exceptions were thrown. The first exception is listed as a cause.", 
        exceptions.size()), exceptions.iterator().next());
    this.exceptions = Collections.unmodifiableCollection(Lists.newArrayList(exceptions));
  }

  /**
   * @see #create(Collection)
   */
  public static RuntimeException create(Exception... exceptions) {
    return create(Lists.newArrayList(exceptions));
  }
  
  /**
   * Given a collection of exceptions, returns a {@link RuntimeException}, with
   * the following rules:
   * 
   * <ul>
   *  <li>If {@code exceptions} has a single exception and that exception is a
   *    {@link RuntimeException}, return it
   *  <li>If {@code exceptions} has a single exceptions and that exceptions is
   *    <em>not</em> a {@link RuntimeException}, return a simple 
   *    {@code RuntimeException} that wraps it
   *  <li>Otherwise, return an instance of {@link ClusterException} that wraps 
   *    the first exception in the {@code exceptions} collection.
   * </ul>
   * 
   * <p>Though this method takes any {@link Collection}, it often makes most 
   * sense to pass a {@link java.util.List} or some other collection that 
   * preserves the order in which the exceptions got added.
   * 
   * @throws NullPointerException if {@code exceptions} is null
   * @throws IllegalArgumentException if {@code exceptions} is empty
   */
  public static RuntimeException create(Collection<? extends Exception> exceptions) {
    if (exceptions.size() == 0) {
      throw new IllegalArgumentException(
          "Can't create an ExceptionCollection with no exceptions");
    }
    if (exceptions.size() == 1) {
      Exception temp = exceptions.iterator().next();
      if (temp instanceof RuntimeException) {
        return (RuntimeException)temp;
      } else {
        return new RuntimeException(temp);
      }
    }
    return new ClusterException(exceptions);
  }
}