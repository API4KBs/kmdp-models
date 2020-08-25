/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp;

import static org.omg.spec.api4kp._20200801.taxonomy.publicationstatus.PublicationStatusSeries.Draft;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._20200801.id.SemanticIdentifier.newId;
import static org.omg.spec.api4kp._20200801.surrogate.SurrogateBuilder.randomArtifactId;
import static org.omg.spec.api4kp._20200801.taxonomy.derivationreltype.DerivationTypeSeries.Is_Derived_From;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetcategory.KnowledgeAssetCategorySeries.Rules_Policies_And_Guidelines;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries.Clinical_Rule;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.HTML;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.KNART_1_3;

import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.kmdp.util.XMLUtil;
import edu.mayo.kmdp.util.properties.jaxb.JaxbConfig.JaxbOptions;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import javax.xml.validation.Schema;
import org.javers.core.diff.Diff;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.surrogate.Derivative;
import org.omg.spec.api4kp._20200801.surrogate.KnowledgeArtifact;
import org.omg.spec.api4kp._20200801.surrogate.ObjectFactory;
import org.omg.spec.api4kp._20200801.surrogate.Publication;
import org.omg.spec.api4kp._20200801.surrogate.SurrogateDiffer;
import org.omg.spec.api4kp._20200801.surrogate.SurrogateHelper;
import org.omg.spec.api4kp._20200801.surrogate.Annotation;
import org.omg.spec.api4kp._20200801.surrogate.Applicability;
import org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetcategory.KnowledgeAssetCategory;

public class KnowledgeAssetSurrogateTest {


  @Test
  void testKS() {
    KnowledgeAssetCategory br = Rules_Policies_And_Guidelines;
    KnowledgeAsset ks = new KnowledgeAsset()
        .withAssetId(newId(URI.create("http://foo.bar/"), "4523", "0.0.0"))
        .withName("Foo")
        .withFormalCategory(br)
            .withCarriers(new KnowledgeArtifact()
                .withArtifactId(randomArtifactId())
                .withLifecycle(new Publication()
                    .withIssuedOn(new Date())
                    .withPublicationStatus(Draft))
                .withRepresentation(new SyntacticRepresentation().withLanguage(HTML))
            )
        .withAnnotation(new Annotation()
            .withRef(Term.mock("mock", "123").asConceptIdentifier()));

    checkRoundTrip(ks);
  }

  @Test
  public void testApplicability() {
    KnowledgeAsset ks = new KnowledgeAsset()
        .withAssetId(newId(URI.create("http://foo.bar/"), "4523", "0.0.0"))
        .withName("Foo")
        .withApplicableIn(new Applicability()
            .withSituation(Term.mock("Example Situation", "x123").asConceptIdentifier())
        );
    assertNotNull(ks);
    ks = checkRoundTrip(ks);
    assertEquals("x123", ks.getApplicableIn().getSituation().get(0).getTag());
  }

  @Test
  /*
   * Minimal descriptive information is gathered about an asset,
   * which itself is implemented in an external system, typically in
   * a proprietary / not transparent form
   */
  void testScenarioBasicExternalImpl() {
    KnowledgeAsset ks = new KnowledgeAsset()

        .withAssetId(newId(URI.create("http://foo.bar/"), "4523", "0.0.0"))
        .withName("Foo")

        .withFormalCategory(Rules_Policies_And_Guidelines)
        .withFormalType(Clinical_Rule)

        .withName("My Favorite Rule")
        .withDescription("When and Whether to Recommend What")

        .withCarriers(new KnowledgeArtifact()
            .withArtifactId(newId(URI.create("urn:to:do")))
            .withName("Bar")
            .withLifecycle(new Publication().withPublicationStatus(Draft))
            .withRepresentation(new SyntacticRepresentation()
                .withLanguage(KNART_1_3))
            // carrier + external catalog is not perfect
            .withSecondaryId(newId(URI.create("urn:poc:RUL-12345")))
            .withInlinedExpression("IF so and so DO nothing"))

        .withLinks(new Derivative()
                .withRel(Is_Derived_From)
                // should I have an inverse flag here?
                .withHref(newId(URI.create("urn:name:TODO"))));

    checkRoundTrip(ks);

    assertEquals(Rules_Policies_And_Guidelines,
        ks.getFormalCategory().get(0));
  }


  private KnowledgeAsset checkRoundTrip(KnowledgeAsset ks) {
    ObjectFactory of = new ObjectFactory();

    String str = JaxbUtil.marshall(Collections.singleton(of.getClass()),
        ks,
        of::createKnowledgeAsset,
        JaxbUtil.defaultProperties()
            .with(JaxbOptions.SCHEMA_LOCATION,
                "https://www.omg.org/spec/API4KP/20200801/surrogate /xsd/API4KP/surrogate/surrogate.xsd"))
        .flatMap(Util::asString)
        .orElse("");
    assertFalse(Util.isEmpty(str));

    assertTrue(str.contains("xmlns:surr=\"https://www.omg.org/spec/API4KP/20200801/surrogate\""));

    Optional<Schema> schema = SurrogateHelper.getSchema();
    assertTrue(schema.isPresent());

    assertTrue(schema.map(s -> XMLUtil.validate(str, s)).orElse(false));

    KnowledgeAsset rec = XMLUtil.loadXMLDocument(str)
        .flatMap(dox -> JaxbUtil.unmarshall(ObjectFactory.class, KnowledgeAsset.class, dox))
        .orElse(null);

    SurrogateDiffer differ = new SurrogateDiffer();
    Diff diff = differ.diff(ks, rec);
    assertFalse(diff.hasChanges());
    return rec;
  }

  @Test
  void testSimpleApplicability() {
    KnowledgeAsset asset = new KnowledgeAsset();
    asset.withApplicableIn(new Applicability()
        .withSituation(Term.mock("test", "123").asConceptIdentifier()));

    assertNotNull(asset.getApplicableIn());
  }

  @Test
  void testComplexApplicability() throws UnsupportedEncodingException {
    KnowledgeAsset asset = new KnowledgeAsset();
    asset.withApplicableIn(new Applicability()
        .withSituation(
            Term.newTerm(
                URI.create("http://snomed.info/scg/"),
                URLEncoder.encode("A + B", Charset.defaultCharset().name()),
                "either A or B")
            .asConceptIdentifier()));

    assertNotNull(asset.getApplicableIn());
  }
}