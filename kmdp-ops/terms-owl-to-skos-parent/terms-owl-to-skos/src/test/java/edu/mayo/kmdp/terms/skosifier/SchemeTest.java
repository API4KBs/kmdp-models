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
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.terms.mireot.EntityTypes;
import edu.mayo.kmdp.terms.mireot.MireotConfig;
import edu.mayo.kmdp.terms.mireot.MireotConfig.MireotParameters;
import edu.mayo.kmdp.terms.mireot.MireotExtractor;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig.OWLtoSKOSTxParams;
import edu.mayo.kmdp.util.JenaUtil;
import edu.mayo.kmdp.util.Util;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.PrintUtil;
import org.apache.jena.vocabulary.DC_11;
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
        .with(OWLtoSKOSTxParams.SCHEME_NAME,schemeName);

    Model result = run(singletonList("/ontology/singleClass.owl"), cfg);

    String id = UUID.nameUUIDFromBytes(schemeName.getBytes()).toString();
    String subj = NS + "#" + id;

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
            ResourceFactory.createResource(NS+"#"+id+"_Top"))));

  }



}
