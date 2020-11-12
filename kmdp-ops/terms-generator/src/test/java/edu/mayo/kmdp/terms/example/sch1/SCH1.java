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
import edu.mayo.kmdp.terms.example.cito.Cito;
import edu.mayo.kmdp.terms.example.cito.ICito;
import edu.mayo.kmdp.terms.example.cito.ICito.ICitoVersion;
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
import org.omg.spec.api4kp._20200801.id.VersionIdentifier;
import org.omg.spec.api4kp._20200801.series.Series;
import org.omg.spec.api4kp._20200801.terms.EnumeratedConceptTerm;
import org.omg.spec.api4kp._20200801.terms.TermDescription;
import org.omg.spec.api4kp._20200801.terms.model.TermImpl;

/*
	Example of generated 'terminology' class
*
* */
@javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter(SCH1.Adapter.class)
@com.fasterxml.jackson.databind.annotation.JsonSerialize( using = SCH1.JsonSerializer.class )
@com.fasterxml.jackson.databind.annotation.JsonDeserialize( using = SCH1.JsonDeserializer.class )
public enum SCH1 implements ISCH1Version, EnumeratedConceptTerm<SCH1, ISCH1Version, ISCH1> {

  @Expose("http://test/generator#specific_concept")
  Specific_Concept("6789",
      UUID.nameUUIDFromBytes("specific_concept".getBytes()).toString(),
      "v01",
      "http://test/generator#specific_concept",
      Collections.emptyList(),
      "specific concept",
      "http://test/generator#specific_concept",
      new SCH1[0],
      new SCH1[0]),
  @Expose("http://test/generator#nested_specific_concept")
  Nested_Specific_Concept("12345",
      UUID.nameUUIDFromBytes("nested_specific_concept".getBytes()).toString(),
      "v01",
      "http://test/generator#nested_specific_concept",
      Collections.emptyList(),
      "nested specific concept",
      "http://test/generator#nested_specific_concept",
      new SCH1[]{Specific_Concept},
      new SCH1[]{Specific_Concept}),
  @Expose("http://test/generator#sub_sub_concept")
  Sub_Sub_Concept("sub",
      UUID.nameUUIDFromBytes("sub_sub_concept".getBytes()).toString(),
      "v01",
      "http://test/generator#sub_sub_concept",
      Collections.emptyList(),
      "sub sub concept",
      "http://test/generator#sub_sub_concept",
      new SCH1[]{Nested_Specific_Concept},
      new SCH1[]{Nested_Specific_Concept, Specific_Concept});

  public static final ResourceIdentifier schemeVersionIdentifier =
      SemanticIdentifier.toVersionId(schemeSeriesIdentifier, "v01",
          DateTimeUtil.parseDateOrNow("20190801", "yyyyMMdd"));

  public static final Map<UUID, SCH1> index = indexByUUID(SCH1.values());

  private TermDescription description;
  private SCH1Series series;

  public TermDescription getDescription() {
    return description;
  }

  SCH1(final String code, final String conceptUUID, String versionTag,
      final String conceptId, final List<String> additionalCodes,
      final String displayName, final String referent,
      final Term[] ancestors,
      final Term[] closure) {
    this.description = new TermImpl(conceptId, conceptUUID, versionTag, code, additionalCodes, displayName,
        referent, ancestors, closure, DateTimeUtil.parseDateOrNow("20190801","yyyyMMdd"));
  }


  @Override
  public Date getVersionEstablishedOn() {
    return schemeVersionIdentifier.getEstablishedOn();
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
  public ResourceIdentifier getDefiningScheme() {
    return schemeVersionIdentifier;
  }


  public static class Adapter extends TermsXMLAdapter {
    public static final TermsXMLAdapter instance = new Adapter();
    protected Term[] getValues() { return values(); }
  }

  public static class JsonSerializer extends AbstractTermsJsonAdapter.AbstractSerializer<SCH1> { }

  public static class JsonDeserializer extends AbstractTermsJsonAdapter.AbstractDeserializer<SCH1> {
    protected SCH1[] getValues() {
      return values();
    }
    @Override
    protected Optional<SCH1> resolveUUID(UUID uuid) {
      return SCH1.resolveUUID(uuid);
    }
  }



  public static Optional<SCH1> resolveId(final URI conceptId) {
    return resolveTerm(conceptId, SCH1.values(), Term::getConceptId);
  }

  public static Optional<SCH1> resolveRef(final URI referentId) {
    return resolveTerm(referentId, SCH1.values(), Term::getReferentId);
  }

  public static Optional<SCH1> resolveTag(final String tag) {
    return resolveTerm(tag, SCH1.values(), Term::getTag);
  }

  public static Optional<SCH1> resolveUUID(final UUID conceptId) {
    return Optional.of(index.get(conceptId));
  }

}


