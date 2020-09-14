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
package edu.mayo.kmdp.terms.example.cito;


import static edu.mayo.kmdp.id.helper.DatatypeHelper.resolveTerm;

import org.omg.spec.api4kp._20200801.series.Series;
import org.omg.spec.api4kp._20200801.terms.ConceptTerm;
import org.omg.spec.api4kp._20200801.terms.TermDescription;
import edu.mayo.kmdp.terms.adapters.json.UUIDTermsJsonAdapter;
import edu.mayo.kmdp.terms.adapters.xml.TermsXMLAdapter;
import org.omg.spec.api4kp._20200801.terms.model.TermImpl;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;

public enum Cito implements ICito {

  Cites("cites",
      UUID.nameUUIDFromBytes("cites".getBytes()).toString(),
      "LATEST",
      "http://test.skos.foo#cites",
      Collections.emptyList(),
      "cites",
      "http://mockpurl.org/cito#cites",
      new Term[]{},
      new Term[]{}),

  Cites_As_Source_Document("citesAsSourceDocument",
      UUID.nameUUIDFromBytes("citesAsSourceDocument".getBytes()).toString(),
      "LATEST",
      "http://test.skos.foo#citesAsSourceDocument",
      Collections.emptyList(),
      "cites as source document",
      "http://mockpurl.org/cito#citesAsSourceDocument",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites});


  public static final ResourceIdentifier schemeURI = Series.toVersion(
      ICito.seriesUri,
      "test.foo");

  public static final ResourceIdentifier namespace = SemanticIdentifier.newNamedId(
      schemeURI.getResourceId(),
      ICito.schemeID,
      ICito.schemeName);

  public static final Map<UUID,Cito> index = Arrays.stream(Cito.values())
      .collect(Collectors.toConcurrentMap(ConceptTerm::getUuid, Function.identity()));

  private TermDescription description;
  private CitoSeries series;

  public TermDescription getDescription() {
    return description;
  }

  Cito(final String conceptId, final String conceptUUID, String versionTag,
      final String code, final List<String> additionalCodes,
      final String displayName, final String referent,
      final Term[] ancestors,
      final Term[] closure) {
    this.description = new TermImpl(conceptId, conceptUUID, versionTag, code, additionalCodes, displayName,
        referent, ancestors, closure, new Date());
  }

  @Override
  public ResourceIdentifier getNamespace() {
    return namespace;
  }

  @Override
  public ResourceIdentifier getVersionIdentifier() {
    return SemanticIdentifier.newId(namespace.getResourceId(), this.getTag(), namespace.getVersionTag());
  }

  public URI getVersionId() {
    return description.getVersionId();
  }

  @Override
  public CitoSeries asEnum() {
    return toSeries();
  }

  @Override
  public Series<ICito> asSeries() {
    return toSeries();
  }

  private CitoSeries toSeries() {
    if (series == null) {
      series = (CitoSeries) CitoSeries.resolveUUID(this.getUuid())
          .orElseThrow(IllegalStateException::new);
    }
    return series;
  }

  @Override
  public URI getResourceId() {
    return this.description.getResourceId();
  }

  @Override
  public UUID getUuid() {
    return this.description.getUuid();
  }

  public static class Adapter extends TermsXMLAdapter {
    public static final TermsXMLAdapter instance = new Adapter();
    protected Term[] getValues() { return values(); }
  }

  public static class JsonSerializer extends UUIDTermsJsonAdapter.Serializer { }

  public static class JsonDeserializer extends UUIDTermsJsonAdapter.Deserializer {
    protected Term[] getValues() {
      return values();
    }
  }

  public static Optional<Cito> resolve(final Term trm) {
    return resolveId(trm.getConceptId()); //TODO
  }

  public static Optional<Cito> resolve(final String tag) {
    return resolveTag(tag);
  }

  public static Optional<Cito> resolveId(final String conceptId) {
    return resolveId(URI.create(conceptId));
  }

  public static Optional<Cito> resolveTag(final String tag) {
    return resolveTerm(tag, Cito.values(), Term::getTag);
  }

  public static Optional<Cito> resolveUUID(final UUID conceptId) {
    return Optional.ofNullable(index.get(conceptId));
  }

  public static Optional<Cito> resolveId(final URI conceptId) {
    return resolveTerm(conceptId, Cito.values(), Term::getConceptId);
  }

  public static Optional<Cito> resolveRef(final String refUri) {
    return resolveTerm(refUri, Cito.values(), Term::getReferentId);
  }

  @Override
  public Date getEstablishedOn() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Date getVersionEstablishedOn() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getVersionTag() {
    return namespace.getVersionTag();
  }

}
