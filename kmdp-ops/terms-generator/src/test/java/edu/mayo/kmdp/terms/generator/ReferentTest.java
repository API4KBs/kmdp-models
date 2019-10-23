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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.generator.config.SkosAbstractionConfig;
import edu.mayo.kmdp.terms.generator.config.SkosAbstractionConfig.SkosAbstractionParameters;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig.OWLtoSKOSTxParams;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConverter;
import java.util.Optional;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLOntology;
import ru.avicomp.ontapi.OntologyManager;

public class ReferentTest {

  @Test
  public void testReferent() {
    SkosTerminologyAbstractor.ConceptGraph graph = doGenerate();
    Optional<Term> trm = graph.getConceptSchemes().stream()
        .findFirst()
        .flatMap((cs) -> cs.getConcepts().filter((t) -> t.getLabel().contains("Parent")).findFirst());
    assertTrue(trm.isPresent());
    assertEquals("http://org.test/labelsTest#Parent",
        trm.get().getRef().toString());
  }


  public static SkosTerminologyAbstractor.ConceptGraph doGenerate() {

    String owlPath = "/singleClass.rdf";

    OntologyManager manager = TestHelper.initManager();

    Owl2SkosConfig cfg = new Owl2SkosConfig()
        .with(OWLtoSKOSTxParams.TGT_NAMESPACE, "http://test.foo")
        .with(OWLtoSKOSTxParams.FLATTEN, false)
        .with(OWLtoSKOSTxParams.ADD_IMPORTS, false);

    Optional<Model> skosModel = new Owl2SkosConverter().apply(ModelFactory.createDefaultModel()
        .read(ReferentTest.class.getResourceAsStream(owlPath), null), cfg);

    if (!skosModel.isPresent()) {
      fail("Unable to generate skos model");
    }

    Optional<OWLOntology> skosOntology = skosModel.map(Model::getGraph)
        .map(manager::addOntology);

    if (!skosOntology.isPresent()) {
      fail("Unable to extract graph");
    }

    return new SkosTerminologyAbstractor()
        .traverse(skosOntology.get(),new SkosAbstractionConfig()
            .with(SkosAbstractionParameters.REASON,true));

  }

}
