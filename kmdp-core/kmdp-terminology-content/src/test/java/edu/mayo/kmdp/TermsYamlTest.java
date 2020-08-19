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

import io.swagger.models.Swagger;
import org.junit.jupiter.api.Test;

import static edu.mayo.kmdp.util.SwaggerTestUtil.parseValidateGroup;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TermsYamlTest {

  @Test
  public void testKAOTerms() {
    Swagger model = parseValidateGroup(
        "/yaml/org/omg/spec/api4kp/_20200801/taxonomy/knowledgeoperation/_20200801/KnowledgeProcessingOperation.yaml");
    assertEquals(2, model.getDefinitions().size());
  }

  @Test
  public void testSkosTerms() {
    Swagger model = parseValidateGroup(
        "/yaml/edu/mayo/ontology/taxonomies/kmdo/relatedconcept/RelatedConcept.yaml");
    assertEquals(2, model.getDefinitions().size());
  }
}
