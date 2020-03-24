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

import edu.mayo.kmdp.id.Identifier;
import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.id.VersionedIdentifier;
import edu.mayo.kmdp.series.Series;
import edu.mayo.kmdp.terms.ConceptTerm;
import edu.mayo.kmdp.terms.TermDescription;
import edu.mayo.kmdp.terms.adapters.json.AbstractTermsJsonAdapter;
import edu.mayo.kmdp.terms.adapters.xml.TermsXMLAdapter;
import edu.mayo.kmdp.util.DateTimeUtil;
import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;

/*
	Example of generated 'terminology' class
*
* */
@com.fasterxml.jackson.databind.annotation.JsonSerialize( using = SCH1Series.JsonSerializer.class )
@com.fasterxml.jackson.databind.annotation.JsonDeserialize( using = SCH1Series.JsonDeserializer.class )
public enum SCH1Series implements ISCH1, Series<ISCH1> {

  Specific_Concept(SCH1.Specific_Concept,SCH1Old.Specific_Concept),
  Nested_Specific_Concept(SCH1.Nested_Specific_Concept),
  Deprecated_Concept(SCH1Old.Deprecated_Concept),
  Sub_Sub_Concept(SCH1.Sub_Sub_Concept);

  public static final List<String> schemeVersions =
      java.util.Arrays.asList( "20190801"  ,   "19811201"   );

  public static final List<Date> schemeReleases =
      DateTimeUtil.parseDates(java.util.Arrays.asList( "20190801"  ,   "19811201"   ),"yyyyMMdd");


  public static final Map<UUID, ISCH1> index = indexByUUID(SCH1Series.values());


  private List<ISCH1> versions;

  SCH1Series(ISCH1... versions) {
    this.versions = Arrays.asList(versions);
  }

  public List<ISCH1> getVersions() {
    return versions;
  }

  public static int count() {
    return values().length;
  }

  public static int countUnexpired() {
    return  (int) Arrays.stream(values())
        .filter(x -> !x.isSeriesExpired())
        .count();
  }

  @Override
  public TermDescription getDescription() {
    return latest().map(ConceptTerm::getDescription)
        .orElse(null);
  }

  @Override
  public Identifier getNamespace() {
    return ISCH1.seriesNamespace;
  }


  public static Optional<ISCH1> resolve(final Term trm) {
    return resolveId(trm.getConceptId());
  }

  public static Optional<ISCH1> resolve(final String tag) {
    return resolveTag(tag);
  }

  public static Optional<ISCH1> resolveId(final String conceptId) {
    return resolveId(URI.create(conceptId));
  }

  public static Optional<ISCH1> resolveTag(final String tag) {
    return resolveTerm(tag, SCH1Series.values(), Term::getTag);
  }

  public static Optional<ISCH1> resolveUUID(final UUID conceptId) {
    return Optional.of(index.get(conceptId));
  }

  public static Optional<ISCH1> resolveId(final URI conceptId) {
    return resolveTerm(conceptId, SCH1Series.values(), Term::getConceptId);
  }

  public static Optional<ISCH1> resolveRef(final String refUri) {
    return resolveTerm(refUri, SCH1Series.values(), Term::getRef);
  }

  @Override
  public VersionedIdentifier getVersionIdentifier() {
    return getLatest().getVersionIdentifier();
  }

  @Override
  public boolean isSeriesExpired() {
    Date lastEstablished = getLatest().getVersionEstablishedOn();
    return schemeReleases.get(0).compareTo(lastEstablished) > 0;
  }

  @Override
  public Optional<Date> getSeriesExpiredOn() {
    Date lastEstablished = getLatest().getVersionEstablishedOn();
    return schemeReleases.stream()
        .filter(r -> r.compareTo(lastEstablished) > 0)
        .min(Comparator.naturalOrder());
  }

  @Override
  public SCH1Series asEnum() {
    return this;
  }

  @Override
  public Series<ISCH1> asSeries() {
    return this;
  }

  public static class Adapter extends TermsXMLAdapter {
    public static final TermsXMLAdapter instance = new SCH1Series.Adapter();
    protected SCH1Series[] getValues() { return values(); }
  }


  public static class JsonSerializer extends AbstractTermsJsonAdapter.AbstractSerializer<ISCH1> {

  }

  public static class JsonDeserializer extends AbstractTermsJsonAdapter.AbstractDeserializer<ISCH1> {
    protected ISCH1[] getValues() {
      return values();
    }
    @Override
    protected Optional<ISCH1> resolveUUID(UUID uuid) {
      return SCH1Series.resolveUUID(uuid);
    }
  }


  @Override
  public String getVersionTag() {
    return ((NamespaceIdentifier)getNamespace()).getVersion();
  }

  @Override
  public Date getEstablishedOn() {
    return ((NamespaceIdentifier)getNamespace()).getEstablishedOn();
  }

}


