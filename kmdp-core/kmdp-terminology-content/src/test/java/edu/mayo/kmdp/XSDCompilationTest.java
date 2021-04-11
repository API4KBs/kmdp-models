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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.omg.spec.api4kp._20200801.taxonomy.clinicalknowledgeassettype.ClinicalKnowledgeAssetTypeSeries.Clinical_Rule;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetcategory.KnowledgeAssetCategorySeries.Plans_Processes_Pathways_And_Protocol_Definitions;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_1;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_2;

import edu.mayo.ontology.taxonomies.kmdo.relatedconcept.IRelatedConcept;
import edu.mayo.ontology.taxonomies.kmdo.relatedconcept.RelatedConceptSeries;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetcategory.KnowledgeAssetCategory;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetcategory.KnowledgeAssetCategorySeries;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetType;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries;
import org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguage;
import org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries;

public class XSDCompilationTest {

  @Test
  public void testEnums() {
    IRelatedConcept rel = RelatedConceptSeries.Has_Broader;
    assertEquals("has broader", rel.getLabel());
  }

  @Test
  public void testAssetOntology() {
    KnowledgeAssetCategory kac = Plans_Processes_Pathways_And_Protocol_Definitions;
    assertNotNull(kac);
    assertEquals(6, KnowledgeAssetCategorySeries.values().length);

    KnowledgeAssetType kat = Clinical_Rule;
    assertEquals("https://www.omg.org/spec/API4KP/api4kp-ckao/ClinicalRule", kat.getReferentId().toString());

    KnowledgeRepresentationLanguage dmn = DMN_1_1;
    KnowledgeRepresentationLanguage dmn2 = DMN_1_2;
    assertEquals("https://www.omg.org/spec/DMN/1.1/", dmn.getReferentId().toString());
    assertEquals("https://www.omg.org/spec/DMN/1.2/", dmn2.getReferentId().toString());
  }
}
