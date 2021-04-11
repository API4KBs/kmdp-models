/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp;

import static edu.mayo.kmdp.util.Util.uuid;
import static edu.mayo.ontology.taxonomies.kmdo.citationreltype.BibliographicCitationTypeSeries.Cites;
import static edu.mayo.ontology.taxonomies.kmdo.citationreltype.BibliographicCitationTypeSeries.Cites_As_Authority;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._20200801.id.SemanticIdentifier.newId;
import static org.omg.spec.api4kp._20200801.surrogate.SurrogateBuilder.artifactId;
import static org.omg.spec.api4kp._20200801.taxonomy.clinicalknowledgeassettype.ClinicalKnowledgeAssetTypeSeries.Case_Enrichment_Rule;
import static org.omg.spec.api4kp._20200801.taxonomy.clinicalknowledgeassettype.ClinicalKnowledgeAssetTypeSeries.Clinical_Rule;
import static org.omg.spec.api4kp._20200801.taxonomy.dependencyreltype.DependencyTypeSeries.Depends_On;
import static org.omg.spec.api4kp._20200801.taxonomy.derivationreltype.DerivationTypeSeries.Is_Adaptation_Of;
import static org.omg.spec.api4kp._20200801.taxonomy.derivationreltype.DerivationTypeSeries.Is_Derived_From;
import static org.omg.spec.api4kp._20200801.taxonomy.derivationreltype.DerivationTypeSeries.Is_Revision_Of;
import static org.omg.spec.api4kp._20200801.taxonomy.iso639_2_languagecode.LanguageSeries.Italian;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeartifactcategory.KnowledgeArtifactCategorySeries.Software;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetcategory.KnowledgeAssetCategorySeries.Rules_Policies_And_Guidelines;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetrole.KnowledgeAssetRoleSeries.Operational_Concept_Definition;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeprocessingtechnique.KnowledgeProcessingTechniqueSeries.Query_Technique;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.TXT;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_1;
import static org.omg.spec.api4kp._20200801.taxonomy.krprofile.KnowledgeRepresentationLanguageProfileSeries.CQL_Essentials;
import static org.omg.spec.api4kp._20200801.taxonomy.krserialization.KnowledgeRepresentationLanguageSerializationSeries.DMN_1_1_XML_Syntax;
import static org.omg.spec.api4kp._20200801.taxonomy.languagerole.KnowledgeRepresentationLanguageRoleSeries.Schema_Language;
import static org.omg.spec.api4kp._20200801.taxonomy.lexicon.LexiconSeries.SNOMED_CT;
import static org.omg.spec.api4kp._20200801.taxonomy.publicationstatus.PublicationStatusSeries.Published;
import static org.omg.spec.api4kp._20200801.taxonomy.relatedversiontype.RelatedVersionTypeSeries.Has_Previous_Version;
import static org.omg.spec.api4kp._20200801.taxonomy.structuralreltype.StructuralPartTypeSeries.Has_Structural_Component;
import static org.omg.spec.api4kp._20200801.taxonomy.summaryreltype.SummarizationTypeSeries.Summarizes;
import static org.omg.spec.api4kp._20200801.taxonomy.variantreltype.VariantTypeSeries.Is_Translation_Of;

import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.kmdo.semanticannotationreltype.SemanticAnnotationRelTypeSeries;
import java.net.URI;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.surrogate.Annotation;
import org.omg.spec.api4kp._20200801.surrogate.Applicability;
import org.omg.spec.api4kp._20200801.surrogate.Citation;
import org.omg.spec.api4kp._20200801.surrogate.Component;
import org.omg.spec.api4kp._20200801.surrogate.Dependency;
import org.omg.spec.api4kp._20200801.surrogate.Derivative;
import org.omg.spec.api4kp._20200801.surrogate.KnowledgeArtifact;
import org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset;
import org.omg.spec.api4kp._20200801.surrogate.Publication;
import org.omg.spec.api4kp._20200801.surrogate.Summary;
import org.omg.spec.api4kp._20200801.surrogate.Variant;
import org.omg.spec.api4kp._20200801.surrogate.Version;
import org.omg.spec.api4kp._20200801.taxonomy.clinicalknowledgeassettype.ClinicalKnowledgeAssetTypeSeries;


public class AssetSurrogateJsonTest {

  @Test
  void testAssetCore() {
    KnowledgeAsset ks = new KnowledgeAsset()
        .withAssetId(newId("http://foo.bar/123", "234"));

    String jsonTree = toJson(ks);
    assertFalse(Util.isEmpty(jsonTree));
  }

  @Test
  void testApplicability() {
    KnowledgeAsset ks = new KnowledgeAsset()
        .withAssetId(newId("http://foo.bar/54123", "001"))
        .withApplicableIn(new Applicability()
            .withSituation(Term.mock("Example Situation", "x123").asConceptIdentifier())
        );

    String x = toJson(ks);
    ks = JSonUtil.parseJson(x, KnowledgeAsset.class).orElse(null);
    assertNotNull(ks);

    assertEquals("x123", ks.getApplicableIn().getSituation().get(0).getTag());
  }

