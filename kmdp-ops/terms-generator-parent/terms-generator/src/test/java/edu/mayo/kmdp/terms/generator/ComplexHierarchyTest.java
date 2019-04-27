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

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.skosifier.Modes;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig.OWLtoSKOSTxParams;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConverter;
import java.util.UUID;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLOntology;
import ru.avicomp.ontapi.OntManagers;
import ru.avicomp.ontapi.OntologyManager;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static edu.mayo.kmdp.terms.mireot.MireotExtractor.extract;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ComplexHierarchyTest {

  @Test
  public void testGenerateConceptsHierarchy() {
    List<Term> list = doGenerate(Modes.SKOS);
    assertEquals(15, list.size());

    assertTrue(list.stream()
        .anyMatch((t) -> ("http://test.foo#" + UUID.nameUUIDFromBytes("ClinicalRule".getBytes()))
            .equals(t.getRef().toString())));
  }

  @Test
  public void testGenerateConceptsHierarchyFromOntology() {
    List<Term> list = doGenerate(Modes.FULL);
    assertEquals(15, list.size());

    assertTrue(list.stream()
        .anyMatch(
            (t) -> "http://test.org/KAO#ClinicalKnowledgeAsset".equals(t.getRef().toString())));
  }


  private List<Term> doGenerate(final Modes modes) {
    try {
      OntologyManager manager = OntManagers.createONT();
      Optional<Model> skosModel = extract(
          Owl2Skos2TermsTest.class.getResourceAsStream("/kac-test.rdf"),
          "http://test.org/KAO#ClinicalKnowledgeAsset")
          .flatMap((extract) -> new Owl2SkosConverter().apply(extract,
              new Owl2SkosConfig()
                  .with(OWLtoSKOSTxParams.TGT_NAMESPACE,"http://test.foo")
              .with(OWLtoSKOSTxParams.MODE,modes)
              .with(OWLtoSKOSTxParams.FLATTEN,true)
              .with(OWLtoSKOSTxParams.VALIDATE,false)));

      if (!skosModel.isPresent()) {
        fail("Unable to generate skos model");
      }

      // OntAPI cannot handle imports properly, but the imports have already been flattened into the skosModel
      // So, for testing purposes only, we remove the imports
      OntModel om = (OntModel) skosModel.get();
      om.remove(om.listStatements(om.getOntology("http://test.foo"),
          OWL.imports,
          (Resource) null).toList());
      // end of workaround

      Optional<OWLOntology> skosOntology = Optional
          .ofNullable(manager.addOntology(om.getBaseModel().getGraph()));

      SkosTerminologyAbstractor abstractor = new SkosTerminologyAbstractor(skosOntology.get(),
          false);

      List<Term> list = abstractor.traverse()
          .getConceptList(URI.create("http://test.foo#test.foo_Scheme"));

      return list;
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    return null;
  }

}
