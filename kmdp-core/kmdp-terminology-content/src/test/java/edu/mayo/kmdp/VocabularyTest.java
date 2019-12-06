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
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations._20190801.KnowledgeProcessingOperation;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel._20190801.ParsingLevel;
import edu.mayo.ontology.taxonomies.iso639_2_languagecodes._20190201.Language;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetcategory._20190801.KnowledgeAssetCategory;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetrole.KnowledgeAssetRoleSeries;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetrole._20190801.KnowledgeAssetRole;
import edu.mayo.ontology.taxonomies.kao.knowledgeassettype._20190801.KnowledgeAssetType;
import edu.mayo.ontology.taxonomies.kao.knowledgeprocessingtechnique._20190801.KnowledgeProcessingTechnique;
import edu.mayo.ontology.taxonomies.kao.languagerole._20190801.KnowledgeRepresentationLanguageRole;
import edu.mayo.ontology.taxonomies.kao.publicationeventtype._20190801.PublicationEventType;
import edu.mayo.ontology.taxonomies.kao.publicationstatus._2014_02_01.PublicationStatus;
import edu.mayo.ontology.taxonomies.kao.publishingrole._2017_09_04.PublishingRole;
import edu.mayo.ontology.taxonomies.kao.rel.citationreltype._2018_02_16.BibliographicCitationType;
import edu.mayo.ontology.taxonomies.kao.rel.dependencyreltype._20190801.DependencyType;
import edu.mayo.ontology.taxonomies.kao.rel.derivationreltype._20190801.DerivationType;
import edu.mayo.ontology.taxonomies.kao.rel.relatedversiontype._20190801.RelatedVersionType;
import edu.mayo.ontology.taxonomies.kao.rel.structuralreltype._20190801.StructuralPartType;
import edu.mayo.ontology.taxonomies.kao.rel.summaryreltype._20190801.SummarizationType;
import edu.mayo.ontology.taxonomies.kao.rel.variantreltype._20190801.VariantType;
import edu.mayo.ontology.taxonomies.kmdo.annotationreltype._20190801.AnnotationRelType;
import edu.mayo.ontology.taxonomies.krformat._20190801.SerializationFormat;
import edu.mayo.ontology.taxonomies.krlanguage._20190801.KnowledgeRepresentationLanguage;
import edu.mayo.ontology.taxonomies.krprofile._20190801.KnowledgeRepresentationLanguageProfile;
import edu.mayo.ontology.taxonomies.krserialization._20190801.KnowledgeRepresentationLanguageSerialization;
import edu.mayo.ontology.taxonomies.lexicon._20190801.Lexicon;
import edu.mayo.ontology.taxonomies.mimetype.MIMEType;
import edu.mayo.ontology.taxonomies.skos.relatedconcept.RelatedConcept;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class VocabularyTest {

  @Test
  public void testGeneratedEnums() {
    assertNotNull(KnowledgeRepresentationLanguage.DMN_1_1);
    assertNotNull(KnowledgeRepresentationLanguageProfile.OWL2_Full);
    assertNotNull(SerializationFormat.JSON);
    assertNotNull(KnowledgeRepresentationLanguageSerialization.DMN_1_1_XML_Syntax);
    assertNotNull(Lexicon.LOINC);
    assertNotNull(Language.Italian);
    assertNotNull(DerivationType.Derived_From);
    assertNotNull(VariantType.Rearrangement_Of);
    assertNotNull(SummarizationType.Digest_Of);
    assertNotNull(DependencyType.Depends_On);
    assertNotNull(RelatedVersionType.Has_Original);
    assertNotNull(StructuralPartType.Has_Part);
    assertNotNull(BibliographicCitationType.Cites_As_Authority);
    assertNotNull(KnowledgeAssetCategory.Rules_Policies_And_Guidelines);
    assertNotNull(KnowledgeProcessingTechnique.Natural_Language_Processing_Technique);
    assertNotNull(KnowledgeAssetType.Decision_Model);
    assertNotNull(RelatedConcept.Has_Broader);
    assertNotNull(KnowledgeRepresentationLanguageRole.Expression_Language);
    assertNotNull(KnowledgeProcessingOperation.Translation_Task);
    assertNotNull(ParsingLevel.Encoded_Knowledge_Expression);
    assertNotNull(KnowledgeAssetRole.Operational_Concept_Definition);
    assertNotNull(PublicationEventType.Authoring);
    assertNotNull(PublishingRole.Contributor);
    assertNotNull(PublicationStatus.Draft);
    assertNotNull(MIMEType.Application_Pdf);
    assertNotNull(AnnotationRelType.Defines);
  }

  @Test
  public void testLanguages() {
    assertNotNull(Language.Italian);
    assertNotNull(Language.Central_Khmer);
    assertNotNull(Language.Arabic);
    assertNotNull(Language.Chinese);
    assertNotNull(Language.Spanish);
    assertNotNull(Language.Vietnamese);
    assertNotNull(Language.Hmong);
    assertNotNull(Language.Somali);

    assertEquals(366,Language.values().length);
  }


  @Test
  public void testKnownTags() {

    assertEquals("ofn",
        KnowledgeRepresentationLanguageSerialization.OWL_Functional_Syntax.getTag());

    assertEquals("dmn-v12",
        KnowledgeRepresentationLanguage.DMN_1_2.getTag());

    assertEquals("lnc",
        Lexicon.LOINC.getTag());

    assertEquals("http://snomed.info/sct/900000000000207008/version/20180731",
        Lexicon.SNOMED_CT.getRef().toString());

    assertEquals("it",
        Language.Italian.getTag());

    assertEquals("fr",
        Language.French.getTag());

    assertEquals("fr",
        edu.mayo.ontology.taxonomies.iso639_1_languagecodes._20190201.Language.French.getTag());

  }

  @Test
  public void testTagConsistency() {
    int tagLen = Language.Italian.getTag().length();
    // preferring Alpha2 Codes
    assertEquals(2,tagLen);

    Set<Language> alpha3Languages = Arrays.stream(Language.values())
        .filter((lan) -> lan.getTag().length() != tagLen)
        .collect(Collectors.toSet());
    // some languages only have alpha3 codes
    assertEquals(194,alpha3Languages.size());
    assertTrue(alpha3Languages.contains(Language.Fanti));

    assertEquals(new HashSet<>(Arrays.asList("fr","fra","fre")),
        new HashSet<>(Language.French.getTags()));
  }

  @Test
  public void testReferents() {
    assertEquals("https://www.omg.org/spec/DMN/1.2/",
        KnowledgeRepresentationLanguage.DMN_1_2.getRef().toString());

    assertEquals("https://www.omg.org/spec/LCC/Languages/ISO639-1-LanguageCodes/Italian",
        Language.Italian.getRef().toString());

  }

  @Test
  public void testKnownIdentifiers() {

    assertEquals("https://ontology.mayo.edu/taxonomies/KAO/KnowledgeAssetType#6047674c-0d9b-3c81-89a3-6943f3a7169b",
        KnowledgeAssetType.Nursing_Protocol.getConceptId().toString());

    assertEquals("https://ontology.mayo.edu/taxonomies/KAO/KnowledgeAssetType#56b58fc2-b66f-3175-878e-bc3ef01cb916",
        KnowledgeAssetType.Semantic_Decision_Model.getConceptId().toString());

    assertEquals("https://ontology.mayo.edu/taxonomies/KAO/KnowledgeAssetCategory#d4b0e868-60c8-387d-a139-e3c35427bfb6",
        KnowledgeAssetCategory.Assessment_Predictive_And_Inferential_Models.getConceptId().toString());

    assertEquals("https://ontology.mayo.edu/taxonomies/KRLanguage#0bf050a2-fbd6-38c2-a4ce-323fd91c7b24",
        KnowledgeRepresentationLanguage.DMN_1_2.getConceptId().toString());
  }


  @Test
  public void testResolveTags() {
    Optional<Language> l0 = Language.resolve("fr");
    assertTrue(l0.isPresent());
    assertSame(Language.French, l0.get());
    
    Optional<Language> l1 = Language.resolveTag("fr");
    assertTrue(l1.isPresent());
    assertSame(Language.French, l1.get());

    Optional<Language> l2 = Language.resolveTag("fra");
    assertTrue(l2.isPresent());
    assertSame(Language.French, l2.get());

    Optional<Language> l3 = Language.resolveTag("fre");
    assertTrue(l3.isPresent());
    assertSame(Language.French, l3.get());

    
  }

  @Test
  public void testGeneratedEnumsVersion() {
    Optional<UUID> uid = ensureUUID(KnowledgeAssetCategory.SCHEME_ID);
    assertTrue(uid.isPresent());

    assertNotNull(KnowledgeAssetCategory.schemeURI.getVersionId());

    String seriesId = KnowledgeAssetCategory.schemeURI.getUri().toString();
    String versionId = KnowledgeAssetCategory.schemeURI.getVersionId().toString();
    String version = versionId.replace(seriesId,"").replace("/","");
    assertEquals(version, KnowledgeAssetCategory.schemeURI.getVersion());
    assertEquals(version, KnowledgeAssetCategory.namespace.getVersion());
  }

  @Test
  public void testKRLanguages() {
    assertNotNull(KnowledgeRepresentationLanguage.SPARQL_1_1);

    assertEquals(KnowledgeRepresentationLanguageSerialization.DMN_1_2_XML_Syntax,
        Registry.getValidationSchema(KnowledgeRepresentationLanguage.DMN_1_2.getRef())
            .flatMap(KnowledgeRepresentationLanguageSerialization::resolveRef)
            .orElse(null));

    assertNotNull(KnowledgeRepresentationLanguageProfile.CQL_Essentials);
  }

  @Test
  void testGracefulFailonUnknown() {
    assertFalse(KnowledgeAssetRoleSeries.resolveUUID(UUID.randomUUID()).isPresent());
    assertFalse(KnowledgeAssetRole.resolveUUID(UUID.randomUUID()).isPresent());
    assertFalse(KnowledgeAssetRole.resolve(UUID.randomUUID().toString()).isPresent());
    assertFalse(KnowledgeAssetRole.resolveTag(UUID.randomUUID().toString()).isPresent());
    assertFalse(KnowledgeAssetRole.resolveId("urn:uuid:" + UUID.randomUUID().toString()).isPresent());
  }

}