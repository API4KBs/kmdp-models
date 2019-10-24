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
@javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter(SCH1.Adapter.class)
public enum SCH1 implements ISCH1 {

  @Expose("http://test/generator#specific_concept")
  Specific_Concept("6789",
      UUID.nameUUIDFromBytes("specific_concept".getBytes()).toString(),
      "http://test/generator#specific_concept",
      Collections.emptyList(),
      "specific concept",
      "http://test/generator#specific_concept",
      new SCH1[0],
      new SCH1[0]),
  @Expose("http://test/generator#nested_specific_concept")
  Nested_Specific_Concept("12345",
      UUID.nameUUIDFromBytes("nested_specific_concept".getBytes()).toString(),
      "http://test/generator#nested_specific_concept",
      Collections.emptyList(),
      "nested specific concept",
      "http://test/generator#nested_specific_concept",
      new SCH1[]{Specific_Concept},
      new SCH1[]{Specific_Concept}),
  @Expose("http://test/generator#sub_sub_concept")
  Sub_Sub_Concept("sub",
      UUID.nameUUIDFromBytes("sub_sub_concept".getBytes()).toString(),
      "http://test/generator#sub_sub_concept",
      Collections.emptyList(),
      "sub sub concept",
      "http://test/generator#sub_sub_concept",
      new SCH1[]{Nested_Specific_Concept},
      new SCH1[]{Nested_Specific_Concept, Specific_Concept});

  public static final URIIdentifier schemeURI = Series.toVersion(
      SCH1.seriesUri,
      URI.create("http://test/generator/v01#concept_scheme1"));

  public static final NamespaceIdentifier namespace = new NamespaceIdentifier()
      .withId(URI.create("http://test/generator#concept_scheme1"))
      .withLabel("Concept Scheme 1")
      .withTag("concept_scheme_1")
      .withVersion("v01");


  public static final Map<UUID, SCH1> index = indexByUUID(SCH1.values());

  private TermDescription description;

  public TermDescription getDescription() {
    return description;
  }

  SCH1(final String code, final String conceptUUID,
      final String conceptId, final List<String> additionalCodes,
      final String displayName, final String referent,
      final Term[] ancestors,
      final Term[] closure) {
    this.description = new TermImpl(conceptId, conceptUUID, code, additionalCodes, displayName,
        referent, ancestors, closure);
  }

  @Override
  public Identifier getNamespace() {
    return namespace;
  }

  @Override
  public VersionedIdentifier getVersionIdentifier() {
    return namespace;
  }

  public static class Adapter extends edu.mayo.kmdp.terms.TermsXMLAdapter {
    public static final edu.mayo.kmdp.terms.TermsXMLAdapter instance = new Adapter();
    protected Term[] getValues() { return values(); }
  }

  public static class JsonAdapter extends edu.mayo.kmdp.terms.TermsJsonAdapter.UUIDBasedDeserializer {
    public static final edu.mayo.kmdp.terms.TermsJsonAdapter.Deserializer instance = new JsonAdapter();
    protected Term[] getValues() { return values(); }

    @Override
    protected Optional<SCH1> resolveUUID(UUID uuid) {
      return SCH1.resolveUUID(uuid);
    }
  }


  public static Optional<SCH1> resolve(final Term trm) {
    return resolveId(trm.getConceptId());
  }

  public static Optional<SCH1> resolve(final String tag) {
    return resolveTag(tag);
  }

  public static Optional<SCH1> resolveId(final String conceptId) {
    return resolveId(URI.create(conceptId));
  }

  public static Optional<SCH1> resolveTag(final String tag) {
    return resolveTerm(tag, SCH1.values(), Term::getTag);
  }

  public static Optional<SCH1> resolveUUID(final UUID conceptId) {
    return Optional.of(index.get(conceptId));
  }

  public static Optional<SCH1> resolveId(final URI conceptId) {
    return resolveTerm(conceptId, SCH1.values(), Term::getConceptId);
  }

  public static Optional<SCH1> resolveRef(final String refUri) {
    return resolveTerm(refUri, SCH1.values(), Term::getRef);
  }
}


