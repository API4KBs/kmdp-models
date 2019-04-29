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

import static edu.mayo.kmdp.util.JenaUtil.dat_a;
import static edu.mayo.kmdp.util.JenaUtil.obj_a;
import static edu.mayo.kmdp.util.Util.uuid;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig.OWLtoSKOSTxParams;
import edu.mayo.kmdp.util.JenaUtil;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.PrintUtil;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


public class SchemeTest extends TestBase {

  private static String NS = "http://my.edu/test";

  @BeforeAll
  public static void init() {
    TestBase.init();
    PrintUtil.registerPrefix("tgt", NS + "#");
  }


  @Test
  public void testSchemeName() {

    String schemeName = "MyScheme";

    Owl2SkosConfig cfg = new Owl2SkosConfig()
        .with(OWLtoSKOSTxParams.TGT_NAMESPACE, NS)
        .with(OWLtoSKOSTxParams.MODE, Modes.LEX_CON)
        .with(OWLtoSKOSTxParams.SCHEME_NAME, schemeName);

    Model result = run(singletonList("/ontology/singleClass.owl"), cfg);

    String subj = NS + "#" + uuid(schemeName);

    JenaUtil.toSystemOut(result);

    assertTrue(result.contains(
        dat_a(subj,
            RDFS.label,
            schemeName)));
    assertTrue(result.contains(
        dat_a(subj,
            SKOS.prefLabel,
            schemeName)));
    assertTrue(result.contains(
        obj_a(subj,
            RDF.type,
            SKOS.ConceptScheme)));
    assertTrue(result.contains(
        obj_a(subj,
            SKOS.hasTopConcept,
            ResourceFactory.createResource(NS + "#" + uuid("MyScheme_Top")))));
    assertTrue(result.contains(
        dat_a(NS + "#" + uuid("MyScheme_Top"),
            RDFS.label,
            "MyScheme_Top")));

  }


  @Test
  public void testKnownTopConcept() {

    String schemeName = "MyScheme";
    String topConcept = "Parent";

    Owl2SkosConfig cfg = new Owl2SkosConfig()
        .with(OWLtoSKOSTxParams.TGT_NAMESPACE, NS)
        .with(OWLtoSKOSTxParams.MODE, Modes.LEX_CON)
        .with(OWLtoSKOSTxParams.SCHEME_NAME, schemeName)
        .with(OWLtoSKOSTxParams.TOP_CONCEPT_NAME, topConcept);

    Model result = run(singletonList("/ontology/hierTest.owl"), cfg);

    String scheme = NS + "#" + uuid(schemeName);
    String top = NS + "#" + uuid(topConcept);

    assertTrue(result.contains(
        obj_a(scheme,
            SKOS.hasTopConcept,
            ResourceFactory.createResource(top))));
    assertTrue(result.contains(
        dat_a(top,
            RDFS.label,
            topConcept)));
    assertTrue(result.contains(
        obj_a(top,
            SKOS.broader,
            ResourceFactory.createResource(top))));
    assertTrue(result.contains(
        obj_a(NS + "#" + uuid("Child"),
            SKOS.broader,
            ResourceFactory.createResource(top))));


  }
}
