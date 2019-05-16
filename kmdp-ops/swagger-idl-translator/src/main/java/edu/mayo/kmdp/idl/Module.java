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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Module {

  private String name;
  private Map<String,Module> subModules = new HashMap<>();
  private Map<String,Struct> structs = new HashMap<>();
  private Map<String,Interface> interfaces = new HashMap<>();

  public Module(String name) {
    this.name = name;
  }

  public Module addModule(Module sub) {
    if (subModules.containsKey(sub.name)) {
      throw new UnsupportedOperationException("Cannot merge modules yet");
    }
    subModules.put(sub.name,sub);
    return this;
  }

  public Collection<Module> getSubModules() {
    return subModules.values();
  }

  public Optional<Interface> getInterface(String tag) {
    return Optional.ofNullable(interfaces.get(tag));
  }

  public Module addInterface(Interface itf) {
    if (interfaces.containsKey(itf.getName())) {
      throw new UnsupportedOperationException("Cannot merge interfaces yet");
    }
    interfaces.put(itf.getName(),itf);
    return this;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setSubModules(Map<String, Module> subModules) {
    this.subModules = subModules;
  }

  public Map<String, Struct> getStructs() {
    return structs;
  }

  public void setStructs(Map<String, Struct> structs) {
    this.structs = structs;
  }

  public Collection<Interface> getInterfaces() {
    return interfaces.values();
  }

  public void setInterfaces(Map<String, Interface> interfaces) {
    this.interfaces = interfaces;
  }
}


