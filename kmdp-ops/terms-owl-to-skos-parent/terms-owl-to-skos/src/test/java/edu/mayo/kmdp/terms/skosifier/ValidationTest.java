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
package edu.mayo.kmdp.terms.skosifier;

import edu.mayo.kmdp.util.JenaUtil;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.PrintUtil;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValidationTest extends TestBase {

  private Owl2SkosConverter cv;

  private static String NS = "http://my.edu/test";

  @BeforeAll
  public static void init() {
    TestBase.init();
    PrintUtil.registerPrefix("tgt", NS + "#");
  }

  @Test
  public void testResultNoInference() {

    cv = new Owl2SkosConverter(NS, Modes.CON);

    Optional<Model> result = cv.run(singletonList("/ontology/singleClass.owl"),
        false,
        false);
    assertTrue(result.isPresent());

    Model m = result.get();

    //JenaUtil.iterateAndStreamModel(m, System.out, PrintUtil::print);

    Resource klass = m.getResource(NS + "#Klass");
    assertNotNull(klass);
    assertNotNull(m.getProperty(klass, SKOS.inScheme));
    assertNotNull(m.getProperty(klass, SKOS.broader));
    assertNotNull(m.getProperty(klass, RDF.type));

    Resource top = m.getResource(NS + "#test_Scheme_Top");
    assertNotNull(top);
    assertNotNull(m.getProperty(top, RDF.type));

    Resource cs = m.getResource(NS + "#test_Scheme");
    assertNotNull(cs);
    assertNotNull(m.getProperty(cs, SKOS.hasTopConcept));
    assertNotNull(m.getProperty(cs, RDF.type));

    //m.write( System.out );
  }

  @Test
  public void testResultWithInference() {

    cv = new Owl2SkosConverter(NS, Modes.CON);

    Optional<Model> result = cv.run(singletonList("/ontology/singleClass.owl"),
        true,
        false);
    assertTrue(result.isPresent());
    Model m = result.get();

    Resource klass = m.getResource(NS + "#Klass");
    assertNotNull(klass);
    assertNotNull(m.getProperty(klass, SKOS.inScheme));
    assertNotNull(m.getProperty(klass, SKOS.broader));
    assertNotNull(m.getProperty(klass, RDF.type));

    Resource top = m.getResource(NS + "#test_Scheme_Top");
    assertNotNull(top);
    assertNotNull(m.getProperty(top, RDF.type));

    Resource cs = m.getResource(NS + "#test_Scheme");
    assertNotNull(cs);
    assertNotNull(m.getProperty(cs, SKOS.hasTopConcept));
    assertNotNull(m.getProperty(cs, RDF.type));


  }
}
