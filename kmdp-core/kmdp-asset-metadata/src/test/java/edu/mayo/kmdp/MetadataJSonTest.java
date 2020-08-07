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
package edu.mayo.kmdp;

import static edu.mayo.kmdp.SurrogateHelper.toLegacyConceptIdentifier;
import static edu.mayo.kmdp.SurrogateHelper.uri;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.metadata.annotations.SimpleAnnotation;
import edu.mayo.kmdp.metadata.annotations.SimpleApplicability;
import edu.mayo.kmdp.metadata.surrogate.Citation;
import edu.mayo.kmdp.metadata.surrogate.Component;
import edu.mayo.kmdp.metadata.surrogate.ComputableKnowledgeArtifact;
import edu.mayo.kmdp.metadata.surrogate.Dependency;
import edu.mayo.kmdp.metadata.surrogate.Derivative;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.metadata.surrogate.Party;
import edu.mayo.kmdp.metadata.surrogate.Publication;
import edu.mayo.kmdp.metadata.surrogate.Representation;
import edu.mayo.kmdp.metadata.surrogate.SubLanguage;
import edu.mayo.kmdp.metadata.surrogate.Summary;
import edu.mayo.kmdp.metadata.surrogate.Variant;
import edu.mayo.kmdp.metadata.surrogate.Version;
import edu.mayo.kmdp.terms.TermsHelper;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.iso639_2_languagecodes.LanguageSeries;
import edu.mayo.ontology.taxonomies.kao.knowledgeartifactcategory.KnowledgeArtifactCategorySeries;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetcategory.KnowledgeAssetCategorySeries;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetrole.KnowledgeAssetRoleSeries;
import edu.mayo.ontology.taxonomies.kao.knowledgeassettype.KnowledgeAssetTypeSeries;
import edu.mayo.ontology.taxonomies.kao.knowledgeprocessingtechnique.KnowledgeProcessingTechniqueSeries;
import edu.mayo.ontology.taxonomies.kao.languagerole.KnowledgeRepresentationLanguageRoleSeries;
import edu.mayo.ontology.taxonomies.kao.publicationstatus.PublicationStatusSeries;
import edu.mayo.ontology.taxonomies.kao.publishingrole.PublishingRoleSeries;
import edu.mayo.ontology.taxonomies.kao.rel.citationreltype.BibliographicCitationTypeSeries;
import edu.mayo.ontology.taxonomies.kao.rel.dependencyreltype.DependencyTypeSeries;
import edu.mayo.ontology.taxonomies.kao.rel.derivationreltype.DerivationTypeSeries;
import edu.mayo.ontology.taxonomies.kao.rel.relatedversiontype.RelatedVersionTypeSeries;
import edu.mayo.ontology.taxonomies.kao.rel.structuralreltype.StructuralPartTypeSeries;
import edu.mayo.ontology.taxonomies.kao.rel.summaryreltype.SummarizationTypeSeries;
import edu.mayo.ontology.taxonomies.kao.rel.variantreltype.VariantTypeSeries;
import edu.mayo.ontology.taxonomies.kmdo.annotationreltype.AnnotationRelTypeSeries;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries;
import edu.mayo.ontology.taxonomies.krprofile.KnowledgeRepresentationLanguageProfileSeries;
import edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries;
import edu.mayo.ontology.taxonomies.lexicon.LexiconSeries;
import java.io.InputStream;
import java.util.Optional;
import org.junit.jupiter.api.Test;


public class MetadataJSonTest {

  @Test
  void testAssetCore() {
    KnowledgeAsset ks = new KnowledgeAsset()
        .withAssetId(uri("http://foo.bar/123", "234"));

    String jsonTree = toJson(ks);
    assertFalse(Util.isEmpty(jsonTree));
  }

  @Test
  void testApplicability() {
    KnowledgeAsset ks = new KnowledgeAsset()
        .withAssetId(uri("http://foo.bar/789", "0001"))
        .withApplicableIn(new SimpleApplicability()
            .withSituation(
                toLegacyConceptIdentifier(TermsHelper.mayo("Example Situation","x123")))
        );

    String x = toJson(ks);
    ks = JSonUtil.parseJson(x,KnowledgeAsset.class).orElse(null);
    assertNotNull(ks);

    assertEquals("x123", ((SimpleApplicability)ks.getApplicableIn()).getSituation().getTag());
  }

  @Test
  void testRelated() {
    KnowledgeAsset ks = new KnowledgeAsset()
        .withAssetId(uri("http://foo./xyz", "1.0.0"))
        .withRelated(new Dependency()
            .withRel(DependencyTypeSeries.Depends_On)
            .withTgt(new KnowledgeAsset())
        );

    String x = toJson(ks);
    ks = JSonUtil.parseJson(x,KnowledgeAsset.class).orElse(null);
    assertNotNull(ks);

    assertEquals(DependencyTypeSeries.Depends_On.getLatest(),
        ((Dependency)ks.getRelated().get(0)).getRel());
  }

