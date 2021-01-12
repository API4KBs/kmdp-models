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
import static org.omg.spec.api4kp._20200801.taxonomy.clinicalknowledgeassettype.ClinicalKnowledgeAssetTypeSeries.Case_Enrichment_Rule;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_2;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.SPARQL_1_1;
import static org.omg.spec.api4kp._20200801.taxonomy.krprofile.KnowledgeRepresentationLanguageProfileSeries.CQL_Essentials;
import static org.omg.spec.api4kp._20200801.taxonomy.krserialization.KnowledgeRepresentationLanguageSerializationSeries.DMN_1_2_XML_Syntax;

import edu.mayo.kmdp.registry.Registry;
import edu.mayo.ontology.taxonomies.clinicalinterrogatives.ClinicalInterrogativeSeries;
import edu.mayo.ontology.taxonomies.kao.ccgentries.ConceptDefinitionTypeSeries;
import edu.mayo.ontology.taxonomies.kao.decisiontype.DecisionTypeSeries;
import edu.mayo.ontology.taxonomies.kmdo.citationreltype.BibliographicCitationTypeSeries;
import edu.mayo.ontology.taxonomies.kmdo.publicationeventtype.PublicationEventTypeSeries;
import edu.mayo.ontology.taxonomies.kmdo.publishingrole.PublishingRoleSeries;
import edu.mayo.ontology.taxonomies.kmdo.relatedconcept.RelatedConceptSeries;
import edu.mayo.ontology.taxonomies.kmdo.semanticannotationreltype.SemanticAnnotationRelTypeSeries;
import edu.mayo.ontology.taxonomies.ws.mimetype.MIMETypeSeries;
import edu.mayo.ontology.taxonomies.ws.responsecodes.ResponseCodeSeries;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.taxonomy.dependencyreltype.DependencyTypeSeries;
import org.omg.spec.api4kp._20200801.taxonomy.derivationreltype.DerivationTypeSeries;
import org.omg.spec.api4kp._20200801.taxonomy.iso639_2_languagecode.Language;
import org.omg.spec.api4kp._20200801.taxonomy.iso639_2_languagecode.LanguageSeries;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetcategory.KnowledgeAssetCategorySeries;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetcategory._20190801.KnowledgeAssetCategory;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetrole.KnowledgeAssetRoleSeries;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetType;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeoperation.KnowledgeProcessingOperationSeries;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeprocessingtechnique.KnowledgeProcessingTechniqueSeries;
import org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries;
import org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries;
import org.omg.spec.api4kp._20200801.taxonomy.krprofile.KnowledgeRepresentationLanguageProfileSeries;
import org.omg.spec.api4kp._20200801.taxonomy.krserialization.KnowledgeRepresentationLanguageSerializationSeries;
import org.omg.spec.api4kp._20200801.taxonomy.languagerole.KnowledgeRepresentationLanguageRoleSeries;
import org.omg.spec.api4kp._20200801.taxonomy.lexicon.LexiconSeries;
import org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries;
import org.omg.spec.api4kp._20200801.taxonomy.publicationstatus.PublicationStatusSeries;
import org.omg.spec.api4kp._20200801.taxonomy.relatedversiontype.RelatedVersionTypeSeries;
import org.omg.spec.api4kp._20200801.taxonomy.structuralreltype.StructuralPartTypeSeries;
import org.omg.spec.api4kp._20200801.taxonomy.summaryreltype.SummarizationTypeSeries;
import org.omg.spec.api4kp._20200801.taxonomy.variantreltype.VariantTypeSeries;

class VocabularyTest {

  @Test
  void testGeneratedEnums() {
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
    assertNotNull(ResponseCodeSeries.OK);
    assertNotNull(SemanticAnnotationRelTypeSeries.Defines);
    assertNotNull(DecisionTypeSeries.Aggregation_Decision);
    assertNotNull(ConceptDefinitionTypeSeries.Interactive_Concept_Definition);
    assertNotNull(ClinicalInterrogativeSeries.Is);
  }

  @Test
  void testLanguages() {
    assertNotNull(LanguageSeries.Italian);
    assertNotNull(LanguageSeries.Central_Khmer);
    assertNotNull(LanguageSeries.Arabic);
    assertNotNull(LanguageSeries.Chinese);
    assertNotNull(LanguageSeries.Spanish);
    assertNotNull(LanguageSeries.Vietnamese);
    assertNotNull(org.omg.spec.api4kp._20200801.taxonomy.iso639_2_languagecode.LanguageSeries.Hmong);
    assertNotNull(LanguageSeries.Somali);

    assertEquals(366,LanguageSeries.values().length);
  }


  @Test
  void testKnownTags() {

    assertEquals("ofn",
        KnowledgeRepresentationLanguageSerializationSeries.OWL_Functional_Syntax.getTag());
    assertEquals("brl",
        KnowledgeRepresentationLanguageSerializationSeries.ODM_BRL_Syntax.getTag());
    assertEquals("odm-bom-v8.10.x",
        KnowledgeRepresentationLanguageSeries.ODM_BOM_8_10_X.getTag());

    assertEquals("dmn-v12",
        DMN_1_2.getTag());

    assertEquals("lnc",
        LexiconSeries.LOINC.getTag());

    assertEquals("http://snomed.info/sct/900000000000207008/version/20180731",
        LexiconSeries.SNOMED_CT.getReferentId().toString());

    assertEquals("it",
        LanguageSeries.Italian.getTag());

    assertEquals("fr",
        LanguageSeries.French.getTag());

    assertEquals("fr",
        org.omg.spec.api4kp._20200801.taxonomy.iso639_1_languagecode._20190201.Language.French.getTag());

  }

