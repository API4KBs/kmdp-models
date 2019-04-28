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
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig.OWLtoSKOSTxParams;
import edu.mayo.kmdp.util.JenaUtil;
import java.util.UUID;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.PrintUtil;
import org.apache.jena.vocabulary.DC_11;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class AnnotationsTest extends TestBase {

  private static String NS = "http://my.edu/test";

  @BeforeAll
  public static void init() {
    TestBase.init();
    PrintUtil.registerPrefix("tgt", NS + "#");
  }

  @Test
  public void testMetadataOnSimple() {

    Owl2SkosConfig cfg = new Owl2SkosConfig()
        .with(OWLtoSKOSTxParams.TGT_NAMESPACE, NS)
        .with(OWLtoSKOSTxParams.MODE, Modes.ANN.name());

    Model m = run(singletonList("/ontology/singleClass.owl"), cfg);

    String id = UUID.nameUUIDFromBytes("Klass".getBytes()).toString();
    String subj = NS + "#" + id;

    assertTrue(m.contains(
        dat_a(subj,
            DC_11.identifier,
            id)));
    assertTrue(m.contains(
        dat_a(subj,
            SKOS.prefLabel,
            "Pref Way to name My Class")));
    assertTrue(m.contains(
        dat_a(subj,
            RDFS.label,
            "My Class")));
    assertTrue(m.contains(
        dat_a(subj,
            SKOS.notation,
            "Klass")));

  }

  @Test
  public void testMetadata() {

    Owl2SkosConfig cfg = new Owl2SkosConfig()
        .with(OWLtoSKOSTxParams.TGT_NAMESPACE, NS)
        .with(OWLtoSKOSTxParams.MODE, Modes.ANN.name());

    Model m = run(singletonList("/ontology/singleClassWithAnnos.owl"), cfg);

    JenaUtil.iterateAndStreamModel(m, System.out, PrintUtil::print);

    String id = UUID.nameUUIDFromBytes("id0001".getBytes()).toString();
    String subj = NS + "#" + id;

    JenaUtil.toSystemOut(m);

    assertTrue(m.contains(
        dat_a(subj,
            DC_11.identifier,
            id)));
    assertTrue(m.contains(
        dat_a(subj,
            SKOS.notation,
            "id0001")));
    assertTrue(m.contains(
        dat_a(subj,
            RDFS.comment,
            "comment")));
    assertTrue(m.contains(
        dat_a(subj,
            SKOS.definition,
            "definition")));
    assertTrue(m.contains(
        dat_a(subj,
            SKOS.example,
            "example")));
    assertTrue(m.contains(
        dat_a(subj,
            SKOS.note,
            "note")));
    assertTrue(m.contains(
        dat_a(subj,
            SKOS.hiddenLabel,
            "hidden")));
    assertTrue(m.contains(
        dat_a(subj,
            SKOS.altLabel,
            "alternative")));
    assertTrue(m.contains(
        dat_a(subj,
            SKOS.notation,
            "id0001")));

  }

}
