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
package edu.mayo.kmdp.test.mockdel;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlType(name = "Trean")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class, property = "treanId"
)
public class Trean {

  @JsonProperty("treanId")
  @XmlID
  private String id;
  private Vid ident;
  @XmlIDREF
  private Bean rel1;
  @XmlIDREF
  private List<Bean> rel2;

  public Trean(Vid vid) {
    this.ident = vid;
    this.id = ident.getVersion().toString();
  }

  public Trean() {
  }

  public Vid getIdent() {
    return ident;
  }

  public void setIdent(Vid ident) {
    this.ident = ident;
  }


  public Bean getRel1() {
    return rel1;
  }

  public void setRel1(Bean rel1) {
    this.rel1 = rel1;
  }

  public List<Bean> getRel2() {
    return rel2;
  }

  public void setRel2(List<Bean> rel2) {
    this.rel2 = rel2;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