  @Test
  void testTagConsistency() {
    int tagLen = LanguageSeries.Italian.getTag().length();
    // preferring Alpha2 Codes
    assertEquals(2,tagLen);

    Set<org.omg.spec.api4kp._20200801.taxonomy.iso639_2_languagecode.LanguageSeries> alpha3Languages = Arrays.stream(
        org.omg.spec.api4kp._20200801.taxonomy.iso639_2_languagecode.LanguageSeries.values())
        .filter((lan) -> lan.getTag().length() != tagLen)
        .collect(Collectors.toSet());
    // some languages only have alpha3 codes
    assertEquals(194,alpha3Languages.size());
    assertTrue(alpha3Languages.contains(
        org.omg.spec.api4kp._20200801.taxonomy.iso639_2_languagecode.LanguageSeries.Fanti));

    assertEquals(new HashSet<>(Arrays.asList("fr","fra","fre")),
        new HashSet<>(LanguageSeries.French.getTags()));
  }

  @Test
  void testReferents() {
    assertEquals("https://www.omg.org/spec/DMN/1.2/",
        DMN_1_2.getReferentId().toString());

    assertEquals("https://www.omg.org/spec/LCC/Languages/ISO639-1-LanguageCodes/Italian",
        LanguageSeries.Italian.getReferentId().toString());

  }

  @Test
  void testKnownIdentifiers() {

    assertEquals("https://www.omg.org/spec/API4KP/20200801/taxonomy/KnowledgeAssetType#6047674c-0d9b-3c81-89a3-6943f3a7169b",
        KnowledgeAssetTypeSeries.Nursing_Protocol.getConceptId().toString());

    assertEquals("https://www.omg.org/spec/API4KP/20200801/taxonomy/KnowledgeAssetType#56b58fc2-b66f-3175-878e-bc3ef01cb916",
        KnowledgeAssetTypeSeries.Semantic_Decision_Model.getConceptId().toString());

    assertEquals("https://www.omg.org/spec/API4KP/20200801/taxonomy/KnowledgeAssetCategory#d4b0e868-60c8-387d-a139-e3c35427bfb6",
        KnowledgeAssetCategorySeries.Assessment_Predictive_And_Inferential_Models.getConceptId().toString());

    assertEquals("https://www.omg.org/spec/API4KP/20200801/taxonomy/KRLanguage#0bf050a2-fbd6-38c2-a4ce-323fd91c7b24",
        DMN_1_2.getConceptId().toString());
  }


  @Test
  void testResolveTags() {
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
  void testGeneratedEnumsVersion() {
    String base = "https://www.omg.org/spec/API4KP/20200801/taxonomy/KnowledgeAssetCategory";

    Optional<UUID> uid = ensureUUID(KnowledgeAssetCategorySeries.SCHEME_ID);
    assertTrue(uid.isPresent());

    ResourceIdentifier versionedScheme = KnowledgeAssetCategory.schemeVersionIdentifier;
    URI schemeURI = versionedScheme.getResourceId();
    URI schemeVersionURI = versionedScheme.getVersionId();

    assertNotNull(schemeURI);
    assertEquals(base + "#02762f0f-208e-3b57-94ab-fa288ecdfe39", schemeURI.toString());
    assertEquals(base + "/versions/20190801#02762f0f-208e-3b57-94ab-fa288ecdfe39", schemeVersionURI.toString());
    //assertEquals(version, versionedScheme.getVersionTag());
    assertEquals(base,
        versionedScheme.getNamespaceUri().toString());

    ResourceIdentifier nspace = KnowledgeAssetCategory.values()[0].getNamespace();
    assertEquals(base,
        nspace.getResourceId().toString());
  }

  @Test
  void testKRLanguages() {
    assertNotNull(SPARQL_1_1);

    assertEquals(DMN_1_2_XML_Syntax,
        Registry.getValidationSchema(DMN_1_2.getReferentId())
            .flatMap(KnowledgeRepresentationLanguageSerializationSeries::resolveRef)
            .orElse(null));

    assertNotNull(CQL_Essentials);
  }

  @Test
  void testGracefulFailonUnknown() {
    assertFalse(KnowledgeAssetRoleSeries.resolveUUID(UUID.randomUUID()).isPresent());
    assertFalse(KnowledgeAssetRoleSeries.resolveUUID(UUID.randomUUID()).isPresent());
    assertFalse(KnowledgeAssetRoleSeries.resolve(UUID.randomUUID().toString()).isPresent());
    assertFalse(KnowledgeAssetRoleSeries.resolveTag(UUID.randomUUID().toString()).isPresent());
    assertFalse(KnowledgeAssetRoleSeries.resolveId("urn:uuid:" + UUID.randomUUID().toString()).isPresent());
  }


  @Test
  void testPolymorph() {
    KnowledgeAssetType kat = Case_Enrichment_Rule;
    KnowledgeAssetType katVer = Case_Enrichment_Rule.getLatest();

    assertTrue(kat.sameAs(katVer));
    assertTrue(kat.sameTermAs(katVer));
    assertTrue(kat.isCoreferent(katVer));
    assertTrue(kat.evokesSameAs(katVer));
  }
}