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
package edu.mayo.kmdp.terms.example;


import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.ConceptScheme;
import edu.mayo.kmdp.terms.Taxonomic;
import edu.mayo.kmdp.terms.impl.model.DefaultConceptScheme;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;

import java.net.URI;

public enum Cito implements Term, Taxonomic<Cito> {


  Cites("cites", "cites", "http://test.skos.foo#cites",
      new Term[]{},
      new Term[]{}),
  Cites_As_Source_Document("citesAsSourceDocument", "cites as source document",
      "http://test.skos.foo#citesAsSourceDocument",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Cites_As_Recommended_Reading("citesAsRecommendedReading", "cites as recommended reading",
      "http://test.skos.foo#citesAsRecommendedReading",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Parodies("parodies", "parodies", "http://test.skos.foo#parodies",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Plagiarizes("plagiarizes", "plagiarizes", "http://test.skos.foo#plagiarizes",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Derides("derides", "derides", "http://test.skos.foo#derides",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Includes_Quotation_From("includesQuotationFrom", "includes quotation from",
      "http://test.skos.foo#includesQuotationFrom",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Confirms("confirms", "confirms", "http://test.skos.foo#confirms",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Ridicules("ridicules", "ridicules", "http://test.skos.foo#ridicules",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Agrees_With("agreesWith", "agrees with", "http://test.skos.foo#agreesWith",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Describes("describes", "describes", "http://test.skos.foo#describes",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Qualifies("qualifies", "qualifies", "http://test.skos.foo#qualifies",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Replies_To("repliesTo", "replies to", "http://test.skos.foo#repliesTo",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Cites_As_Data_Source("citesAsDataSource", "cites as data source",
      "http://test.skos.foo#citesAsDataSource",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Cites_As_Authority("citesAsAuthority", "cites as authority",
      "http://test.skos.foo#citesAsAuthority",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Cites_As_Metadata_Document("citesAsMetadataDocument", "cites as metadata document",
      "http://test.skos.foo#citesAsMetadataDocument",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Disagrees_With("disagreesWith", "disagrees with", "http://test.skos.foo#disagreesWith",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Links_To("linksTo", "links to", "http://test.skos.foo#linksTo",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Documents("documents", "documents", "http://test.skos.foo#documents",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Updates("updates", "updates", "http://test.skos.foo#updates",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Obtains_Background_From("obtainsBackgroundFrom", "obtains background from",
      "http://test.skos.foo#obtainsBackgroundFrom",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Reviews("reviews", "reviews", "http://test.skos.foo#reviews",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Cites_As_Evidence("citesAsEvidence", "cites as evidence", "http://test.skos.foo#citesAsEvidence",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Discusses("discusses", "discusses", "http://test.skos.foo#discusses",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Refutes("refutes", "refutes", "http://test.skos.foo#refutes",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Uses_Conclusions_From("usesConclusionsFrom", "uses conclusions from",
      "http://test.skos.foo#usesConclusionsFrom",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Contains_Assertion_From("containsAssertionFrom", "contains assertion from",
      "http://test.skos.foo#containsAssertionFrom",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Extends("extends", "extends", "http://test.skos.foo#extends",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Critiques("critiques", "critiques", "http://test.skos.foo#critiques",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Disputes("disputes", "disputes", "http://test.skos.foo#disputes",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Uses_Data_From("usesDataFrom", "uses data from", "http://test.skos.foo#usesDataFrom",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Includes_Excerpt_From("includesExcerptFrom", "includes excerpt from",
      "http://test.skos.foo#includesExcerptFrom",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Compiles("compiles", "compiles", "http://test.skos.foo#compiles",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Cites_As_Related("citesAsRelated", "cites as related", "http://test.skos.foo#citesAsRelated",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Supports("supports", "supports", "http://test.skos.foo#supports",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Cites_As_Potential_Solution("citesAsPotentialSolution", "cites as potential solution",
      "http://test.skos.foo#citesAsPotentialSolution",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Cites_For_Information("citesForInformation", "cites for information",
      "http://test.skos.foo#citesForInformation",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Obtains_Support_From("obtainsSupportFrom", "obtains support from",
      "http://test.skos.foo#obtainsSupportFrom",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Speculates_On("speculatesOn", "speculates on", "http://test.skos.foo#speculatesOn",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Corrects("corrects", "corrects", "http://test.skos.foo#corrects",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Credits("credits", "credits", "http://test.skos.foo#credits",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Uses_Method_In("usesMethodIn", "uses method in", "http://test.skos.foo#usesMethodIn",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites}),
  Retracts("retracts", "retracts", "http://test.skos.foo#retracts",
      new Term[]{Cito.Cites},
      new Term[]{Cito.Cites});


  public static final String schemeName = "cito";
  public static final String schemeID = "cito";
  public static final URIIdentifier schemeURI = new URIIdentifier()
      .withUri(URI.create("http://test.skos.foo#cito"));
  private static final URIIdentifier namespace = new URIIdentifier()
      .withUri(URI.create("http://test.skos.foo"));

  public static final ConceptScheme<Cito> __SCHEME = new DefaultConceptScheme<>(schemeID,
      schemeName,
      schemeURI.getUri(),
      null,
      Cito.class);

  private URI ref;
  private String displayName;
  private String code;
  private URI conceptId;

  private Term[] ancestors;
  private Term[] ancestorsClosure;

  Cito(String code, String displayName, String ref, Term[] ancestors, Term[] closure) {
    this.ref = URI.create(ref);
    this.code = code;
    this.displayName = displayName;
    this.ancestors = ancestors;
    this.ancestorsClosure = closure;
    this.conceptId = URI.create("http://test.skos.foo#" + code);
  }


  @Override
  public String getLabel() {
    return displayName;
  }

  @Override
  public String getTag() {
    return code;
  }

  @Override
  public URI getRef() {
    return ref;
  }

  @Override
  public URI getConceptId() {
    return conceptId;
  }

  @Override
  public URIIdentifier getNamespace() {
    return namespace;
  }

  @Override
  public Term[] getClosure() {
    return ancestorsClosure;
  }

  public Term[] getAncestors() {
    return ancestors;
  }

}
