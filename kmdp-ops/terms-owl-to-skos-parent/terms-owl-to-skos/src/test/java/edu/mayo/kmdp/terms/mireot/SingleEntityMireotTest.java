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
package edu.mayo.kmdp.terms.mireot;

import static edu.mayo.kmdp.util.JenaUtil.dat_a;
import static edu.mayo.kmdp.util.JenaUtil.obj_a;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.util.JenaUtil;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.DC_11;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class SingleEntityMireotTest extends BaseMireotTest {

  public static Stream<Arguments> argProvider() {
    return Stream.of(
        Arguments.of("Book", 5),
        Arguments.of("stores", 5),
        Arguments.of("hasSubtitle", 5),
        Arguments.of("hard-drive", 6));
  }

  @ParameterizedTest
  @MethodSource("argProvider")
  public void testEntityMireot(String resource, Integer count) {
    MireotExtractor extractor = newExtractor("/ontology/fabio.rdf",
        "http://purl.org/spar/fabio/");
    Optional<Model> chunk = extractor.fetchResource(baseUri + resource);

    assertTrue(chunk.isPresent());

//    JenaUtil.toSystemOut(chunk.get());

    assertEquals(count, JenaUtil.sizeOf(chunk.get()));
  }


  @Test
  public void testMireotedProperties() {
    String base = "http://org.test/labelsTest";
    String klass = base + "#Klass";
    MireotExtractor extractor = newExtractor("/ontology/singleClassWithAnnos.owl",
        base);
    Optional<Model> chunk = extractor.fetchResource(klass);

    assertTrue(chunk.isPresent());
    Model m = chunk.get();

    assertTrue(m.contains(obj_a(klass, RDF.type, OWL2.Class)));
    assertTrue(m.contains(dat_a(klass, RDFS.label, "My Class")));
    assertTrue(m.contains(dat_a(klass, RDFS.comment, "comment")));
    assertTrue(m.contains(dat_a(klass, RDFS.comment, "comment")));
    assertTrue(m.contains(dat_a(klass, RDFS.isDefinedBy, base)));
    assertTrue(m.contains(dat_a(klass, SKOS.prefLabel, "Pref Way to name My Class")));
    assertTrue(m.contains(dat_a(klass, SKOS.example, "example")));
    assertTrue(m.contains(dat_a(klass, SKOS.note, "note")));
    assertTrue(m.contains(dat_a(klass, SKOS.definition, "definition")));
    assertTrue(m.contains(dat_a(klass, DC_11.identifier, "id0001")));

  }


}
