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
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.registry.Registry;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperation;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevel;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetcategory.KnowledgeAssetCategory;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetrole.KnowledgeAssetRole;
import edu.mayo.ontology.taxonomies.kao.knowledgeassettype.KnowledgeAssetType;
import edu.mayo.ontology.taxonomies.kao.knowledgeprocessingtechnique.KnowledgeProcessingTechnique;
import edu.mayo.ontology.taxonomies.kao.languagerole.KnowledgeRepresentationLanguageRole;
import edu.mayo.ontology.taxonomies.kao.publicationeventtype.PublicationEventType;
import edu.mayo.ontology.taxonomies.kao.rel.dependencyreltype.DependencyType;
import edu.mayo.ontology.taxonomies.kao.rel.derivationreltype.DerivationType;
import edu.mayo.ontology.taxonomies.kao.rel.relatedversiontype.RelatedVersionType;
import edu.mayo.ontology.taxonomies.kao.rel.structuralreltype.StructuralPartType;
import edu.mayo.ontology.taxonomies.kao.rel.summaryreltype.SummarizationType;
import edu.mayo.ontology.taxonomies.kao.rel.variantreltype.VariantType;
import edu.mayo.ontology.taxonomies.kmdo.annotationreltype.AnnotationRelType;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormat;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import edu.mayo.ontology.taxonomies.krprofile.KnowledgeRepresentationLanguageProfile;
import edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerialization;
import edu.mayo.ontology.taxonomies.lexicon.Lexicon;
import edu.mayo.ontology.taxonomies.skos.relatedconcept.RelatedConcept;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class VersionAgnosticVocabularyTest {

  @Test
  public void testGeneratedEnums() {
    assertNotNull(KnowledgeRepresentationLanguage.DMN_1_1);
    assertNotNull(KnowledgeRepresentationLanguageProfile.OWL2_Full);
    assertNotNull(SerializationFormat.JSON);
    assertNotNull(KnowledgeRepresentationLanguageSerialization.DMN_1_1_XML_Syntax);
    assertNotNull(Lexicon.LOINC);
    assertNotNull(DerivationType.Derived_From);
    assertNotNull(VariantType.Rearrangement_Of);
    assertNotNull(SummarizationType.Digest_Of);
    assertNotNull(DependencyType.Depends_On);
    assertNotNull(RelatedVersionType.Has_Original);
    assertNotNull(StructuralPartType.Has_Part);
    assertNotNull(KnowledgeAssetCategory.Rules_Policies_And_Guidelines);
    assertNotNull(KnowledgeProcessingTechnique.Natural_Language_Processing_Technique);
    assertNotNull(KnowledgeAssetType.Decision_Model);
    assertNotNull(RelatedConcept.Has_Broader);
    assertNotNull(KnowledgeRepresentationLanguageRole.Expression_Language);
    assertNotNull(KnowledgeProcessingOperation.Translation_Task);
    assertNotNull(ParsingLevel.Encoded_Knowledge_Expression);
    assertNotNull(KnowledgeAssetRole.Operational_Concept_Definition);
    assertNotNull(PublicationEventType.Authoring);
    assertNotNull(AnnotationRelType.Defines);
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

    assertEquals("fr",
        edu.mayo.ontology.taxonomies.iso639_1_languagecodes._20190201.Language.French.getTag());

  }



  @Test
  public void testReferents() {
    assertEquals("https://www.omg.org/spec/DMN/1.2/",
        KnowledgeRepresentationLanguage.DMN_1_2.getRef().toString());
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
  public void testGeneratedEnumsVersion() {
    Optional<UUID> uid = ensureUUID(KnowledgeAssetCategory.schemeID);
    assertTrue(uid.isPresent());

    assertNotNull(KnowledgeAssetCategory.schemeURI.getVersionId());
    assertEquals(uid.get().toString(),
        KnowledgeAssetCategory.schemeURI.getVersionId().getFragment());
  }

  @Test
  public void testKRLanguages() {
    assertNotNull(KnowledgeRepresentationLanguage.KNART_1_3);

    assertEquals(KnowledgeRepresentationLanguageSerialization.DMN_1_1_XML_Syntax,
        Registry.getValidationSchema(KnowledgeRepresentationLanguage.DMN_1_1.getRef())
            .flatMap(KnowledgeRepresentationLanguageSerialization::resolveRef)
            .orElse(null));

    assertNotNull(KnowledgeRepresentationLanguageProfile.CQL_Essentials);
    assertNotNull(KnowledgeRepresentationLanguageProfile.GraphQL_Queries);
    assertNotNull(KnowledgeRepresentationLanguageProfile.GraphQL_Schemas);
  }

}