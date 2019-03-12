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
import de.escalon.hypermedia.hydra.mapping.Expose;
import de.escalon.hypermedia.hydra.mapping.Term;
import de.escalon.hypermedia.hydra.mapping.Vocab;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "Bean")
@XmlAccessorType(XmlAccessType.FIELD)
@Vocab("http://www.foo.com/bar")
@Term(define = "foo", as = "http://www.foo.com/bar")
@Expose("foo:Bean")
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class
)
public class Bean {

  @JsonProperty("@id")
  private String id;
  private Vid ident;
  @XmlElementRef
  private Trean t;

  public Bean(Vid vid) {
    this.ident = vid;
    this.id = vid.getVersion().toString();
  }

  public Bean() {
  }

  public Vid getIdent() {
    return ident;
  }

  public void setIdent(Vid ident) {
    this.ident = ident;
  }

  public Trean getT() {
    return t;
  }

  public void setT(Trean t) {
    this.t = t;
  }

  public String getId() {
    return id;
  }

  public void setId(String _id) {
    this.id = _id;
  }
}
