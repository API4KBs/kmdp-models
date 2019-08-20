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

import static edu.mayo.kmdp.util.JenaUtil.datA;
import static edu.mayo.kmdp.util.JenaUtil.objA;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig.OWLtoSKOSTxParams;
import edu.mayo.kmdp.util.JenaUtil;
import java.util.UUID;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.PrintUtil;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class IdentifiersTest extends TestBase {

  private static String NS = "http://my.edu/test";

  @BeforeAll
  public static void init() {
    TestBase.init();
    PrintUtil.registerPrefix("tgt", NS + "#");
  }

  @Test
  public void testConceptUrisFromLocalName() {

    Owl2SkosConfig cfg = new Owl2SkosConfig()
        .with(OWLtoSKOSTxParams.TGT_NAMESPACE, NS)
        .with(OWLtoSKOSTxParams.MODE, Modes.CON.name());

    Model m = run(singletonList("/ontology/singleClass.owl"), cfg);

    assertTrue(m.contains(
        objA(NS + "#" + UUID.nameUUIDFromBytes("Klass".getBytes()),
        RDF.type,
        SKOS.Concept)));
  }


  @Test
  public void testConceptUrisFromDCIdentifier() {

    Owl2SkosConfig cfg = new Owl2SkosConfig()
        .with(OWLtoSKOSTxParams.TGT_NAMESPACE, NS)
        .with(OWLtoSKOSTxParams.MODE, Modes.CON.name());

    Model m = run(singletonList("/ontology/singleClassWithDCIdentifier.owl"), cfg);

    assertTrue(m.contains(
        objA(NS + "#" + UUID.nameUUIDFromBytes("klassID".getBytes()),
            RDF.type,
            SKOS.Concept)));
  }


  @Test
  public void testIdentifierFromLocalName() {

    Owl2SkosConfig cfg = new Owl2SkosConfig()
        .with(OWLtoSKOSTxParams.TGT_NAMESPACE, NS)
        .with(OWLtoSKOSTxParams.MODE, Modes.SKOS.name());

    Model m = run(singletonList("/ontology/singleClass.owl"), cfg);

    assertTrue(m.contains(
        datA(NS + "#" + UUID.nameUUIDFromBytes("Klass".getBytes()),
            DCTerms.identifier,
            UUID.nameUUIDFromBytes("Klass".getBytes()).toString())));
  }


  @Test
  public void testIdentifierFromDCIdentifier() {

    Owl2SkosConfig cfg = new Owl2SkosConfig()
        .with(OWLtoSKOSTxParams.TGT_NAMESPACE, NS)
        .with(OWLtoSKOSTxParams.MODE, Modes.SKOS.name());

    Model m = run(singletonList("/ontology/singleClassWithDCIdentifier.owl"), cfg);

    assertTrue(m.contains(
        datA(NS + "#" + UUID.nameUUIDFromBytes("klassID".getBytes()),
            DCTerms.identifier,
            UUID.nameUUIDFromBytes("klassID".getBytes()).toString())));
  }


  @Test
  public void testNotationFromLocalName() {

    Owl2SkosConfig cfg = new Owl2SkosConfig()
        .with(OWLtoSKOSTxParams.TGT_NAMESPACE, NS)
        .with(OWLtoSKOSTxParams.MODE, Modes.SKOS.name());

    Model m = run(singletonList("/ontology/singleClass.owl"), cfg);

    assertTrue(m.contains(
        datA(NS + "#" + UUID.nameUUIDFromBytes("Klass".getBytes()),
            SKOS.notation,
            "Klass")));
  }


  @Test
  public void testNotationFromDCIdentifier() {

    Owl2SkosConfig cfg = new Owl2SkosConfig()
        .with(OWLtoSKOSTxParams.TGT_NAMESPACE, NS)
        .with(OWLtoSKOSTxParams.MODE, Modes.SKOS.name());

    Model m = run(singletonList("/ontology/singleClassWithDCIdentifier.owl"), cfg);

    assertTrue(m.contains(
        datA(NS + "#" + UUID.nameUUIDFromBytes("klassID".getBytes()),
            SKOS.notation,
            "klassID")));
  }



}
