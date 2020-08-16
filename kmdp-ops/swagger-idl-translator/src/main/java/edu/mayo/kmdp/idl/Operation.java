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

public class Operation {

  private String name;
  private Type returnType;
  private Map<String, Parameter> inputs = new HashMap<>();
  private Map<Integer, Exception> exceptions = new HashMap<>();
  private String actionVerb;
  private String restPath;
  private String summary;
  private String description;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Type getReturnType() {
    return returnType;
  }

  public void setReturnType(Type returnType) {
    this.returnType = returnType;
  }

  public String getActionVerb() {
    return actionVerb;
  }

  public void setActionVerb(String actionVerb) {
    this.actionVerb = actionVerb;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Operation addInput(Parameter param) {
    inputs.put(param.getName(), param);
    return this;
  }

  public Collection<Parameter> getInputs() {
    return inputs.values();
  }

  public String getRestPath() {
    return restPath;
  }

  public void setRestPath(String restPath) {
    this.restPath = restPath;
  }

  public List<Parameter> listInputs() {
    List<Parameter> ins = new ArrayList<>(inputs.values());
    Collections.sort(ins, (p1,p2) -> {
      if (p1.isRequired() && ! p2.isRequired()) {
        return -1;
      }
      return p1.getName().compareTo(p2.getName());
    });
    return ins;
  }

  public List<Exception> listExceptions() {
    List<Exception> exceptions = new ArrayList<>(this.exceptions.values());
    Collections.sort(exceptions, Comparator.comparing(Exception::getCode));
    return exceptions;
  }

  public void addException(Exception ex) {
    this.exceptions.put(ex.getCode(),ex);
  }
}