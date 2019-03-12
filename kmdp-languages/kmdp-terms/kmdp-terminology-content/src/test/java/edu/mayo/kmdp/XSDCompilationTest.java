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

import edu.mayo.kmdp.terms.kao.knowledgeassetcategory._1_0.KnowledgeAssetCategory;
import edu.mayo.kmdp.terms.kao.knowledgeassettype._1_0.KnowledgeAssetType;
import edu.mayo.kmdp.terms.krlanguage._2018._08.KRLanguage;
import edu.mayo.kmdp.terms.skos.relatedconcept.RelatedConcept;
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
    assertEquals("http://ckm.ontology.mayo.edu/ontology/KAO#ClinicalRule", kat.getRef().toString());

    KRLanguage dmn = KRLanguage.DMN_1_1;
    assertEquals("https://www.omg.org/spec/DMN/1.1", dmn.getRef().toString());
  }
}
