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

import com.google.zigva.collections.FilterIterator;
import com.google.zigva.lang.Lambda;

import junit.framework.TestCase;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class FilterIteratorTest extends TestCase {

  String[] SET_A_ARRAY = {
    "c",
    "a",
    "b",
    "ac",
    "ab",
  };

  List<String> SET_A_SET = Arrays.asList(SET_A_ARRAY);    

  public void testFilterIterator() throws Exception {



    Iterator<String> it = SET_A_SET.iterator();

    it = new FilterIterator<String>(it, new Lambda<String, Boolean> (){

      Set<String> filter = new HashSet<String> ();

      {
        filter.addAll(Arrays.asList(new String[] {
          "a",
          "ab",
        }));
      }

      public Boolean apply(String owner) {
        return filter.contains(owner);
      }

    });

    String[] EXPECTED = {
      "a",
      "ab",
    };
    for (String expected: EXPECTED) {
      assertTrue (expected, it.hasNext());
      assertEquals(expected, it.next());
    }
    
  
  }

  
  
}

