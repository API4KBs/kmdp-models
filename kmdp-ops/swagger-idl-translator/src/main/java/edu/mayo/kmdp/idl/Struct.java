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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Struct {

  private String typeName;
  private Map<String, Field> fields = new LinkedHashMap<>();

  private List<String> packages = Collections.emptyList();

  public Struct(String name, String packageName) {
    this.typeName = name;
    if (packageName != null) {
      this.packages = Arrays.asList(packageName.split(","));
    }
  }

  public Struct(String name) {
    this.typeName = name;
  }

  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

  public String getTypeName() {
    return typeName;
  }

  public void addField(String name, Type type) {
    this.fields.put(name, new Field(name,type));
  }

  public Collection<Field> getFields() {
    return fields.values();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Struct)) {
      return false;
    }

    Struct struct = (Struct) o;

    return typeName.equals(struct.typeName);
  }

  @Override
  public int hashCode() {
    return typeName.hashCode();
  }

  @Override
  public String toString() {
    return "Struct{" +
        "typeName='" + typeName + '\'' +
        '}';
  }

  public static class Field {
    private String name;
    private Type type;

    public Field(String name, Type type) {
      this.name = name;
      this.type = type;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Type getType() {
      return type;
    }

    public void setType(Type type) {
      this.type = type;
    }
  }


}

