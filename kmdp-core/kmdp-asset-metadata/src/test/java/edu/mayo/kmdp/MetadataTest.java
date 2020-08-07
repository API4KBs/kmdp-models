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

import static edu.mayo.kmdp.SurrogateHelper.toLegacyConceptIdentifier;
import static edu.mayo.kmdp.SurrogateHelper.uri;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.metadata.annotations.ComplexApplicability;
import edu.mayo.kmdp.metadata.annotations.SimpleAnnotation;
import edu.mayo.kmdp.metadata.annotations.SimpleApplicability;
import edu.mayo.kmdp.metadata.surrogate.ComputableKnowledgeArtifact;
import edu.mayo.kmdp.metadata.surrogate.Derivative;
import edu.mayo.kmdp.metadata.surrogate.InlinedRepresentation;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.metadata.surrogate.ObjectFactory;
import edu.mayo.kmdp.metadata.surrogate.Publication;
import edu.mayo.kmdp.metadata.surrogate.Representation;
import edu.mayo.kmdp.surrogate.SurrogateDiffer;
import edu.mayo.kmdp.terms.TermsHelper;
import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.kmdp.util.XMLUtil;
import edu.mayo.kmdp.util.properties.jaxb.JaxbConfig.JaxbOptions;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetcategory._20190801.KnowledgeAssetCategory;
import edu.mayo.ontology.taxonomies.kao.knowledgeassettype._20190801.KnowledgeAssetType;
import edu.mayo.ontology.taxonomies.kao.publicationstatus._2014_02_01.PublicationStatus;
import edu.mayo.ontology.taxonomies.kao.rel.derivationreltype._20190801.DerivationType;
import edu.mayo.ontology.taxonomies.krlanguage._20190801.KnowledgeRepresentationLanguage;
import java.util.Collections;
import java.util.Optional;
import javax.xml.validation.Schema;
import org.javers.core.diff.Diff;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.identifiers.ID;
import org.omg.spec.api4kp._1_0.identifiers.SimpleIdentifier;

public class MetadataTest {


  @Test
  void testKS() {
    KnowledgeAssetCategory br = KnowledgeAssetCategory.Rules_Policies_And_Guidelines;
    KnowledgeAsset ks = new KnowledgeAsset()
        .withAssetId(uri("http://foo.bar", "234"))
        .withName("Foo")
        .withFormalCategory(br)
        .withLifecycle(new Publication().withPublicationStatus(PublicationStatus.Draft))
        .withSubject(new SimpleAnnotation()
            .withExpr(toLegacyConceptIdentifier(TermsHelper.sct("mock", "123"))));

    checkRoundTrip(ks);
  }

  @Test
  public void testApplicability() {
    KnowledgeAsset ks = new KnowledgeAsset()
        .withAssetId(uri("http://foo.bar", "142412"))
        .withName("Foo")
        .withApplicableIn(new SimpleApplicability()
            .withSituation(
                toLegacyConceptIdentifier(TermsHelper.mayo("Example Situation", "x123")))
        );

    ks = checkRoundTrip(ks);
    assertEquals("x123",
        ((SimpleApplicability) ks.getApplicableIn()).getSituation().getTag());
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

        .withFormalCategory(KnowledgeAssetCategory.Rules_Policies_And_Guidelines)
        .withFormalType(KnowledgeAssetType.Clinical_Rule)

        .withName("My Favorite Rule")
        .withDescription("When and Whether to Recommend What")

        .withLifecycle(new Publication().withPublicationStatus(PublicationStatus.Draft))

        .withCarriers(new ComputableKnowledgeArtifact()
            .withArtifactId(uri("urn:to:do", "LATEST"))
            .withName("Bar")
            .withRepresentation(new Representation()
                .withLanguage(KnowledgeRepresentationLanguage.KNART_1_3))
            // carrier + external catalog is not perfect
            .withSecondaryId(name("poc:RUL-12345"))
            .withInlined(new InlinedRepresentation()
                .withExpr("IF so and so DO nothing"))
            .withRelated(new Derivative()
                .withRel(DerivationType.Derived_From)
                // should I have an inverse flag here?
                .withTgt(new ComputableKnowledgeArtifact()
                    .withArtifactId(uri("urn:TODO", "LATEST"))
                    .withName("TODO")
                    .withSecondaryId(new SimpleIdentifier().withTag("urn:epic:LGL-123")))
            ));

    checkRoundTrip(ks);

    assertEquals(KnowledgeAssetCategory.Rules_Policies_And_Guidelines,
        ks.getFormalCategory().get(0));
  }

  private ID name(String s) {
    return new SimpleIdentifier().withTag(s);
  }


  private KnowledgeAsset checkRoundTrip(KnowledgeAsset ks) {
    ObjectFactory of = new ObjectFactory();

    String str = JaxbUtil.marshall(Collections.singleton(of.getClass()),
        ks,
        of::createKnowledgeAsset,
        JaxbUtil.defaultProperties()
            .with(JaxbOptions.SCHEMA_LOCATION,
                "http://kmdp.mayo.edu/metadata/surrogate /xsd/metadata/surrogate/surrogate.xsd"))
        .flatMap(Util::asString)
        .orElse("");
    assertFalse(Util.isEmpty(str));

    assertTrue(str.contains("xmlns:surr=\"http://kmdp.mayo.edu/metadata/surrogate\""));

    Optional<Schema> schema = SurrogateHelper.getSchema();
    assertTrue(schema.isPresent());

    // XSD Schemas bind the terminologies to the new datatypes, not the legacy ones
    // schema.ifPresent(sc -> assertTrue( XMLUtil.validate(str, sc)));

    KnowledgeAsset rec = XMLUtil.loadXMLDocument(str)
        .flatMap(dox -> JaxbUtil.unmarshall(ObjectFactory.class, KnowledgeAsset.class, dox))
        .orElse(null);

    SurrogateDiffer sc = new SurrogateDiffer();
    Diff diff = sc.diff(ks,rec);
    assertFalse(diff.hasChanges());
    return rec;
  }

  @Test
  void testSimpleApplicability() {
    KnowledgeAsset asset = new KnowledgeAsset();
    asset.withApplicableIn(new SimpleApplicability()
        .withSituation(toLegacyConceptIdentifier(TermsHelper.mayo("test", "123"))));

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