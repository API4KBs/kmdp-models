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
import edu.mayo.kmdp.util.DateTimeUtil;
import edu.mayo.ontology.taxonomies.api4kp.responsecodes.ResponseCodeSeries;
import edu.mayo.ontology.taxonomies.ccgentries.ConceptDefinitionTypeSeries;
import edu.mayo.ontology.taxonomies.kao.decisiontype.DecisionTypeSeries;
import edu.mayo.ontology.taxonomies.kmdo.annotationreltype.AnnotationRelTypeSeries;
import edu.mayo.ontology.taxonomies.kmdo.citationreltype.BibliographicCitationTypeSeries;
import edu.mayo.ontology.taxonomies.kmdo.publicationeventtype.PublicationEventTypeSeries;
import edu.mayo.ontology.taxonomies.kmdo.publicationstatus.PublicationStatusSeries;
import edu.mayo.ontology.taxonomies.kmdo.publishingrole.PublishingRoleSeries;
import edu.mayo.ontology.taxonomies.mimetype.MIMETypeSeries;
import edu.mayo.ontology.taxonomies.skos.relatedconcept.RelatedConceptSeries;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp.taxonomies.iso639_1_languagecodes.LanguageSeries;
import org.omg.spec.api4kp.taxonomies.iso639_1_languagecodes._20190201.Language;
import org.omg.spec.api4kp.taxonomies.kao.knowledgeartifactcategory.KnowledgeArtifactCategorySeries;
import org.omg.spec.api4kp.taxonomies.kao.knowledgeassetcategory.KnowledgeAssetCategory;
import org.omg.spec.api4kp.taxonomies.kao.knowledgeassetcategory.KnowledgeAssetCategorySeries;
import org.omg.spec.api4kp.taxonomies.kao.knowledgeassetrole.KnowledgeAssetRoleSeries;
import org.omg.spec.api4kp.taxonomies.kao.knowledgeassettype.KnowledgeAssetTypeSeries;
import org.omg.spec.api4kp.taxonomies.knowledgeoperations.KnowledgeProcessingOperationSeries;
import org.omg.spec.api4kp.taxonomies.knowledgeprocessingtechnique.KnowledgeProcessingTechniqueSeries;
import org.omg.spec.api4kp.taxonomies.krformat.SerializationFormatSeries;
import org.omg.spec.api4kp.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries;
import org.omg.spec.api4kp.taxonomies.krprofile.KnowledgeRepresentationLanguageProfileSeries;
import org.omg.spec.api4kp.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries;
import org.omg.spec.api4kp.taxonomies.languagerole.KnowledgeRepresentationLanguageRoleSeries;
import org.omg.spec.api4kp.taxonomies.lexicon.LexiconSeries;
import org.omg.spec.api4kp.taxonomies.parsinglevel.ParsingLevelSeries;
import org.omg.spec.api4kp.taxonomies.rel.dependencyreltype.DependencyTypeSeries;
import org.omg.spec.api4kp.taxonomies.rel.derivationreltype.DerivationTypeSeries;
import org.omg.spec.api4kp.taxonomies.rel.relatedversiontype.RelatedVersionTypeSeries;
import org.omg.spec.api4kp.taxonomies.rel.structuralreltype.StructuralPartTypeSeries;
import org.omg.spec.api4kp.taxonomies.rel.summaryreltype.SummarizationTypeSeries;
import org.omg.spec.api4kp.taxonomies.rel.variantreltype.VariantTypeSeries;

public class VersionAgnosticVocabularyTest {

