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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.registry.Registry;
import edu.mayo.ontology.taxonomies.ccgentries.ConceptDefinitionTypeSeries;
import edu.mayo.ontology.taxonomies.kao.decisiontype.DecisionTypeSeries;
import edu.mayo.ontology.taxonomies.kmdo.annotationreltype.AnnotationRelTypeSeries;
import edu.mayo.ontology.taxonomies.kmdo.citationreltype.BibliographicCitationTypeSeries;
import edu.mayo.ontology.taxonomies.kmdo.publicationeventtype.PublicationEventTypeSeries;
import org.omg.spec.api4kp.taxonomy.publicationstatus.PublicationStatusSeries;
import edu.mayo.ontology.taxonomies.kmdo.publishingrole.PublishingRoleSeries;
import edu.mayo.ontology.taxonomies.mimetype.MIMETypeSeries;
import edu.mayo.ontology.taxonomies.skos.relatedconcept.RelatedConceptSeries;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.id.VersionIdentifier;
import org.omg.spec.api4kp.taxonomy.iso639_2_languagecode.Language;
import org.omg.spec.api4kp.taxonomy.iso639_2_languagecode.LanguageSeries;
import org.omg.spec.api4kp.taxonomy.knowledgeassetcategory.KnowledgeAssetCategorySeries;
import org.omg.spec.api4kp.taxonomy.knowledgeassetcategory._20190801.KnowledgeAssetCategory;
import org.omg.spec.api4kp.taxonomy.knowledgeassetrole.KnowledgeAssetRoleSeries;
import org.omg.spec.api4kp.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries;
import org.omg.spec.api4kp.taxonomy.knowledgeoperation.KnowledgeProcessingOperationSeries;
import org.omg.spec.api4kp.taxonomy.knowledgeprocessingtechnique.KnowledgeProcessingTechniqueSeries;
import org.omg.spec.api4kp.taxonomy.krformat.SerializationFormatSeries;
import org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries;
import org.omg.spec.api4kp.taxonomy.krprofile.KnowledgeRepresentationLanguageProfileSeries;
import org.omg.spec.api4kp.taxonomy.krserialization.KnowledgeRepresentationLanguageSerializationSeries;
import org.omg.spec.api4kp.taxonomy.languagerole.KnowledgeRepresentationLanguageRoleSeries;
import org.omg.spec.api4kp.taxonomy.lexicon.LexiconSeries;
import org.omg.spec.api4kp.taxonomy.parsinglevel.ParsingLevelSeries;
import org.omg.spec.api4kp.taxonomy.dependencyreltype.DependencyTypeSeries;
import org.omg.spec.api4kp.taxonomy.derivationreltype.DerivationTypeSeries;
import org.omg.spec.api4kp.taxonomy.relatedversiontype.RelatedVersionTypeSeries;
import org.omg.spec.api4kp.taxonomy.structuralreltype.StructuralPartTypeSeries;
import org.omg.spec.api4kp.taxonomy.summaryreltype.SummarizationTypeSeries;
import org.omg.spec.api4kp.taxonomy.variantreltype.VariantTypeSeries;

public class VocabularyTest {

  @Test
  public void testGeneratedEnums() {
    assertNotNull(KnowledgeRepresentationLanguageSeries.DMN_1_1);
    assertNotNull(KnowledgeRepresentationLanguageProfileSeries.OWL2_Full);
    assertNotNull(SerializationFormatSeries.JSON);
    assertNotNull(KnowledgeRepresentationLanguageSerializationSeries.DMN_1_1_XML_Syntax);
    assertNotNull(LexiconSeries.LOINC);
    assertNotNull(LanguageSeries.Italian);
    assertNotNull(DerivationTypeSeries.Is_Derived_From);
    assertNotNull(VariantTypeSeries.Is_Rearrangement_Of);
    assertNotNull(SummarizationTypeSeries.Is_Digest_Of);
    assertNotNull(DependencyTypeSeries.Depends_On);
    assertNotNull(RelatedVersionTypeSeries.Has_Next_Version);
    assertNotNull(StructuralPartTypeSeries.Has_Structural_Component);
    assertNotNull(BibliographicCitationTypeSeries.Cites_As_Authority);
    assertNotNull(KnowledgeAssetCategorySeries.Rules_Policies_And_Guidelines);
    assertNotNull(KnowledgeProcessingTechniqueSeries.Natural_Language_Processing_Technique);
    assertNotNull(KnowledgeAssetTypeSeries.Decision_Model);
    assertNotNull(RelatedConceptSeries.Has_Broader);
    assertNotNull(KnowledgeRepresentationLanguageRoleSeries.Expression_Language);
    assertNotNull(KnowledgeProcessingOperationSeries.Syntactic_Translation_Task);
    assertNotNull(ParsingLevelSeries.Encoded_Knowledge_Expression);
    assertNotNull(KnowledgeAssetRoleSeries.Operational_Concept_Definition);
    assertNotNull(PublicationEventTypeSeries.Authoring);
    assertNotNull(PublishingRoleSeries.Contributor);

    assertNotNull(PublicationStatusSeries.Draft);
    assertNotNull(MIMETypeSeries.Application_Pdf);
    assertNotNull(AnnotationRelTypeSeries.Defines);
    assertNotNull(DecisionTypeSeries.Aggregation_Decision);
    assertNotNull(ConceptDefinitionTypeSeries.Interactive_Concept_Definition);
  }