  @Test
  void testRelated() {
    KnowledgeAsset ks = new KnowledgeAsset()
        .withAssetId(newId("http://foo.bar/random", "0.0.12"))
        .withLinks(new Dependency()
            .withRel(Depends_On)
            .withHref(newId("1", "1.0.0"))
        );

    String x = toJson(ks);
    ks = JSonUtil.parseJson(x, KnowledgeAsset.class).orElse(null);
    assertNotNull(ks);

    assertEquals(Depends_On.getLatest(),
        ((Dependency) ks.getLinks().get(0)).getRel());
  }

  @Test
  void testSeriesSerialization() {
    KnowledgeAsset ks = new KnowledgeAsset()
        .withAssetId(newId("http://foo.bar/7443", "142412"))
        .withFormalCategory(Rules_Policies_And_Guidelines.getLatest());

    String x = toJson(ks);
    ks = JSonUtil.parseJson(x, KnowledgeAsset.class).orElse(null);
    assertNotNull(ks);

    assertEquals(Rules_Policies_And_Guidelines.getLatest(),
        ks.getFormalCategory().get(0));
  }


  @Test
  void testDeserializationOfKnownVocabularies() {
    KnowledgeAsset ks = new KnowledgeAsset()
        .withAssetId(newId(URI.create("http://foo.bar"), "baz", "142412"))
        .withFormalCategory(Rules_Policies_And_Guidelines)
        .withFormalType(Clinical_Rule)
        .withProcessingMethod(Query_Technique)
        .withRole(Operational_Concept_Definition)
        .withLinks(
            new Component().withRel(Has_Structural_Component))
        .withLinks(
            new Derivative().withRel(Is_Adaptation_Of))
        .withLinks(
            new Dependency().withRel(Depends_On))
        .withLinks(
            new Variant().withRel(Is_Translation_Of))
        .withLinks(
            new Version().withRel(Has_Previous_Version))
        .withCitations(
            new Citation().withRel(Cites_As_Authority.asConceptIdentifier()))
        .withCarriers(
            new KnowledgeArtifact()
                .withLocalization(Italian)
                .withExpressionCategory(Software)
                .withSummary(
                    new Summary().withRel(Summarizes))
                .withRepresentation(new SyntacticRepresentation()
                    .withLanguage(DMN_1_1)
                    .withProfile(CQL_Essentials)
                    .withFormat(TXT)
                    .withLexicon(SNOMED_CT)
                    .withSerialization(DMN_1_1_XML_Syntax)
                    .withSubLanguage(new SyntacticRepresentation()
                        .withRole(Schema_Language))
                ).withLifecycle(
                new Publication()
                    .withPublicationStatus(Published))
        );

    String x = toJson(ks);
    ks = JSonUtil.parseJson(x, KnowledgeAsset.class).orElse(null);
    assertNotNull(ks);
  }


  @Test
  void testSerialization() {
    ResourceIdentifier assetId = newId(URI.create("http://foo.bar"), "234");
    KnowledgeAsset ks = new KnowledgeAsset()

        .withAssetId(assetId)

        .withFormalCategory(Rules_Policies_And_Guidelines.getLatest())

        .withDescription("This is a test")

        .withAnnotation(new Annotation()
            .withRel(SemanticAnnotationRelTypeSeries.Has_Primary_Subject.getLatest().asConceptIdentifier())
            .withRef(Term.mock("fooLabel", "123456").asConceptIdentifier()))

        .withLinks(new Derivative()
                .withRel(Is_Revision_Of)
                .withHref(artifactId(assetId.getNamespaceUri(),uuid("234"), "0.0.0")),
            new Derivative()
                .withRel(Is_Derived_From)
                .withHref(newId("http://foo.bar/234")))
        .withCitations(
            new Citation()
                .withRel(Cites.asConceptIdentifier())
                .withBibliography("Joe,D. On everything. 2001"));
    assertNotNull(ks);

    assertNotNull(toJson(ks));
  }

  @Test
  void testPolymorphicEnumerations() {
    KnowledgeAsset ks = new KnowledgeAsset()
        .withAssetId(newId("http://foo.bar", "234"))
        .withFormalType(Case_Enrichment_Rule);

    assertNotNull(toJson(ks));
  }


  private String toJson(Object x) {
    assertNotNull(x);

    String json = JSonUtil.writeJsonAsString(x)
        .orElse("");

    Optional<?> y = JSonUtil.readJson(json, x.getClass());
    assertTrue(y.isPresent());

    String json2 = y.flatMap(JSonUtil::writeJsonAsString).orElse("");
    assertEquals(json, json2);

    return json;
  }

}
