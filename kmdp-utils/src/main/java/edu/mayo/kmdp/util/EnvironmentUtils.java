/*
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp.util;

import java.util.Optional;

/**
 * Helper class to extract property values from {@link System#getenv()}
 */
public class EnvironmentUtils {

  private EnvironmentUtils() {
    // functions only
  }

  /**
   * @param key a system property identifier
   * @return an Optional value for the key
   */
  public static Optional<String> getProperty(String key) {
    return Optional.ofNullable(System.getenv(key));
  }

  /**
   * @param key a system property identifier
   * @return the value for the key, or defaultValue if not found
   */
  public static String getProperty(String key, String defaultValue) {
    return getProperty(key).orElse(defaultValue);
  }

  /**
   * @param key a system property identifier
   * @return the value for the key, throwing an exception if not found
   * @throws Exception if not found
   */
  public static String getRequiredProperty(String key) {
    return getProperty(key)
        .orElseThrow(() -> {
          var message = String.format("Required environment property '%s' is empty", key);
          return new IllegalStateException(message);
        });
  }

}
