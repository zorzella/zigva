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

public interface Waitable {

  /**
   * @return false if it returned because of the timeout; true if the 
   * {@link Waitable} actually completed. 
   * 
   * @throws CommandFailedException if the command failed to execute
   */
  boolean waitFor(long timeoutInMillis) throws CommandFailedException;
  
  public static final class CommandFailedException extends RuntimeException {
    public CommandFailedException(String string) {
      super(string);
    }

    public CommandFailedException(RuntimeException cause) {
      super(cause);
    }
  }
}