  @Test
  void testSeriesSerialization() {
    KnowledgeAsset ks = new KnowledgeAsset()
        .withAssetId(uri("http://foo.bar/5435", "142412"))
        .withFormalCategory(KnowledgeAssetCategorySeries.Rules_Policies_And_Guidelines.getLatest());

    String x = toJson(ks);
    ks = JSonUtil.parseJson(x,KnowledgeAsset.class).orElse(null);
    assertNotNull(ks);

    assertEquals(KnowledgeAssetCategorySeries.Rules_Policies_And_Guidelines.getLatest(),
        ks.getFormalCategory().get(0));
  }


  @Test
  void testDeserializationOfKnownVocabularies() {
    KnowledgeAsset ks = new KnowledgeAsset()
        .withAssetId(uri("http://foo.bar", "142412"))
        .withFormalCategory(KnowledgeAssetCategorySeries.Rules_Policies_And_Guidelines)
        .withFormalType(KnowledgeAssetTypeSeries.Clinical_Rule)
        .withProcessingMethod(KnowledgeProcessingTechniqueSeries.Logic_Based_Technique)
        .withRole(KnowledgeAssetRoleSeries.Operational_Concept_Definition)
        .withRelated(
            new Component().withRel(StructuralPartTypeSeries.Has_Part))
        .withRelated(
            new Derivative().withRel(DerivationTypeSeries.Inspired_By))
        .withRelated(
            new Dependency().withRel(DependencyTypeSeries.Depends_On))
        .withRelated(
            new Variant().withRel(VariantTypeSeries.Adaptation_Of))
        .withRelated(
            new Version().withRel(RelatedVersionTypeSeries.Has_Original))
        .withCitations(
            new Citation().withRel(BibliographicCitationTypeSeries.Cites_As_Authority))
        .withLifecycle(
            new Publication()
                .withPublicationStatus(PublicationStatusSeries.Published)
                .withAssociatedTo(new Party().withPublishingRole(PublishingRoleSeries.Author))
        )
        .withCarriers(
            new ComputableKnowledgeArtifact()
                .withLocalization(LanguageSeries.Italian)
                .withExpressionCategory(KnowledgeArtifactCategorySeries.Software)
                .withSummary(
                    new Summary().withRel(SummarizationTypeSeries.Compact_Representation_Of))
              .withRepresentation(new Representation()
                  .withLanguage(KnowledgeRepresentationLanguageSeries.DMN_1_1)
                  .withProfile(KnowledgeRepresentationLanguageProfileSeries.CQL_Essentials)
                  .withFormat(SerializationFormatSeries.TXT)
                  .withLexicon(LexiconSeries.SNOMED_CT)
                  .withSerialization(KnowledgeRepresentationLanguageSerializationSeries.DMN_1_1_XML_Syntax)
                  .withWith(
                      new SubLanguage().withRole(KnowledgeRepresentationLanguageRoleSeries.Schema_Language))
              )
        );

    String x = toJson(ks);
    ks = JSonUtil.parseJson(x,KnowledgeAsset.class).orElse(null);
    assertNotNull(ks);
  }

  @Test
  void testParseLegacySurrogate() {
    InputStream in = MetadataJSonTest.class.getResourceAsStream("/exampleSurr.json");

    KnowledgeAsset ks = JSonUtil.readJson(in,KnowledgeAsset.class).orElse(null);
    assertNotNull(ks);
  }


  @Test
  void testSerialization() {
    KnowledgeAsset ks = new KnowledgeAsset()

        .withAssetId(uri("http://foo.bar", "234"))

        .withFormalCategory(KnowledgeAssetCategorySeries.Rules_Policies_And_Guidelines.getLatest())

        .withDescription("This is a test")

        .withSubject(new SimpleAnnotation()
            .withRel(toLegacyConceptIdentifier(AnnotationRelTypeSeries.Has_Primary_Subject.getLatest()))
            .withExpr(toLegacyConceptIdentifier(TermsHelper.mayo("fooLabel", "123456"))))

        .withRelated(new Derivative()
                .withRel(DerivationTypeSeries.Abdridgement_Of)
                .withTgt(new ComputableKnowledgeArtifact()
                    .withArtifactId(uri("http://foo.bar/234", "LATEST"))),
            new Derivative()
                .withRel(DerivationTypeSeries.Derived_From)
                .withTgt(new KnowledgeAsset()
                    .withAssetId(uri("http://foo.bar/234", "LATEST"))))
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
    assertEquals(json,json2);

    return json;
  }


}
