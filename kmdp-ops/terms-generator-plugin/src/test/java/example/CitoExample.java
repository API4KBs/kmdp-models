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
package example;

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.id.helper.DatatypeHelper;
import edu.mayo.kmdp.terms.MockTermsJsonAdapter;
import edu.mayo.kmdp.terms.Taxonomic;
import edu.mayo.kmdp.terms.MockTermsXMLAdapter;
import edu.mayo.kmdp.terms.example.Cito;
import java.util.Arrays;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;

import java.net.URI;
import java.util.Optional;


@javax.xml.bind.annotation.XmlType(name = "CitoExample")
@javax.xml.bind.annotation.XmlEnum
@javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter(CitoExample.Adapter.class)

public enum CitoExample implements Term, Taxonomic<CitoExample> {


  @javax.xml.bind.annotation.XmlEnumValue("cites")

  Cites("http://org.test.terms/CitoExample#cites", "cites", "cites",
      "http://purl.org/spar/cito#cites",
      new Term[]{},
      new Term[]{});


  public static final String schemeName = "CitoExample_Scheme";
  public static final String schemeID = "CitoExample_Scheme";


  public static final URIIdentifier schemeURI = new URIIdentifier()
      .withUri(URI.create("http://org.test.terms/CitoExample#CitoExample_Scheme"))
      .withVersionId(URI.create("http://org.test.terms/CitoExample#CitoExample_Scheme"));

  public static final NamespaceIdentifier __SCHEME = new NamespaceIdentifier()
      .withId(schemeURI.getUri())
      .withLabel(schemeName)
      .withTag(schemeID)
      .withVersion(DatatypeHelper.versionOf(schemeURI.getVersionId(), schemeURI.getUri()));


  private URI ref;
  private String displayName;
  private String tag;
  private URI conceptId;

  private Term[] ancestors;
  private Term[] ancestorsClosure;

  CitoExample(String conceptId, String code, String displayName, String referent, Term[] ancestors,
      Term[] closure) {
    this.ref = URI.create(referent);
    this.tag = code;
    this.displayName = displayName;
    this.ancestors = ancestors;
    this.ancestorsClosure = closure;
    this.conceptId = URI.create(conceptId);
  }

  @Override
  public String getLabel() {
    return displayName;
  }

  @Override
  public String getTag() {
    return tag;
  }

  @Override
  public URI getRef() {
    return ref;
  }

  @Override
  public NamespaceIdentifier getNamespace() {
    return __SCHEME;
  }

  @Override
  public URI getConceptId() {
    return conceptId;
  }

  @Override
  public Term[] getClosure() {
    return ancestorsClosure;
  }

  public Term[] getAncestors() {
    return ancestors;
  }

  public org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier asConcept() {
    return CitoExample.Adapter.instance.marshal(this);
  }

  public org.omg.spec.api4kp._1_0.identifiers.QualifiedIdentifier asQualified() {
    return edu.mayo.kmdp.id.helper.DatatypeHelper.toQualifiedIdentifier(this.ref);
  }

  public static Optional<CitoExample> resolve(final Term trm) {
    return Arrays.stream(CitoExample.values())
        .filter((x) -> trm.getRef().equals(x.getRef()))
        .findAny();
  }

  public static class Adapter extends MockTermsXMLAdapter {

    public static final MockTermsXMLAdapter instance = new Adapter();

    @Override
    protected Term[] getValues() {
      return values();
    }
  }

  public static class JsonAdapter extends MockTermsJsonAdapter.Deserializer {

    public static final MockTermsJsonAdapter.Deserializer instance = new JsonAdapter();

    @Override
    protected Term[] getValues() {
      return values();
    }
  }
}
