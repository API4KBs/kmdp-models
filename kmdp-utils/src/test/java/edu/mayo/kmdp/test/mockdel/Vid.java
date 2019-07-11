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

import de.escalon.hypermedia.hydra.mapping.Expose;
import de.escalon.hypermedia.hydra.mapping.Term;
import de.escalon.hypermedia.hydra.mapping.Vocab;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.net.URI;
import java.util.Objects;


@XmlType(name = "Vid")
@XmlAccessorType(XmlAccessType.FIELD)
@Vocab("http://www.main.org")
@Term(define = "base", as = "http://www.main.org/base")
@Expose("base:VersionID")
public class Vid {

  private URI identifier;
  private URI version;

  public Vid() {
  }

  public Vid(String uri, String tag) {
    identifier = URI.create(uri);
    version = URI.create(uri + "/" + tag);
  }

  @Expose("base:identifier")
  public URI getIdentifier() {
    return identifier;
  }

  public void setIdentifier(URI identifier) {
    this.identifier = identifier;
  }

  @Expose("base:versionIdentifier")
  public URI getVersion() {
    return version;
  }

  public void setVersion(URI version) {
    this.version = version;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Vid)) {
      return false;
    }
    Vid vid = (Vid) o;
    return Objects.equals(identifier, vid.identifier) &&
        Objects.equals(version, vid.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(identifier, version);
  }
}
