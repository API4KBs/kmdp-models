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


import edu.mayo.kmdp.util.NameUtils;
import edu.mayo.kmdp.util.NameUtils.IdentifierType;
import edu.mayo.kmdp.util.Util;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Module {

  private String name;
  private Map<String,Module> subModules = new LinkedHashMap<>();
  private Map<String,Struct> structs = new LinkedHashMap<>();
  private Map<String,Interface> interfaces = new LinkedHashMap<>();

  public Module(String name) {
    this.name = name;
  }

  public Module addModule(Module sub) {
    if (subModules.containsKey(sub.name) && ! subModules.containsValue(sub)) {
      throw new UnsupportedOperationException("Cannot merge modules yet");
    }
    subModules.put(sub.name,sub);
    return this;
  }

  public Collection<Module> getSubModules() {
    return subModules.values().stream()
        .filter(m -> !Util.isEmpty(m.getName()))
        .collect(Collectors.toList());
  }

  public void clear() {
    subModules.clear();
  }

  public Optional<Interface> getInterface(String tag) {
    return Optional.ofNullable(interfaces.get(tag2Name(tag)));
  }

  public Interface addInterface(String tag) {
    Interface itf = new Interface(tag2Name(tag));

    if (interfaces.containsKey(itf.getName())) {
      Interface existing = interfaces.get(itf.getName());
      itf.merge(existing);
    }
    interfaces.put(itf.getName(),itf);
    return itf;
  }

  private String tag2Name(String tag) {
    return NameUtils.nameToIdentifier(tag, IdentifierType.CLASS);
  }

  public Module insertStruct(Struct struct) {
    insertStruct(struct, struct.getPackageStack());
    return this;
  }

  public Module addStruct(Struct struct) {
    this.structs.put(struct.getTypeName(),struct);
    return this;
  }

  private void insertStruct(Struct struct, Deque<String> packageStack) {
    if (packageStack.isEmpty()) {
      this.structs.put(struct.getTypeName(),struct);
    } else {
      String pack = packageStack.pop();
      Module sub;
      if (subModules.containsKey(pack)) {
        sub = subModules.get(pack);
      } else {
        sub = new Module(pack);
        addModule(sub);
      }
      sub.insertStruct(struct,packageStack);
    }
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

  public Collection<Struct> getStructs() {
    return structs.values();
  }

  public Map<String,Struct> getStructMap() {
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


