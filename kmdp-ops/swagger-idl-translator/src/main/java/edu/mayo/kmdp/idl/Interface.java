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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Interface {

  private Map<String,Operation> operations = new HashMap<>();
  private String name;
  private String documentation;

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

  public String getDocumentation() {
    return documentation;
  }

  public void setDocumentation(String documentation) {
    this.documentation = documentation;
  }

  public Collection<Operation> getOperations() {
    return listOperations();
  }

  public Collection<Operation> listOperations() {
    List<Operation> ops = new ArrayList<>(operations.values()).stream()
        .filter(op -> op.getName() != null)
        .collect(Collectors.toList());
    ops.sort(Comparator.comparing(Operation::getName));
    return ops;
  }

  public void merge(Interface existing) {
    existing.listOperations().forEach(this::addOperation);
    this.documentation = this.documentation + "\n" + existing.documentation;
  }
}