  @Test
  public void testGeneratedEnums() {
    assertNotNull(KnowledgeRepresentationLanguageSeries.DMN_1_1);
    assertNotNull(KnowledgeRepresentationLanguageProfileSeries.OWL2_Full);
    assertNotNull(SerializationFormatSeries.JSON);
    assertNotNull(KnowledgeRepresentationLanguageSerializationSeries.DMN_1_1_XML_Syntax);    
    assertNotNull(KnowledgeRepresentationLanguageRoleSeries.Expression_Language);
    assertNotNull(LexiconSeries.LOINC);
    assertNotNull(ParsingLevelSeries.Encoded_Knowledge_Expression);

    assertNotNull(LanguageSeries.Italian);
    assertNotNull(org.omg.spec.api4kp.taxonomies.iso639_2_languagecodes.LanguageSeries.Italian);
    
    assertNotNull(DerivationTypeSeries.Is_Derived_From);
    assertNotNull(VariantTypeSeries.Is_Translation_Of);
    assertNotNull(SummarizationTypeSeries.Is_Digest_Of);
    assertNotNull(DependencyTypeSeries.Depends_On);
    assertNotNull(RelatedVersionTypeSeries.Has_Next_Version);
    assertNotNull(StructuralPartTypeSeries.Has_Structural_Component);
    
    assertNotNull(BibliographicCitationTypeSeries.Cites_As_Evidence);
    assertNotNull(PublishingRoleSeries.Author);
    assertNotNull(PublicationStatusSeries.Draft);
    assertNotNull(PublicationEventTypeSeries.Authoring);
    assertNotNull(AnnotationRelTypeSeries.Defines);

    assertNotNull(RelatedConceptSeries.Has_Broader);

    assertNotNull(KnowledgeAssetTypeSeries.Decision_Model);
    assertNotNull(KnowledgeAssetCategorySeries.Rules_Policies_And_Guidelines);
    assertNotNull(KnowledgeAssetRoleSeries.Operational_Concept_Definition);
    assertNotNull(KnowledgeArtifactCategorySeries.Software);
    
    assertNotNull(KnowledgeProcessingTechniqueSeries.Natural_Language_Processing_Technique);
    assertNotNull(KnowledgeProcessingOperationSeries.Syntactic_Translation_Task);

    assertNotNull(DecisionTypeSeries.Aggregation_Decision);
    assertNotNull(ConceptDefinitionTypeSeries.Interactive_Concept_Definition);
    
    assertNotNull(MIMETypeSeries.Application_Xml);
    assertNotNull(ResponseCodeSeries.SwitchingProtocols);
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
        LexiconSeries.SNOMED_CT.getReferentId().toString());

    assertEquals("fr",
        Language.French.getTag());

  }



  @Test
  public void testReferents() {
    assertEquals("https://www.omg.org/spec/DMN/1.2/",
        KnowledgeRepresentationLanguageSeries.DMN_1_2.getReferentId().toString());
  }

  @Test
  public void testKnownIdentifiers() {

    assertEquals("https://www.omg.org/spec/API4KP/taxonomies/kao/KnowledgeAssetType#6047674c-0d9b-3c81-89a3-6943f3a7169b",
        KnowledgeAssetTypeSeries.Nursing_Protocol.getConceptId().toString());

    assertEquals("https://www.omg.org/spec/API4KP/taxonomies/kao/KnowledgeAssetType#56b58fc2-b66f-3175-878e-bc3ef01cb916",
        KnowledgeAssetTypeSeries.Semantic_Decision_Model.getConceptId().toString());

    assertEquals("https://www.omg.org/spec/API4KP/taxonomies/kao/KnowledgeAssetCategory#d4b0e868-60c8-387d-a139-e3c35427bfb6",
        KnowledgeAssetCategorySeries.Assessment_Predictive_And_Inferential_Models.getConceptId().toString());

    assertEquals("https://www.omg.org/spec/API4KP/taxonomies/KRLanguage#0bf050a2-fbd6-38c2-a4ce-323fd91c7b24",
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
        Registry.getValidationSchema(KnowledgeRepresentationLanguageSeries.DMN_1_1.getReferentId())
            .flatMap(KnowledgeRepresentationLanguageSerializationSeries::resolveRef)
            .orElse(null));

    assertNotNull(KnowledgeRepresentationLanguageProfileSeries.CQL_Essentials);
    assertNotNull(KnowledgeRepresentationLanguageProfileSeries.GraphQL_Queries);
    assertNotNull(KnowledgeRepresentationLanguageProfileSeries.GraphQL_Schemas);
  }

  @Test
  public void testHistory() {
    Date effectiveDate = KnowledgeAssetCategorySeries.Rules_Policies_And_Guidelines
        .asOf(DateTimeUtil.parseDate("2019-09-15"))
        .map(Versionable::getVersionEstablishedOn)
        .orElse(null);
    assertNotNull(effectiveDate);
    assertEquals("2019-08-01",DateTimeUtil.serializeAsDate(effectiveDate));
  }


  @Test
  public void testKnownVersions() {
    assertEquals(1, DecisionTypeSeries.schemeVersions.size());
  }

}