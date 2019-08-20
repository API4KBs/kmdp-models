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
package edu.mayo.kmdp.terms.impl.model;


import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.id.VersionedIdentifier;
import edu.mayo.kmdp.terms.ValueSet;
import java.net.URI;
import java.util.Optional;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;

public abstract class AbstractValueSet<T extends Term> implements ValueSet {

  private String id;
  private URIIdentifier uri;
  private String name;

  private T pivotalConcept;

  protected AbstractValueSet(String id, String name, URI uri) {
    this(id, name, uri, uri);
  }

  protected AbstractValueSet(String id, String name, URI uri, URI versionUri) {
    this.id = id;
    this.uri = new URIIdentifier().withUri(uri)
        .withVersionId(versionUri);
    this.name = name;
  }

  protected void setPivotalConcept(T pivotalConcept) {
    this.pivotalConcept = pivotalConcept;
  }

  @Override
  public Optional<Term> getPivotalConcept() {
    return Optional.ofNullable(pivotalConcept);
  }

  @Override
  public String getTag() {
    return id;
  }

  @Override
  public String getLabel() {
    return name;
  }

  @Override
  public VersionedIdentifier getId() {
    return uri;
  }

}
