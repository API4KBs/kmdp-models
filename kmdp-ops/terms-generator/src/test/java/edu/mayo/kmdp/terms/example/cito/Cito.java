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


import static edu.mayo.kmdp.id.helper.DatatypeHelper.indexByUUID;
import static edu.mayo.kmdp.id.helper.DatatypeHelper.resolveTerm;

import edu.mayo.kmdp.terms.example.cito.ICito.ICitoVersion;
import edu.mayo.kmdp.terms.example.sch1.SCH1;
import edu.mayo.kmdp.terms.example.sch1.SCH1Series;
import edu.mayo.kmdp.util.DateTimeUtil;
import org.omg.spec.api4kp._20200801.id.VersionIdentifier;
import org.omg.spec.api4kp._20200801.series.Series;
import org.omg.spec.api4kp._20200801.terms.ConceptTerm;
import org.omg.spec.api4kp._20200801.terms.EnumeratedConceptTerm;
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

public enum Cito implements ICitoVersion, EnumeratedConceptTerm<Cito,ICitoVersion,ICito> {

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


  public static final ResourceIdentifier schemeVersionIdentifier =
      SemanticIdentifier.toVersionId(schemeSeriesIdentifier, "v01",
          DateTimeUtil.parseDateOrNow("20190801", "yyyyMMdd"));


  public static final Map<UUID, Cito> index = indexByUUID(Cito.values());

  private TermDescription description;
  private CitoSeries series;

  public TermDescription getDescription() {
    return description;
  }

  Cito(final String code, final String conceptUUID, String versionTag,
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

  public CitoSeries asSeries() {
    if (series == null) {
      series = CitoSeries.resolveUUID(this.getUuid())
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

  public static class JsonSerializer extends UUIDTermsJsonAdapter.Serializer<Cito> { }

  public static class JsonDeserializer extends UUIDTermsJsonAdapter.Deserializer<Cito> {
    protected Cito[] getValues() {
      return values();
    }
  }


  public static Optional<Cito> resolveTag(final String tag) {
    return resolveTerm(tag, Cito.values(), Term::getTag);
  }

}
