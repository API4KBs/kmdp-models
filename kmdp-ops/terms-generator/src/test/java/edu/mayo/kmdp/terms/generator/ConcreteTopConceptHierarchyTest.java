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

import static edu.mayo.kmdp.util.Util.uuid;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.mayo.kmdp.terms.mireot.MireotConfig;
import edu.mayo.kmdp.terms.mireot.MireotExtractor;
import edu.mayo.kmdp.terms.skosifier.Modes;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig.OWLtoSKOSTxParams;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConverter;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.id.Term;
import org.semanticweb.owlapi.model.OWLOntology;
import ru.avicomp.ontapi.OntologyManager;

public class ConcreteTopConceptHierarchyTest {

  @Test
  public void testGenerateConceptsHierarchy() {
    List<Term> list = doGenerate(Modes.SKOS);
    assertEquals(3, list.size());

    assertTrue(list.stream()
        .anyMatch((t) -> ("http://test.foo#" + UUID.nameUUIDFromBytes("Parent".getBytes()))
            .equals(t.getConceptId().toString())));
    assertTrue(list.stream()
        .anyMatch((t) -> ("http://test.foo#" + UUID.nameUUIDFromBytes("Child".getBytes()))
            .equals(t.getConceptId().toString())));

    assertTrue(list.stream()
        .anyMatch((t) -> ("http://org.test/labelsTest#Parent")
            .equals(t.getReferentId().toString())));
    assertTrue(list.stream()
        .anyMatch((t) -> ("http://org.test/labelsTest#Child")
            .equals(t.getReferentId().toString())));
  }



  private List<Term> doGenerate(final Modes modes) {
    try {
      OntologyManager manager = TestHelper.initManager();

      Optional<Model> skosModel = new MireotExtractor()
          .fetch(Owl2Skos2TermsTest.class.getResourceAsStream("/version.rdf"),
              URI.create("http://org.test/labelsTest#Parent"),
              new MireotConfig())
          .flatMap((extract) -> new Owl2SkosConverter().apply(extract,
              new Owl2SkosConfig()
                  .with(OWLtoSKOSTxParams.TGT_NAMESPACE, "http://test.foo")
                  .with(OWLtoSKOSTxParams.MODE, modes)
                  .with(OWLtoSKOSTxParams.ADD_IMPORTS,false)
                  .with(OWLtoSKOSTxParams.FLATTEN, true)
                  .with(OWLtoSKOSTxParams.VALIDATE, false)));

      if (!skosModel.isPresent()) {
        fail("Unable to generate skos model");
      }

      OntModel om = (OntModel) skosModel.get();

      Optional<OWLOntology> skosOntology = Optional
          .ofNullable(manager.addOntology(om.getBaseModel().getGraph()));

      List<Term> list = new SkosTerminologyAbstractor().traverse(skosOntology.get())
          .getConceptList(URI.create("http://test.foo#" + uuid("test.foo")));

      return list;
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    return null;
  }

}
