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
package edu.mayo.kmdp.terms.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.mayo.kmdp.terms.generator.config.SkosAbstractionConfig;
import edu.mayo.kmdp.terms.generator.config.SkosAbstractionConfig.SkosAbstractionParameters;
import edu.mayo.kmdp.terms.generator.internal.ConceptGraph;
import edu.mayo.kmdp.terms.generator.internal.ConceptTermImpl;
import edu.mayo.kmdp.terms.mireot.EntityTypes;
import edu.mayo.kmdp.terms.mireot.MireotConfig;
import edu.mayo.kmdp.terms.mireot.MireotConfig.MireotParameters;
import edu.mayo.kmdp.terms.mireot.MireotExtractor;
import edu.mayo.kmdp.terms.skosifier.Modes;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig.OWLtoSKOSTxParams;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConverter;
import java.net.URI;
import java.util.Optional;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.id.Term;
import org.semanticweb.owlapi.model.OWLOntology;
import ru.avicomp.ontapi.OntologyManager;

class NotationTest {

  private static OWLOntology ontology;

  @BeforeEach
  void init() {
    ontology = doRead();
  }

  @Test
  void testNotationA() {
    ConceptGraph graph = doGenerateGraph(ontology, "urn:CodeA");
    Term trm = getAndCheck(graph);
    assertEquals("123", trm.getTag());
  }

  @Test
  void testNotationB() {
    ConceptGraph graph = doGenerateGraph(ontology, "urn:CodeB");
    Term trm = getAndCheck(graph);
    assertEquals("00", trm.getTag());
  }

  private Term getAndCheck(ConceptGraph graph) {
    assertEquals(1, graph.getConceptSchemes().size());

    ConceptTermImpl trm = (ConceptTermImpl) graph.getConceptSchemes().iterator().next().getConcepts()
        .findFirst().orElse(null);
    assertNotNull(trm);

    assertEquals("9f711427-f811-37db-b591-4bdf1d438d16",
        trm.getUuid().toString());
    assertEquals("9f711427-f811-37db-b591-4bdf1d438d16",
        trm.getConceptId().getFragment());
    assertEquals(2, trm.getNotations().size());
    assertTrue(trm.getNotations().contains("00"));
    assertTrue(trm.getNotations().contains("123"));
    return trm;
  }


  private ConceptGraph doGenerateGraph(OWLOntology onto, String type) {
    ConceptGraph schemes = new SkosTerminologyAbstractor()
        .traverse(onto,
            new SkosAbstractionConfig()
                .with(SkosAbstractionParameters.TAG_TYPE, type)
                .with(SkosAbstractionParameters.REASON, false));
    assertNotNull(schemes);
    return schemes;
  }


  private OWLOntology doRead() {
    try {

      OntologyManager manager = TermsGeneratorTestHelper.initManager();

      Optional<Model> skosModel = new MireotExtractor()
          .fetch(NotationTest.class.getResourceAsStream("/multipleNotation.rdf"),
              URI.create("https://foo.test/AThing"),
              new MireotConfig()
                  .with(MireotParameters.ENTITY_TYPE, EntityTypes.INST))
          .flatMap((extract) -> new Owl2SkosConverter().apply(extract,
              new Owl2SkosConfig()
                  .with(OWLtoSKOSTxParams.TGT_NAMESPACE, "https://foo.test")
                  .with(OWLtoSKOSTxParams.MODE, Modes.SKOS)
                  .with(OWLtoSKOSTxParams.SCHEME_NAME, "Test")
                  .with(OWLtoSKOSTxParams.TOP_CONCEPT_NAME, "Root")
                  .with(OWLtoSKOSTxParams.FLATTEN, true)
                  .with(OWLtoSKOSTxParams.VALIDATE, false)));

      if (skosModel.isEmpty()) {
        fail("Unable to generate skos model");
      }

      skosModel.get().add(
          ResourceFactory.createResource("urn:CodeA"),
          RDF.type,
          RDFS.Datatype);
      skosModel.get().add(
          ResourceFactory.createResource("urn:CodeB"),
          RDF.type,
          RDFS.Datatype);

      Optional<OWLOntology> skosOntology = Optional
          .ofNullable(manager.addOntology(skosModel.get().getGraph()));

      return skosOntology.orElse(null);

    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    return null;
  }
}
