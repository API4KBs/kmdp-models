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

import edu.mayo.ontology.taxonomies.skos.relatedconcept.RelatedConcept;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp.taxonomy.knowledgeassetcategory.KnowledgeAssetCategory;
import org.omg.spec.api4kp.taxonomy.knowledgeassetcategory.KnowledgeAssetCategorySeries;
import org.omg.spec.api4kp.taxonomy.knowledgeassettype.KnowledgeAssetType;
import org.omg.spec.api4kp.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries;
import org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguage;
import org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XSDCompilationTest {

  @Test
  public void testEnums() {
    RelatedConcept rel = RelatedConcept.Has_Broader;
    assertEquals("has broader", rel.getLabel());
  }

  @Test
  public void testAssetOntology() {
    KnowledgeAssetCategory kac = KnowledgeAssetCategorySeries.Plans_Processes_Pathways_And_Protocol_Definitions;
    assertEquals(6, KnowledgeAssetCategorySeries.values().length);

    KnowledgeAssetType kat = KnowledgeAssetTypeSeries.Clinical_Rule;
    assertEquals("http://ontology.mayo.edu/ontologies/clinicalknowledgeassets/ClinicalRule", kat.getReferentId().toString());

    KnowledgeRepresentationLanguage dmn = KnowledgeRepresentationLanguageSeries.DMN_1_1;
    KnowledgeRepresentationLanguage dmn2 = KnowledgeRepresentationLanguageSeries.DMN_1_2;
    assertEquals("https://www.omg.org/spec/DMN/1.1/", dmn.getReferentId().toString());
    assertEquals("https://www.omg.org/spec/DMN/1.2/", dmn2.getReferentId().toString());
  }
}
