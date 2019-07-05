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

import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations._2018._06.KnowledgeProcessingOperation;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel._20190801.ParsingLevel;
import edu.mayo.ontology.taxonomies.iso639_2_languagecodes._20190201.Language;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetcategory._1_0.KnowledgeAssetCategory;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetrole._1_0.KnowledgeAssetRole;
import edu.mayo.ontology.taxonomies.kao.knowledgeassettype._1_0.KnowledgeAssetType;
import edu.mayo.ontology.taxonomies.kao.knowledgeprocessingtechnique._1_0.KnowledgeProcessingTechnique;
import edu.mayo.ontology.taxonomies.kao.languagerole._1_0.KnowledgeRepresentationLanguageRole;
import edu.mayo.ontology.taxonomies.kao.publicationeventtype._20180601.PublicationEventType;
import edu.mayo.ontology.taxonomies.kao.publicationstatus._2014_02_01.PublicationStatus;
import edu.mayo.ontology.taxonomies.kao.publishingrole.PublishingRole;
import edu.mayo.ontology.taxonomies.kao.rel.citationreltype._2018_02_16.BibliographicCitationType;
import edu.mayo.ontology.taxonomies.kao.rel.dependencyreltype._20190801.DependencyType;
import edu.mayo.ontology.taxonomies.kao.rel.derivationreltype._20190801.DerivationType;
import edu.mayo.ontology.taxonomies.kao.rel.relatedversiontype._20190801.RelatedVersionType;
import edu.mayo.ontology.taxonomies.kao.rel.structuralreltype._20190801.StructuralPartType;
import edu.mayo.ontology.taxonomies.kao.rel.summaryreltype._20190801.SummarizationType;
import edu.mayo.ontology.taxonomies.kao.rel.variantreltype._20190801.VariantType;
import edu.mayo.ontology.taxonomies.krformat._2018._08.SerializationFormat;
import edu.mayo.ontology.taxonomies.krlanguage._2018._08.KnowledgeRepresentationLanguage;
import edu.mayo.ontology.taxonomies.krprofile._2018._08.KnowledgeRepresentationLanguageProfile;
import edu.mayo.ontology.taxonomies.krserialization._2018._08.KnowledgeRepresentationLanguageSerialization;
import edu.mayo.ontology.taxonomies.lexicon._2018._08.Lexicon;
import edu.mayo.ontology.taxonomies.mimetype.MIMEType;
import edu.mayo.ontology.taxonomies.skos.relatedconcept.RelatedConcept;
import java.util.Optional;
import java.util.UUID;
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
  public void testGeneratedEnumsVersion() {
    Optional<UUID> uid = ensureUUID(KnowledgeAssetCategory.schemeID);
    assertTrue(uid.isPresent());

    assertNotNull(KnowledgeAssetCategory.schemeURI.getVersionId());
    assertEquals(uid.get().toString(),
        KnowledgeAssetCategory.schemeURI.getVersionId().getFragment());

  }

}