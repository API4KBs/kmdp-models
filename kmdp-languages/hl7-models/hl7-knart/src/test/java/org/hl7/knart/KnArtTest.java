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
package org.hl7.knart;

import org.hl7.cdsdt.r2.ST;
import org.hl7.elm.r1.And;
import org.hl7.knowledgeartifact.r1.Condition;
import org.hl7.knowledgeartifact.r1.Conditions;
import org.hl7.knowledgeartifact.r1.KnowledgeDocument;
import org.hl7.knowledgeartifact.r1.Metadata;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KnArtTest {

  @Test
  public void testCompiled() {
    KnowledgeDocument kdoc = new KnowledgeDocument();

    kdoc.setMetadata(new Metadata().withTitle(new ST().withValue("Test")));
    kdoc.withConditions(new Conditions().withCondition(new Condition().withLogic(new And())));

    assertEquals("Test", kdoc.getMetadata().getTitle().getValue());
  }
}