  @Test
  public void testLanguages() {
    assertNotNull(LanguageSeries.Italian);
    assertNotNull(LanguageSeries.Central_Khmer);
    assertNotNull(LanguageSeries.Arabic);
    assertNotNull(LanguageSeries.Chinese);
    assertNotNull(LanguageSeries.Spanish);
    assertNotNull(LanguageSeries.Vietnamese);
    assertNotNull(org.omg.spec.api4kp.taxonomy.iso639_2_languagecode.LanguageSeries.Hmong);
    assertNotNull(LanguageSeries.Somali);

    assertEquals(366,LanguageSeries.values().length);
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

    assertEquals("it",
        LanguageSeries.Italian.getTag());

    assertEquals("fr",
        LanguageSeries.French.getTag());

    assertEquals("fr",
        org.omg.spec.api4kp.taxonomy.iso639_1_languagecode._20190201.Language.French.getTag());

  }

  @Test
  public void testTagConsistency() {
    int tagLen = LanguageSeries.Italian.getTag().length();
    // preferring Alpha2 Codes
    assertEquals(2,tagLen);

    Set<org.omg.spec.api4kp.taxonomy.iso639_2_languagecode.LanguageSeries> alpha3Languages = Arrays.stream(
        org.omg.spec.api4kp.taxonomy.iso639_2_languagecode.LanguageSeries.values())
        .filter((lan) -> lan.getTag().length() != tagLen)
        .collect(Collectors.toSet());
    // some languages only have alpha3 codes
    assertEquals(194,alpha3Languages.size());
    assertTrue(alpha3Languages.contains(
        org.omg.spec.api4kp.taxonomy.iso639_2_languagecode.LanguageSeries.Fanti));

    assertEquals(new HashSet<>(Arrays.asList("fr","fra","fre")),
        new HashSet<>(LanguageSeries.French.getTags()));
  }

  @Test
  public void testReferents() {
    assertEquals("https://www.omg.org/spec/DMN/1.2/",
        KnowledgeRepresentationLanguageSeries.DMN_1_2.getReferentId().toString());

    assertEquals("https://www.omg.org/spec/LCC/Languages/ISO639-1-LanguageCodes/Italian",
        LanguageSeries.Italian.getReferentId().toString());

  }

  @Test
  public void testKnownIdentifiers() {

    assertEquals("https://www.omg.org/spec/API4KP/taxonomy/KnowledgeAssetType#6047674c-0d9b-3c81-89a3-6943f3a7169b",
        KnowledgeAssetTypeSeries.Nursing_Protocol.getConceptId().toString());

    assertEquals("https://www.omg.org/spec/API4KP/taxonomy/KnowledgeAssetType#56b58fc2-b66f-3175-878e-bc3ef01cb916",
        KnowledgeAssetTypeSeries.Semantic_Decision_Model.getConceptId().toString());

    assertEquals("https://www.omg.org/spec/API4KP/taxonomy/KnowledgeAssetCategory#d4b0e868-60c8-387d-a139-e3c35427bfb6",
        KnowledgeAssetCategorySeries.Assessment_Predictive_And_Inferential_Models.getConceptId().toString());

    assertEquals("https://www.omg.org/spec/API4KP/taxonomy/KRLanguage#0bf050a2-fbd6-38c2-a4ce-323fd91c7b24",
        KnowledgeRepresentationLanguageSeries.DMN_1_2.getConceptId().toString());
  }


  @Test
  public void testResolveTags() {
    Optional<Language> l0 = LanguageSeries.resolve("fr");
    assertTrue(l0.isPresent());
    assertSame(LanguageSeries.French, l0.get());
    
    Optional<Language> l1 = LanguageSeries.resolveTag("fr");
    assertTrue(l1.isPresent());
    assertSame(LanguageSeries.French, l1.get());

    Optional<Language> l2 = LanguageSeries.resolveTag("fra");
    assertTrue(l2.isPresent());
    assertSame(LanguageSeries.French, l2.get());

    Optional<Language> l3 = LanguageSeries.resolveTag("fre");
    assertTrue(l3.isPresent());
    assertSame(LanguageSeries.French, l3.get());

    
  }

  @Test
  public void testGeneratedEnumsVersion() {
    Optional<UUID> uid = ensureUUID(KnowledgeAssetCategorySeries.SCHEME_ID);
    assertTrue(uid.isPresent());

    URI schemeURI = KnowledgeAssetCategory.schemeURI.getVersionId();
    ResourceIdentifier nspace = KnowledgeAssetCategory.namespace;

    assertNotNull(schemeURI);
    VersionIdentifier vid = SemanticIdentifier.newVersionId(schemeURI);

    String version = vid.getVersionTag();
    assertEquals(version, nspace.getVersionTag());
  }

  @Test
  public void testKRLanguages() {
    assertNotNull(KnowledgeRepresentationLanguageSeries.SPARQL_1_1);

    assertEquals(KnowledgeRepresentationLanguageSerializationSeries.DMN_1_2_XML_Syntax,
        Registry.getValidationSchema(KnowledgeRepresentationLanguageSeries.DMN_1_2.getReferentId())
            .flatMap(KnowledgeRepresentationLanguageSerializationSeries::resolveRef)
            .orElse(null));

    assertNotNull(KnowledgeRepresentationLanguageProfileSeries.CQL_Essentials);
  }

  @Test
  void testGracefulFailonUnknown() {
    assertFalse(KnowledgeAssetRoleSeries.resolveUUID(UUID.randomUUID()).isPresent());
    assertFalse(KnowledgeAssetRoleSeries.resolveUUID(UUID.randomUUID()).isPresent());
    assertFalse(KnowledgeAssetRoleSeries.resolve(UUID.randomUUID().toString()).isPresent());
    assertFalse(KnowledgeAssetRoleSeries.resolveTag(UUID.randomUUID().toString()).isPresent());
    assertFalse(KnowledgeAssetRoleSeries.resolveId("urn:uuid:" + UUID.randomUUID().toString()).isPresent());
  }

}