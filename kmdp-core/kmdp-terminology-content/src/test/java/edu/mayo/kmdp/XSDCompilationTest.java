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

import edu.mayo.ontology.taxonomies.kao.knowledgeassetcategory._20190801.KnowledgeAssetCategory;
import edu.mayo.ontology.taxonomies.kao.knowledgeassettype._20190801.KnowledgeAssetType;
import edu.mayo.ontology.taxonomies.krlanguage._20190801.KnowledgeRepresentationLanguage;
import edu.mayo.ontology.taxonomies.skos.relatedconcept.RelatedConcept;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XSDCompilationTest {

  @Test
  public void testEnums() {
    RelatedConcept rel = RelatedConcept.Has_Broader;
    assertEquals("has broader", rel.getLabel());
  }

  @Test
  public void testAssetOntology() {
    KnowledgeAssetCategory kac = KnowledgeAssetCategory.Plans_Processes_Pathways_And_Protocol_Definitions;
    assertEquals(6, KnowledgeAssetCategory.values().length);

    KnowledgeAssetType kat = KnowledgeAssetType.Clinical_Rule;
    assertEquals("http://ontology.mayo.edu/ontologies/clinicalknowledgeassets/ClinicalRule", kat.getRef().toString());

    KnowledgeRepresentationLanguage dmn = KnowledgeRepresentationLanguage.DMN_1_1;
    KnowledgeRepresentationLanguage dmn2 = KnowledgeRepresentationLanguage.DMN_1_2;
    assertEquals("https://www.omg.org/spec/DMN/1.1/", dmn.getRef().toString());
    assertEquals("https://www.omg.org/spec/DMN/1.2/", dmn2.getRef().toString());
  }
}
