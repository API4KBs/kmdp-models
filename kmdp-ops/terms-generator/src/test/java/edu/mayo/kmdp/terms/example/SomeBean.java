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
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import de.escalon.hypermedia.hydra.mapping.Expose;
import de.escalon.hypermedia.hydra.mapping.Vocab;
import edu.mayo.kmdp.terms.example.sch1.ISCH1;
import edu.mayo.kmdp.terms.example.sch1.SCH1Series;
import java.net.URI;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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

  @XmlJavaTypeAdapter(value = SCH1Series.Adapter.class)
  @com.fasterxml.jackson.databind.annotation.JsonSerialize(
      using = edu.mayo.kmdp.terms.TermsJsonAdapter.Serializer.class)
  @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = SCH1Series.JsonAdapter.class)
  private ISCH1 schone;

  @XmlTransient
  private Set<ISCH1> schones;

  @Expose("http://org.foo/xyz")
   public ISCH1 getSchone() {
    return schone;
  }

  public void setSchone(ISCH1 schone) {
    this.schone = schone;
  }

  @Expose("http://org.foo/xyz")
  public Set<ISCH1> getSchones() {
    return schones;
  }

  public void setSchones(Set<ISCH1> schones) {
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

  private class My implements Converter {

    @Override
    public Object convert(Object value) {
      return null;
    }

    @Override
    public JavaType getInputType(TypeFactory typeFactory) {
      return null;
    }

    @Override
    public JavaType getOutputType(TypeFactory typeFactory) {
      return null;
    }
  }
}
