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
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;
import static org.omg.spec.api4kp._1_0.contrastors.SyntacticRepresentationContrastor.repContrastor;

import edu.mayo.kmdp.comparator.Contrastor.Comparison;
import edu.mayo.ontology.taxonomies.krlanguage._2018._08.KRLanguage;
import edu.mayo.ontology.taxonomies.krserialization._2018._08.KRSerialization;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

public class RepresentationComparisonTest {

  @Test
  public void languageTest() {
    SyntacticRepresentation r1 = rep(KRLanguage.OWL_2).withSerialization(KRSerialization.RDF_XML_Syntax);
    SyntacticRepresentation r2 = rep(KRLanguage.OWL_2).withSerialization(KRSerialization.OWL_Functional_Syntax);
    SyntacticRepresentation r3 = rep(KRLanguage.DMN_1_1);
    SyntacticRepresentation r4 = rep(KRLanguage.OWL_2);

    assertEquals(Comparison.INCOMPARABLE,repContrastor.contrast(r1,r2));
    assertEquals(Comparison.INCOMPARABLE,repContrastor.contrast(r1,r3));
    assertEquals(Comparison.NARROWER,repContrastor.contrast(r1,r4));
  }

}
