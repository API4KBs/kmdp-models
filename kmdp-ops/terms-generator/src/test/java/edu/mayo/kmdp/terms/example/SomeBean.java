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
package edu.mayo.kmdp.terms.example;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.escalon.hypermedia.hydra.mapping.Expose;
import de.escalon.hypermedia.hydra.mapping.Vocab;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;
import java.util.Set;

@Expose("http://org.foo.test/Some/Bean")
@Vocab("http://my/base/voc#")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "@id", scope = SomeBean.class)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SomeBean", propOrder = {
    "schone"
})
@XmlRootElement
public class SomeBean {

  @JsonProperty("@id")
  @XmlTransient
  private URI id = URI.create("http://my/individual/ID107");

  @XmlJavaTypeAdapter(value = SCH1.Adapter.class)
  private SCH1 schone;

  @XmlTransient
  private Set<SCH1> schones;

  @Expose("http://org.foo/xyz")
  public SCH1 getSchone() {
    return schone;
  }

  public void setSchone(SCH1 schone) {
    this.schone = schone;
  }

  @Expose("http://org.foo/xyz")
  public Set<SCH1> getSchones() {
    return schones;
  }

  public void setSchones(Set<SCH1> schones) {
    this.schones = schones;
  }

  public URI getId() {
    return id;
  }

  public void setId(URI id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return "SomeBean{" +
        "id=" + id +
        ", schone=" + schone +
        ", schones=" + schones +
        '}';
  }
}
