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
import edu.mayo.kmdp.terms.ConceptScheme;
import java.net.URI;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;
import org.omg.spec.api4kp._1_0.id.Term;

public class AnonymousConceptScheme
    extends IdentifiedConceptScheme<Term> implements ConceptScheme<Term> {

  private static final String NOT_SUPPORTED = "Unable to track Concepts in Anonymous Scheme";

  private URI versionId;

  public AnonymousConceptScheme(String schemeId, String versionTag,
      String schemeName, URI schemeURI, URI schemeVersionURI, Date pubDate) {
    super(schemeURI, versionTag, schemeName, pubDate);
    this.versionId = schemeVersionURI;
  }

  public AnonymousConceptScheme(String schemeId, String schemeName, URI schemeURI,
      URI schemeVersionURI) {
    this(schemeId, null, schemeName, schemeURI, schemeVersionURI, null);
  }

  public AnonymousConceptScheme(URI scheme) {
    this(null, null, null, scheme, null, null);
  }

  public URI getVersionId() {
    return versionId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AnonymousConceptScheme)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    AnonymousConceptScheme that = (AnonymousConceptScheme) o;

    return versionId != null ? versionId.equals(that.versionId) : that.versionId == null;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (versionId != null ? versionId.hashCode() : 0);
    return result;
  }

  @Override
  @JsonIgnore
  public Stream<Term> getConcepts() {
    throw new UnsupportedOperationException(NOT_SUPPORTED);
  }

  @Override
  @JsonIgnore
  public Optional<Term> getTopConcept() {
    throw new UnsupportedOperationException(NOT_SUPPORTED);
  }

  @Override
  public Optional<Term> lookup(Term other) {
    return Optional.ofNullable(other);
  }

  @Override
  public boolean subsumes(Term sup, Term sub) {
    throw new UnsupportedOperationException(NOT_SUPPORTED);
  }

}
