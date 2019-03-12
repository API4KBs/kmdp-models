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

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.id.helper.DatatypeHelper;
import edu.mayo.kmdp.terms.ConceptScheme;
import edu.mayo.kmdp.util.NameUtils;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class AnonymousConceptScheme extends NamespaceIdentifier implements ConceptScheme<Term> {

  private URI versionId;

  public AnonymousConceptScheme() {
  }

  public AnonymousConceptScheme(String schemeId, String schemeName, URI schemeURI,
      URI schemeVersionURI) {
    this.withId(schemeURI)
        .withLabel(schemeName)
        .withTag(schemeId)
        .withVersion(DatatypeHelper.versionOf(schemeVersionURI, schemeURI));
    this.versionId = schemeVersionURI;
  }

  public AnonymousConceptScheme(URI scheme) {
    this.withId(scheme)
        .withTag(scheme.getFragment())
        .withLabel(scheme.getFragment());
  }


  @Override
  @JsonIgnore
  public Stream<Term> getConcepts() {
    throw new UnsupportedOperationException("Unable to track Concepts in Anonymous Scheme");
  }

  @Override
  public Optional<Term> lookup(Term other) {
    return Optional.ofNullable(other);
  }

  @Override
  public boolean subsumes(Term sup, Term sub) {
    throw new UnsupportedOperationException("Unable to track Concepts in Anonymous Scheme");
  }

  @Override
  public URI getVersionId() {
    return versionId;
  }

  @Override
  public NamespaceIdentifier asNamespace() {
    return this;
  }

}
