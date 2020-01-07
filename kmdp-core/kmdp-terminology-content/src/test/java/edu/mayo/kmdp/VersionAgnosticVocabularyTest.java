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

import static edu.mayo.kmdp.util.Util.ensureUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.registry.Registry;
import edu.mayo.kmdp.series.Versionable;
import edu.mayo.kmdp.terms.ConceptTerm;
import edu.mayo.kmdp.util.DateTimeUtil;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperation;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevel;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries;
import edu.mayo.ontology.taxonomies.ccgentries.ConceptDefinitionTypeSeries;
import edu.mayo.ontology.taxonomies.kao.decisiontype.DecisionTypeSeries;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetcategory.KnowledgeAssetCategory;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetcategory.KnowledgeAssetCategorySeries;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetrole.KnowledgeAssetRole;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetrole.KnowledgeAssetRoleSeries;
import edu.mayo.ontology.taxonomies.kao.knowledgeassettype.KnowledgeAssetType;
import edu.mayo.ontology.taxonomies.kao.knowledgeassettype.KnowledgeAssetTypeSeries;
import edu.mayo.ontology.taxonomies.kao.knowledgeprocessingtechnique.KnowledgeProcessingTechnique;
import edu.mayo.ontology.taxonomies.kao.knowledgeprocessingtechnique.KnowledgeProcessingTechniqueSeries;
import edu.mayo.ontology.taxonomies.kao.languagerole.KnowledgeRepresentationLanguageRole;
import edu.mayo.ontology.taxonomies.kao.languagerole.KnowledgeRepresentationLanguageRoleSeries;
import edu.mayo.ontology.taxonomies.kao.publicationeventtype.PublicationEventType;
import edu.mayo.ontology.taxonomies.kao.publicationeventtype.PublicationEventTypeSeries;
import edu.mayo.ontology.taxonomies.kao.rel.dependencyreltype.DependencyType;
import edu.mayo.ontology.taxonomies.kao.rel.dependencyreltype.DependencyTypeSeries;
import edu.mayo.ontology.taxonomies.kao.rel.derivationreltype.DerivationType;
import edu.mayo.ontology.taxonomies.kao.rel.derivationreltype.DerivationTypeSeries;
import edu.mayo.ontology.taxonomies.kao.rel.relatedversiontype.RelatedVersionType;
import edu.mayo.ontology.taxonomies.kao.rel.relatedversiontype.RelatedVersionTypeSeries;
import edu.mayo.ontology.taxonomies.kao.rel.structuralreltype.StructuralPartType;
import edu.mayo.ontology.taxonomies.kao.rel.structuralreltype.StructuralPartTypeSeries;
import edu.mayo.ontology.taxonomies.kao.rel.summaryreltype.SummarizationType;
import edu.mayo.ontology.taxonomies.kao.rel.summaryreltype.SummarizationTypeSeries;
import edu.mayo.ontology.taxonomies.kao.rel.variantreltype.VariantType;
import edu.mayo.ontology.taxonomies.kao.rel.variantreltype.VariantTypeSeries;
import edu.mayo.ontology.taxonomies.kmdo.annotationreltype.AnnotationRelType;
import edu.mayo.ontology.taxonomies.kmdo.annotationreltype.AnnotationRelTypeSeries;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormat;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries;
import edu.mayo.ontology.taxonomies.krprofile.KnowledgeRepresentationLanguageProfile;
import edu.mayo.ontology.taxonomies.krprofile.KnowledgeRepresentationLanguageProfileSeries;
import edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerialization;
import edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries;
import edu.mayo.ontology.taxonomies.lexicon.Lexicon;
import edu.mayo.ontology.taxonomies.lexicon.LexiconSeries;
import edu.mayo.ontology.taxonomies.skos.relatedconcept.RelatedConcept;
import edu.mayo.ontology.taxonomies.skos.relatedconcept.RelatedConceptSeries;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class VersionAgnosticVocabularyTest {

  @Test
  public void testGeneratedEnums() {
    assertNotNull(KnowledgeRepresentationLanguageSeries.DMN_1_1);
    assertNotNull(KnowledgeRepresentationLanguageProfileSeries.OWL2_Full);
    assertNotNull(SerializationFormatSeries.JSON);
    assertNotNull(KnowledgeRepresentationLanguageSerializationSeries.DMN_1_1_XML_Syntax);
    assertNotNull(LexiconSeries.LOINC);
    assertNotNull(DerivationTypeSeries.Derived_From);
    assertNotNull(VariantTypeSeries.Rearrangement_Of);
    assertNotNull(SummarizationTypeSeries.Digest_Of);
    assertNotNull(DependencyTypeSeries.Depends_On);
    assertNotNull(RelatedVersionTypeSeries.Has_Original);
    assertNotNull(StructuralPartTypeSeries.Has_Part);
    assertNotNull(KnowledgeAssetCategorySeries.Rules_Policies_And_Guidelines);
    assertNotNull(KnowledgeProcessingTechniqueSeries.Natural_Language_Processing_Technique);
    assertNotNull(KnowledgeAssetTypeSeries.Decision_Model);
    assertNotNull(RelatedConceptSeries.Has_Broader);
    assertNotNull(KnowledgeRepresentationLanguageRoleSeries.Expression_Language);
    assertNotNull(KnowledgeProcessingOperationSeries.Translation_Task);
    assertNotNull(ParsingLevelSeries.Encoded_Knowledge_Expression);
    assertNotNull(KnowledgeAssetRoleSeries.Operational_Concept_Definition);
    assertNotNull(PublicationEventTypeSeries.Authoring);
    assertNotNull(AnnotationRelTypeSeries.Defines);
    assertNotNull(DecisionTypeSeries.Aggregation_Decision);
    assertNotNull(ConceptDefinitionTypeSeries.Interactive_Concept_Definition);
  }


  @Test
  public void testKnownTags() {

    assertEquals("ofn",
        KnowledgeRepresentationLanguageSerializationSeries.OWL_Functional_Syntax.getTag());

    assertEquals("dmn-v12",
        KnowledgeRepresentationLanguageSeries.DMN_1_2.getTag());

    assertEquals("lnc",
        LexiconSeries.LOINC.getTag());

    assertEquals("http://snomed.info/sct/900000000000207008/version/20180731",
        LexiconSeries.SNOMED_CT.getRef().toString());

    assertEquals("fr",
        edu.mayo.ontology.taxonomies.iso639_1_languagecodes._20190201.Language.French.getTag());

  }



  @Test
  public void testReferents() {
    assertEquals("https://www.omg.org/spec/DMN/1.2/",
        KnowledgeRepresentationLanguageSeries.DMN_1_2.getRef().toString());
  }

  @Test
  public void testKnownIdentifiers() {

    assertEquals("https://ontology.mayo.edu/taxonomies/KAO/KnowledgeAssetType#6047674c-0d9b-3c81-89a3-6943f3a7169b",
        KnowledgeAssetTypeSeries.Nursing_Protocol.getConceptId().toString());

    assertEquals("https://ontology.mayo.edu/taxonomies/KAO/KnowledgeAssetType#56b58fc2-b66f-3175-878e-bc3ef01cb916",
        KnowledgeAssetTypeSeries.Semantic_Decision_Model.getConceptId().toString());

    assertEquals("https://ontology.mayo.edu/taxonomies/KAO/KnowledgeAssetCategory#d4b0e868-60c8-387d-a139-e3c35427bfb6",
        KnowledgeAssetCategorySeries.Assessment_Predictive_And_Inferential_Models.getConceptId().toString());

    assertEquals("https://ontology.mayo.edu/taxonomies/KRLanguage#0bf050a2-fbd6-38c2-a4ce-323fd91c7b24",
        KnowledgeRepresentationLanguageSeries.DMN_1_2.getConceptId().toString());
  }


  @Test
  public void testGeneratedEnumsVersion() {
    Optional<UUID> uid = ensureUUID(KnowledgeAssetCategory.SCHEME_ID);
    assertTrue(uid.isPresent());

    assertNull(KnowledgeAssetCategory.seriesUri.getVersionId());
  }

  @Test
  public void testKRLanguages() {
    assertNotNull(KnowledgeRepresentationLanguageSeries.KNART_1_3);

    assertEquals(KnowledgeRepresentationLanguageSerializationSeries.DMN_1_1_XML_Syntax,
        Registry.getValidationSchema(KnowledgeRepresentationLanguageSeries.DMN_1_1.getRef())
            .flatMap(KnowledgeRepresentationLanguageSerializationSeries::resolveRef)
            .orElse(null));

    assertNotNull(KnowledgeRepresentationLanguageProfileSeries.CQL_Essentials);
    assertNotNull(KnowledgeRepresentationLanguageProfileSeries.GraphQL_Queries);
    assertNotNull(KnowledgeRepresentationLanguageProfileSeries.GraphQL_Schemas);
  }

  @Test
  public void testHistory() {
    Date effectiveDate = KnowledgeRepresentationLanguageSeries.DMN_1_2
        .asOf(DateTimeUtil.parseDate("2019-09-15"))
        .map(Versionable::getVersionEstablishedOn)
        .orElse(null);
    assertNotNull(effectiveDate);
    assertEquals("2019-08-01",DateTimeUtil.format(effectiveDate));
  }


  @Test
  public void testKnownVersions() {
    assertEquals(2, DecisionTypeSeries.schemeVersions.size());
  }

}