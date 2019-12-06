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
package edu.mayo.kmdp.idl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Interface {

  private Map<String,Operation> operations = new HashMap<>();
  private String name;

  public Interface(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public Interface addOperation(Operation op) {
    if (operations.containsKey(op.getName())) {
      throw new UnsupportedOperationException("Cannot merge interfaces yet");
    }
    operations.put(op.getName(), op);
    return this;
  }

  public Collection<Operation> getOperations() {
    return operations.values();
  }

  public void merge(Interface existing) {
    existing.getOperations().forEach(this::addOperation);
  }
}

