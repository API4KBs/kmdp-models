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
import edu.mayo.kmdp.terms.adapters.json.AbstractTermsJsonAdapter;
import edu.mayo.kmdp.terms.adapters.xml.TermsXMLAdapter;
import edu.mayo.kmdp.terms.example.sch1.ISCH1.ISCH1Version;
import edu.mayo.kmdp.util.DateTimeUtil;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.terms.EnumeratedConceptTerm;
import org.omg.spec.api4kp._20200801.terms.TermDescription;
import org.omg.spec.api4kp._20200801.terms.model.TermImpl;

/*
	Example of generated 'terminology' class
*
* */
@javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter(SCH1Old.Adapter.class)
public enum SCH1Old implements ISCH1Version, EnumeratedConceptTerm<SCH1Old, ISCH1Version, ISCH1> {

  @Expose("http://test/generator#specific_concept")
  Specific_Concept("6789",
      UUID.nameUUIDFromBytes("specific_concept".getBytes()).toString(),
      "v00_Ancient",
      "http://test/generator#specific_concept",
      Collections.emptyList(),
      "specific concept",
      "http://test/generator#specific_concept",
      new SCH1Old[0],
      new SCH1Old[0]),
  @Expose("http://test/generator#deprecated_concept")
  Deprecated_Concept("999",
      UUID.nameUUIDFromBytes("deprecated_concept".getBytes()).toString(),
      "v00_Ancient",
      "http://test/generator#deprecated_concept",
      Collections.emptyList(),
      "deprecated concept",
      "http://test/generator#deprecated_concept",
      new SCH1Old[0],
      new SCH1Old[0]);

  public static final String schemeName = "SCH1";
  public static final String schemeID = "0.0.0.0";

  public static final ResourceIdentifier schemeSeriesIdentifier =
      SemanticIdentifier.newNamedId(
          URI.create("http://test/generator#concept_scheme1"),
          schemeID,
          schemeName);

  public static final ResourceIdentifier schemeVersionIdentifier =
      SemanticIdentifier.toVersionId(
          schemeSeriesIdentifier,
          "v00_Ancient",
          DateTimeUtil.parseDate("1981-12-01"));

  public static final Map<UUID,SCH1Old> index = indexByUUID(SCH1Old.values());


  private TermDescription description;
  private SCH1Series series;

  public TermDescription getDescription() {
    return description;
  }

  SCH1Old(final String code, final String conceptUUID, final String versionTag,
      final String conceptId, final List<String> additionalCodes,
      final String displayName, final String referent,
      final Term[] ancestors,
      final Term[] closure) {
    this.description = new TermImpl(conceptId, conceptUUID, versionTag, code, additionalCodes, displayName,
        referent, ancestors, closure, DateTimeUtil.parseDate("1981-12-01"));
  }

  @Override
  public ResourceIdentifier getDefiningScheme() {
    return schemeVersionIdentifier;
  }

  @Override
  public SCH1Series asSeries() {
    if (series == null) {
      series = (SCH1Series) SCH1Series.resolveUUID(this.getUuid())
          .orElseThrow(IllegalStateException::new);
    }
    return series;
  }


  @Override
  public Date getVersionEstablishedOn() {
    return schemeVersionIdentifier.getEstablishedOn();
  }


  public static class Adapter extends TermsXMLAdapter {
    public static final TermsXMLAdapter instance = new SCH1Old.Adapter();
    protected Term[] getValues() { return values(); }
  }

  public static class JsonSerializer extends AbstractTermsJsonAdapter.AbstractSerializer<SCH1Old> { }

  public static class JsonDeserializer extends AbstractTermsJsonAdapter.AbstractDeserializer<SCH1Old> {
    protected SCH1Old[] getValues() {
      return values();
    }
    @Override
    protected Optional<SCH1Old> resolveUUID(UUID uuid) {
      return SCH1Old.resolveUUID(uuid);
    }
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
    return resolveTerm(refUri, SCH1Old.values(), Term::getReferentId);
  }

}


