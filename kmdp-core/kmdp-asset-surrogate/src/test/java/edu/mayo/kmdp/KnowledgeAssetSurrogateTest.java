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

import static edu.mayo.kmdp.id.helper.DatatypeHelper.name;
import static edu.mayo.kmdp.id.helper.DatatypeHelper.uri;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.metadata.v2.surrogate.ComputableKnowledgeArtifact;
import edu.mayo.kmdp.metadata.v2.surrogate.Derivative;
import edu.mayo.kmdp.metadata.v2.surrogate.InlinedRepresentation;
import edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.metadata.v2.surrogate.ObjectFactory;
import edu.mayo.kmdp.metadata.v2.surrogate.Publication;
import edu.mayo.kmdp.metadata.v2.surrogate.Representation;
import edu.mayo.kmdp.metadata.v2.surrogate.SurrogateHelper;
import edu.mayo.kmdp.metadata.v2.surrogate.annotations.ComplexApplicability;
import edu.mayo.kmdp.metadata.v2.surrogate.annotations.SimpleAnnotation;
import edu.mayo.kmdp.metadata.v2.surrogate.annotations.SimpleApplicability;
import edu.mayo.kmdp.terms.TermsHelper;
import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.kmdp.util.XMLUtil;
import edu.mayo.kmdp.util.properties.jaxb.JaxbConfig.JaxbOptions;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetcategory.KnowledgeAssetCategory;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetcategory.KnowledgeAssetCategorySeries;
import edu.mayo.ontology.taxonomies.kao.knowledgeassettype.KnowledgeAssetTypeSeries;
import edu.mayo.ontology.taxonomies.kao.publicationstatus.PublicationStatusSeries;
import edu.mayo.ontology.taxonomies.kao.rel.derivationreltype.DerivationTypeSeries;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries;
import java.util.Collections;
import java.util.Optional;
import javax.xml.validation.Schema;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.identifiers.SimpleIdentifier;

public class KnowledgeAssetSurrogateTest {


  @Test
  void testKS() {
    KnowledgeAssetCategory br = KnowledgeAssetCategorySeries.Rules_Policies_And_Guidelines;
    KnowledgeAsset ks = new KnowledgeAsset()
        .withAssetId(uri("http://foo.bar/4523", "234"))
        .withName("Foo")
        .withFormalCategory(br)
        .withLifecycle(new Publication().withPublicationStatus(PublicationStatusSeries.Draft))
        .withSubject(new SimpleAnnotation().withExpr(TermsHelper.sct("mock", "123")));

    checkRoundTrip(ks);
  }

  @Test
  public void testApplicability() {
    KnowledgeAsset ks = new KnowledgeAsset()
        .withAssetId(uri("http://foo.bar", "142412"))
        .withName("Foo")
        .withApplicableIn(new SimpleApplicability()
            .withSituation(TermsHelper.mayo("Example Situation", "x123"))
        );
    assertNotNull(ks);
    ks = checkRoundTrip(ks);
    assertEquals("x123", ((SimpleApplicability) ks.getApplicableIn()).getSituation().getTag());
  }

  @Test
  /**
   * Minimal descriptive information is gathered about an asset,
   * which itself is implemented in an external system, typically in
   * a proprietary / not transparent form
   */
  void testScenarioBasicExternalImpl() {
    KnowledgeAsset ks = new KnowledgeAsset()

        .withAssetId(uri("http://foo.bar", "234"))
        .withName("Foo")

        .withFormalCategory(KnowledgeAssetCategorySeries.Rules_Policies_And_Guidelines)
        .withFormalType(KnowledgeAssetTypeSeries.Clinical_Rule)

        .withName("My Favorite Rule")
        .withDescription("When and Whether to Recommend What")

        .withLifecycle(new Publication().withPublicationStatus(PublicationStatusSeries.Draft))

        .withCarriers(new ComputableKnowledgeArtifact()
            .withArtifactId(uri("urn:to:do"))
            .withName("Bar")
            .withRepresentation(new Representation()
                .withLanguage(KnowledgeRepresentationLanguageSeries.KNART_1_3))
            // carrier + external catalog is not perfect
            .withSecondaryId(name("poc:RUL-12345"))
            .withInlined(new InlinedRepresentation()
                .withExpr("IF so and so DO nothing"))
            .withRelated(new Derivative()
                .withRel(DerivationTypeSeries.Derived_From)
                // should I have an inverse flag here?
                .withTgt(new ComputableKnowledgeArtifact()
                    .withArtifactId(uri("urn:TODO"))
                    .withName("TODO")
                    .withSecondaryId(new SimpleIdentifier().withTag("urn:epic:LGL-123")))
            ));

    checkRoundTrip(ks);

    assertEquals(KnowledgeAssetCategorySeries.Rules_Policies_And_Guidelines,
        ks.getFormalCategory().get(0));
  }


  private KnowledgeAsset checkRoundTrip(KnowledgeAsset ks) {
    ObjectFactory of = new ObjectFactory();

    String str = JaxbUtil.marshall(Collections.singleton(of.getClass()),
        ks,
        of::createKnowledgeAsset,
        JaxbUtil.defaultProperties()
            .with(JaxbOptions.SCHEMA_LOCATION,
                "http://kmdp.mayo.edu/metadata/v2/surrogate /xsd/metadata/v2/surrogate/surrogate.xsd"))
        .flatMap(Util::asString)
        .orElse("");
    assertFalse(Util.isEmpty(str));

    assertTrue(str.contains("xmlns:surr=\"http://kmdp.mayo.edu/metadata/v2/surrogate\""));

    Optional<Schema> schema = SurrogateHelper.getSchema();
    assertTrue(schema.isPresent());

    assertTrue(schema.map(s -> XMLUtil.validate(str, s)).orElse(false));

    KnowledgeAsset rec = XMLUtil.loadXMLDocument(str)
        .flatMap(dox -> JaxbUtil.unmarshall(ObjectFactory.class, KnowledgeAsset.class, dox))
        .orElse(null);

    assertEquals(ks, rec);
    return rec;
  }

  @Test
  void testSimpleApplicability() {
    KnowledgeAsset asset = new KnowledgeAsset();
    asset.withApplicableIn(new SimpleApplicability()
        .withSituation(TermsHelper.mayo("test", "123")));

    assertTrue(asset.getApplicableIn() instanceof SimpleApplicability);
  }

  @Test
  void testComplexApplicability() {
    KnowledgeAsset asset = new KnowledgeAsset();
    asset.withApplicableIn(new ComplexApplicability()
        .withSituation(new InlinedRepresentation()
            .withExpr("A or B")
            .withCodedRepresentationType("plain/txt")));

    assertTrue(asset.getApplicableIn() instanceof ComplexApplicability);
  }
}