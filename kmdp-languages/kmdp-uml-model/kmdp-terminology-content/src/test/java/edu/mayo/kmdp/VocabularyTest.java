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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.mayo.kmdp.terms.api4kp.knowledgeoperations._2018._06.KnowledgeOperations;
import edu.mayo.kmdp.terms.api4kp.parsinglevel._20190801.ParsingLevel;
import edu.mayo.kmdp.terms.iso639_1_languagecodes._20170801.ISO639_1_LanguageCodes;
import edu.mayo.kmdp.terms.kao.knowledgeassetcategory._1_0.KnowledgeAssetCategory;
import edu.mayo.kmdp.terms.kao.knowledgeassettype._1_0.KnowledgeAssetType;
import edu.mayo.kmdp.terms.kao.knowledgeprocessingtechnique._1_0.KnowledgeProcessingTechnique;
import edu.mayo.kmdp.terms.kao.languagerole._1_0.LanguageRole;
import edu.mayo.kmdp.terms.kao.rel.citationreltype._2018_02_16.CitationRelType;
import edu.mayo.kmdp.terms.kao.rel.dependencyreltype._20190801.DependencyRelType;
import edu.mayo.kmdp.terms.kao.rel.derivationreltype._20190801.DerivationRelType;
import edu.mayo.kmdp.terms.kao.rel.relatedversiontype._20190801.RelatedVersionType;
import edu.mayo.kmdp.terms.kao.rel.structuralreltype._20190801.StructuralRelType;
import edu.mayo.kmdp.terms.kao.rel.summaryreltype._20190801.SummaryRelType;
import edu.mayo.kmdp.terms.kao.rel.variantreltype._20190801.VariantRelType;
import edu.mayo.kmdp.terms.krformat._2018._08.KRFormat;
import edu.mayo.kmdp.terms.krlanguage._2018._08.KRLanguage;
import edu.mayo.kmdp.terms.krprofile._2018._08.KRProfile;
import edu.mayo.kmdp.terms.krserialization._2018._08.KRSerialization;
import edu.mayo.kmdp.terms.lexicon._2018._08.Lexicon;
import edu.mayo.kmdp.terms.skos.relatedconcept.RelatedConcept;
import org.junit.jupiter.api.Test;

public class VocabularyTest {

  @Test
  public void testGeneratedEnums() {
    assertNotNull(KRLanguage.DMN_1_1);
    assertNotNull(KRProfile.OWL_2_Full);
    assertNotNull(KRFormat.JSON);
    assertNotNull(KRSerialization.DMN_XML);
    assertNotNull(Lexicon.LOINC_US);
    assertNotNull(ISO639_1_LanguageCodes.Italian);
    assertNotNull(DerivationRelType.Derived_From);
    assertNotNull(VariantRelType.Rearrangement_Of);
    assertNotNull(SummaryRelType.Digest_Of);
    assertNotNull(DependencyRelType.Depends_On);
    assertNotNull(RelatedVersionType.Has_Original);
    assertNotNull(StructuralRelType.Has_Part);
    assertNotNull(CitationRelType.Cites_As_Authority);
    assertNotNull(KnowledgeAssetCategory.Rules_Policies_And_Guidelines);
    assertNotNull(KnowledgeProcessingTechnique.Natural_Language_Processing_Technique);
    assertNotNull(KnowledgeAssetType.Decision_Model);
    assertNotNull(RelatedConcept.Has_Broader);
    assertNotNull(LanguageRole.Expression_Language);
    assertNotNull(KnowledgeOperations.Translation_Task);
    assertNotNull(ParsingLevel.Encoded_Knowledge_Expression);
  }

}
