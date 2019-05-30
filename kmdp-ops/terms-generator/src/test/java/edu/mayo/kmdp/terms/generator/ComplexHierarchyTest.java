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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.ConceptScheme;
import edu.mayo.kmdp.terms.generator.SkosTerminologyAbstractor.ConceptGraph;
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
import java.util.stream.Collectors;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLOntology;
import ru.avicomp.ontapi.OntManagers;
import ru.avicomp.ontapi.OntologyManager;

public class ComplexHierarchyTest {

  @Test
  public void testGenerateConceptsHierarchy() {
    ConceptScheme<Term> scheme = doGenerate(Modes.SKOS);
    assertNotNull(scheme);

    List<Term> list = scheme.getConcepts().collect(Collectors.toList());
    assertEquals(15, list.size());

    assertTrue(list.stream()
        .anyMatch((t) -> ("http://test.foo#" + UUID.nameUUIDFromBytes("ClinicalRule".getBytes()))
            .equals(t.getConceptId().toString())));
  }

  @Test
  public void testGenerateConceptsHierarchyFromOntology() {
    ConceptScheme<Term> scheme = doGenerate(Modes.SKOS);
    assertNotNull(scheme);

    List<Term> list = scheme.getConcepts().collect(Collectors.toList());
    assertEquals(15, list.size());

    assertTrue(list.stream()
        .anyMatch(
            (t) -> "http://test.org/KAO#ClinicalKnowledgeAsset".equals(t.getRef().toString())));
  }


  @Test
  public void testTopConcept() {
    ConceptScheme<Term> scheme = doGenerate(Modes.SKOS);
    assertNotNull(scheme);

    assertTrue(scheme.getTopConcept().isPresent());
    assertEquals("KnowledgeAssetCategory",
        scheme.getTopConcept().map(Term::getLabel).orElse("missing"));
  }


  private ConceptScheme<Term> doGenerate(final Modes modes) {
    try {
      OntologyManager manager = OntManagers.createONT();

      Optional<Model> skosModel = new MireotExtractor()
          .fetch(Owl2Skos2TermsTest.class.getResourceAsStream("/kac-test.rdf"),
              URI.create("http://test.org/KAO#ClinicalKnowledgeAsset"),
              new MireotConfig())
          .flatMap((extract) -> new Owl2SkosConverter().apply(extract,
              new Owl2SkosConfig()
                  .with(OWLtoSKOSTxParams.TGT_NAMESPACE, "http://test.foo")
                  .with(OWLtoSKOSTxParams.MODE, modes)
                  .with(OWLtoSKOSTxParams.SCHEME_NAME,"KnowledgeAssetCategories")
                  .with(OWLtoSKOSTxParams.TOP_CONCEPT_NAME,"KnowledgeAssetCategory")
                  .with(OWLtoSKOSTxParams.FLATTEN, true)
                  .with(OWLtoSKOSTxParams.VALIDATE, false)));

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

      ConceptGraph schemes = new SkosTerminologyAbstractor().traverse(skosOntology.get(),
          false);

      Optional<ConceptScheme<Term>> scheme  = schemes.getConceptScheme(
          URI.create("http://test.foo#" + uuid("KnowledgeAssetCategories")));
      assertTrue(scheme.isPresent());

      return scheme.get();
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    return null;
  }

}
