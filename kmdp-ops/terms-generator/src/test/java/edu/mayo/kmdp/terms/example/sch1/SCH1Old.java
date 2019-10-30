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
package edu.mayo.kmdp.terms.example.sch1;


import static edu.mayo.kmdp.id.helper.DatatypeHelper.indexByUUID;
import static edu.mayo.kmdp.id.helper.DatatypeHelper.resolveTerm;

import de.escalon.hypermedia.hydra.mapping.Expose;
import edu.mayo.kmdp.id.Identifier;
import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.id.VersionedIdentifier;
import edu.mayo.kmdp.series.Series;
import edu.mayo.kmdp.terms.TermDescription;
import edu.mayo.kmdp.terms.adapters.ConceptTermsJsonAdapter;
import edu.mayo.kmdp.terms.adapters.TermsXMLAdapter;
import edu.mayo.kmdp.terms.example.cito.ICito;
import edu.mayo.kmdp.terms.impl.model.TermImpl;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;

/*
	Example of generated 'terminology' class
*
* */
@javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter(SCH1Old.Adapter.class)
public enum SCH1Old implements ISCH1 {

  @Expose("http://test/generator#specific_concept")
  Specific_Concept("6789",
      UUID.nameUUIDFromBytes("specific_concept".getBytes()).toString(),
      "http://test/generator#specific_concept",
      Collections.emptyList(),
      "specific concept",
      "http://test/generator#specific_concept",
      new SCH1Old[0],
      new SCH1Old[0],
      SCH1Series.Specific_Concept),
  @Expose("http://test/generator#deprecated_concept")
  Deprecated_Concept("999",
      UUID.nameUUIDFromBytes("deprecated_concept".getBytes()).toString(),
      "http://test/generator#deprecated_concept",
      Collections.emptyList(),
      "deprecated concept",
      "http://test/generator#deprecated_concept",
      new SCH1Old[0],
      new SCH1Old[0],
      SCH1Series.Deprecated_Concept);

  public static final URIIdentifier schemeURI = Series.toVersion(
      ICito.seriesUri,
      URI.create("http://test/generator/v00_Ancient#concept_scheme1"));

  public static final NamespaceIdentifier namespace = new NamespaceIdentifier()
      .withId(URI.create("http://test/generator#concept_scheme1"))
      .withLabel("Concept Scheme 1")
      .withTag("concept_scheme_1")
      .withVersion("v00_Ancient");

  public static final Map<UUID,SCH1Old> index = indexByUUID(SCH1Old.values());


  private TermDescription description;
  private SCH1Series series;

  public TermDescription getDescription() {
    return description;
  }

  SCH1Old(final String code, final String conceptUUID,
      final String conceptId, final List<String> additionalCodes,
      final String displayName, final String referent,
      final Term[] ancestors,
      final Term[] closure,
      SCH1Series series) {
    this.description = new TermImpl(conceptId, conceptUUID, code, additionalCodes, displayName,
        referent, ancestors, closure);
    this.series = series;
  }

  @Override
  public Identifier getNamespace() {
    return namespace;
  }

  @Override
  public VersionedIdentifier getVersionIdentifier() {
    return namespace;
  }

  @Override
  public SCH1Series asEnum() {
    return toSeries();
  }

  @Override
  public Series<ISCH1> asSeries() {
    return toSeries();
  }

  private SCH1Series toSeries() {
    if (series == null) {
      series = (SCH1Series) SCH1Series.resolveUUID(this.getConceptUUID())
          .orElseThrow(IllegalStateException::new);
    }
    return series;
  }


  public static class Adapter extends TermsXMLAdapter {
    public static final TermsXMLAdapter instance = new SCH1Old.Adapter();
    protected Term[] getValues() { return values(); }
  }

  public static class JsonSerializer extends ConceptTermsJsonAdapter.Serializer { }

  public static class JsonDeserializer extends ConceptTermsJsonAdapter.Deserializer {
    protected Term[] getValues() {
      return values();
    }
    @Override
    protected Optional<SCH1Old> resolveUUID(UUID uuid) {
      return SCH1Old.resolveUUID(uuid);
    }
  }


  public static Optional<SCH1Old> resolve(final Term trm) {
    return resolveId(trm.getConceptId()); //TODO
  }

  public static Optional<SCH1Old> resolve(final String tag) {
    return resolveTag(tag);
  }

  public static Optional<SCH1Old> resolveId(final String conceptId) {
    return resolveId(URI.create(conceptId));
  }

  public static Optional<SCH1Old> resolveTag(final String tag) {
    return resolveTerm(tag, SCH1Old.values(), Term::getTag);
  }

  public static Optional<SCH1Old> resolveUUID(final UUID conceptId) {
    return Optional.of(index.get(conceptId));
  }

  public static Optional<SCH1Old> resolveId(final URI conceptId) {
    return resolveTerm(conceptId, SCH1Old.values(), Term::getConceptId);
  }

  public static Optional<SCH1Old> resolveRef(final String refUri) {
    return resolveTerm(refUri, SCH1Old.values(), Term::getRef);
  }
}


