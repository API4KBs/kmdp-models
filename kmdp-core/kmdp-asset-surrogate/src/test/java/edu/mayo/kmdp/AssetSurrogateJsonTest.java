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

import static edu.mayo.ontology.taxonomies.iso639_2_languagecodes.LanguageSeries.Italian;
import static edu.mayo.ontology.taxonomies.kao.knowledgeartifactcategory.KnowledgeArtifactCategorySeries.Software;
import static edu.mayo.ontology.taxonomies.kao.knowledgeassetcategory.KnowledgeAssetCategorySeries.Rules_Policies_And_Guidelines;
import static edu.mayo.ontology.taxonomies.kao.knowledgeassettype.KnowledgeAssetTypeSeries.Clinical_Rule;
import static edu.mayo.ontology.taxonomies.kao.knowledgeprocessingtechnique.KnowledgeProcessingTechniqueSeries.Logic_Based_Technique;
import static edu.mayo.ontology.taxonomies.kao.languagerole.KnowledgeRepresentationLanguageRoleSeries.Schema_Language;
import static edu.mayo.ontology.taxonomies.kao.publicationstatus.PublicationStatusSeries.Published;
import static edu.mayo.ontology.taxonomies.kao.rel.citationreltype.BibliographicCitationTypeSeries.Cites_As_Authority;
import static edu.mayo.ontology.taxonomies.kao.rel.dependencyreltype.DependencyTypeSeries.Depends_On;
import static edu.mayo.ontology.taxonomies.kao.rel.derivationreltype.DerivationTypeSeries.Abdridgement_Of;
import static edu.mayo.ontology.taxonomies.kao.rel.derivationreltype.DerivationTypeSeries.Derived_From;
import static edu.mayo.ontology.taxonomies.kao.rel.derivationreltype.DerivationTypeSeries.Inspired_By;
import static edu.mayo.ontology.taxonomies.kao.rel.relatedversiontype.RelatedVersionTypeSeries.Has_Original;
import static edu.mayo.ontology.taxonomies.kao.rel.structuralreltype.StructuralPartTypeSeries.Has_Part;
import static edu.mayo.ontology.taxonomies.kao.rel.summaryreltype.SummarizationTypeSeries.Compact_Representation_Of;
import static edu.mayo.ontology.taxonomies.kao.rel.variantreltype.VariantTypeSeries.Adaptation_Of;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.TXT;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_1;
import static edu.mayo.ontology.taxonomies.krprofile.KnowledgeRepresentationLanguageProfileSeries.CQL_Essentials;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.DMN_1_1_XML_Syntax;
import static edu.mayo.ontology.taxonomies.lexicon.LexiconSeries.SNOMED_CT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._1_0.id.SemanticIdentifier.newId;

import edu.mayo.kmdp.metadata.v2.surrogate.Citation;
import edu.mayo.kmdp.metadata.v2.surrogate.Component;
import edu.mayo.kmdp.metadata.v2.surrogate.ComputableKnowledgeArtifact;
import edu.mayo.kmdp.metadata.v2.surrogate.Dependency;
import edu.mayo.kmdp.metadata.v2.surrogate.Derivative;
import edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.metadata.v2.surrogate.Publication;
import edu.mayo.kmdp.metadata.v2.surrogate.Summary;
import edu.mayo.kmdp.metadata.v2.surrogate.SurrogateBuilder;
import edu.mayo.kmdp.metadata.v2.surrogate.Variant;
import edu.mayo.kmdp.metadata.v2.surrogate.Version;
import edu.mayo.kmdp.metadata.v2.surrogate.annotations.Annotation;
import edu.mayo.kmdp.metadata.v2.surrogate.annotations.Applicability;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetrole.KnowledgeAssetRoleSeries;
import edu.mayo.ontology.taxonomies.kao.rel.citationreltype.BibliographicCitationTypeSeries;
import edu.mayo.ontology.taxonomies.kmdo.annotationreltype.AnnotationRelTypeSeries;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.id.Term;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;


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
        .withAssetId(newId("http://foo.bar", "142412"))
        .withFormalCategory(Rules_Policies_And_Guidelines)
        .withFormalType(Clinical_Rule)
        .withProcessingMethod(Logic_Based_Technique)
        .withRole(KnowledgeAssetRoleSeries.Operational_Concept_Definition)
        .withLinks(
            new Component().withRel(Has_Part))
        .withLinks(
            new Derivative().withRel(Inspired_By))
        .withLinks(
            new Dependency().withRel(Depends_On))
        .withLinks(
            new Variant().withRel(Adaptation_Of))
        .withLinks(
            new Version().withRel(Has_Original))
        .withCitations(
            new Citation().withRel(Cites_As_Authority))
        .withCarriers(
            new ComputableKnowledgeArtifact()
                .withLocalization(Italian)
                .withExpressionCategory(Software)
                .withSummary(
                    new Summary().withRel(Compact_Representation_Of))
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
    KnowledgeAsset ks = new KnowledgeAsset()

        .withAssetId(newId("http://foo.bar", "234"))

        .withFormalCategory(Rules_Policies_And_Guidelines.getLatest())

        .withDescription("This is a test")

        .withAnnotation(new Annotation()
            .withRel(AnnotationRelTypeSeries.Has_Primary_Subject.getLatest().asConceptIdentifier())
            .withRef(Term.mock("fooLabel", "123456").asConceptIdentifier()))

        .withLinks(new Derivative()
                .withRel(Abdridgement_Of)
                .withHref(SurrogateBuilder.artifactId("234", "0.0.0")),
            new Derivative()
                .withRel(Derived_From)
                .withHref(newId("http://foo.bar/234")))
        .withCitations(
            new Citation()
                .withRel(BibliographicCitationTypeSeries.Cites)
                .withBibliography("Joe,D. On everything. 2001"));
    assertNotNull(ks);

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
