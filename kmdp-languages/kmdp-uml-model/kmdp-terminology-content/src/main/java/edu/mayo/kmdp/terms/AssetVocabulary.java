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
package edu.mayo.kmdp.terms;

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.id.helper.DatatypeHelper;
import edu.mayo.kmdp.util.URIUtil;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;

import java.net.URI;
import java.util.Optional;


@javax.xml.bind.annotation.XmlType(name = "CitoExample")
@javax.xml.bind.annotation.XmlEnum
@javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter(AssetVocabulary.Adapter.class)

@com.fasterxml.jackson.databind.annotation.JsonSerialize(as = org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier.class, using = edu.mayo.kmdp.terms.TermsJsonAdapter.Serializer.class)
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = edu.mayo.kmdp.terms.TermsJsonAdapter.Deserializer.class)

public enum AssetVocabulary implements Term {

  // on models
  HAS_ID("identified by",
      URI.create("http://www.omg.org/spec/API4KP/core#identified-by")),

  IS_A("is a",
      URI.create("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")),

  CAPTURES("captures",
      URI.create("http://www.omg.org/spec/API4KP/core#captures")),

  DEFINES("defines",
      URI.create("http://www.omg.org/spec/API4KP/core#defines")),

  REPRESENTS("representation of a",
      URI.create("http://www.omg.org/spec/API4KP/core#knowledge-representation-of")),

  IN_TERMS_OF("defines-in-terms-of",
      URI.create("http://www.omg.org/spec/API4KP/core#defines-in-terms-of")),

  HAS_SUBJECT("subject",
      URI.create("http://purl.org/dc/terms/subject"));


  public static final String schemeName = "AssetVocabulary_Scheme";
  public static final String schemeID = "AssetVocabulary_Scheme";

  public static final URIIdentifier schemeURI = new URIIdentifier()
      .withUri(URI.create("http://terms.mayo.edu/assets#AssetVocabulary_Scheme"))
      .withVersionId(URI.create("http://terms.mayo.edu/assets#AssetVocabulary_Scheme"));

  public static final NamespaceIdentifier __SCHEME = new NamespaceIdentifier()
      .withId(schemeURI.getUri())
      .withLabel(schemeName)
      .withTag(schemeID)
      .withVersion(DatatypeHelper.versionOf(schemeURI.getVersionId(), schemeURI.getUri()));

  //
  private URI ref;
  private String label;
  private String tag;
  private URI conceptId;

  //
  AssetVocabulary(String label, URI ref) {
    this.ref = ref;
    this.tag = ref.getFragment();
    this.label = label;
    this.conceptId = URIUtil.fromNamespacedFragment(tag, ref);
  }

  //
  public org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier asConcept() {
    return Adapter.instance.marshal(this);
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
  public String getLabel() {
    return label;
  }

  @Override
  public NamespaceIdentifier getNamespace() {
    return __SCHEME;
  }

  @Override
  public String getTag() {
    return tag;
  }

  public static Optional<AssetVocabulary> resolve(final Term trm) {
    return KMDPTerms.directory.resolve(trm, AssetVocabulary.class);
  }

  public static Optional<AssetVocabulary> resolve(final String tag) {
    return KMDPTerms.directory.resolve(tag, AssetVocabulary.class);
  }

  public static Optional<AssetVocabulary> resolveRef(final String refUri) {
    return KMDPTerms.directory.resolveRef(refUri, AssetVocabulary.class);
  }

  public static class Adapter extends TermsXMLAdapter {

    public static final TermsXMLAdapter instance = new Adapter();

    @Override
    protected Term[] getValues() {
      return values();
    }
  }

}
