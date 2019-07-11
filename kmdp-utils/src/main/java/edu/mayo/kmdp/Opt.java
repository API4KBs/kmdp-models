/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.mayo.kmdp;


public class Opt<T> {

  private final String name;
  private final String defaultValue;
  private final String definition;
  private final Class<T> type;
  private boolean required;

  public static <T> Opt of(String name, String defaultValue, String definition, Class<T> type, boolean required) {
    return new Opt<>(name, defaultValue, definition, type, required);
  }

  public static <T> Opt of(String name, String defaultValue, Class<T> type, boolean required) {
    return of(name, defaultValue, "", type, required);
  }

  private Opt(String name, String defaultValue, String definition, Class<T> type, boolean required) {
    this.name = name;
    this.defaultValue = defaultValue;
    this.definition = definition;
    this.type = type;
    this.required = required;
  }

  public String getName() {
    return name;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public Class<T> getType() {
    return type;
  }

  public String getDefinition() {
    return definition;
  }

  public boolean isRequired() {
    return required;
  }
}
