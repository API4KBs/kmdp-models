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

import java.util.Optional;
import java.util.stream.Collectors;

public class Type {

  private String name;
  private String packageName;
  private String fullyQualifiedName;
  private boolean collection;
  private Struct struct;

  public Type(String type) {
    this(type,null,false);
  }

  public Type(String type, String packageName) {
    this(type,packageName,false);
  }

  public Type(String type, String packageName, boolean collection) {
    this.name = type;
    this.packageName = packageName;
    this.collection = collection;
    if (packageName != null) {
      this.fullyQualifiedName = packageName;
    } else {
      this.fullyQualifiedName = name;
    }
  }

  public Type(String type, boolean collection) {
    this(type,null,collection);
  }

  public boolean isCollection() {
    return collection;
  }

  public void setCollection(boolean collection) {
    this.collection = collection;
  }

  public String getName() {
    return name;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getFullyQualifiedName() {
    return fullyQualifiedName;
  }

  public void setFullyQualifiedName(String fullyQualifiedName) {
    this.fullyQualifiedName = fullyQualifiedName;
  }

  public void linkStruct(Struct structDef) {
    this.struct = structDef;
    this.packageName = structDef.getPackageStack().stream()
        .collect(Collectors.joining("."));
    this.fullyQualifiedName = IDLNameUtil.toFQName(packageName,name);
  }

  public Optional<Struct> tryGetStruct() {
    return Optional.ofNullable(struct);
  }

}
