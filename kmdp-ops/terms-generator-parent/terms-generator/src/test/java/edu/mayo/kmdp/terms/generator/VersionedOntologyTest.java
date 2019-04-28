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

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.ConceptScheme;
import edu.mayo.kmdp.terms.mireot.MireotConfig;
import edu.mayo.kmdp.terms.mireot.MireotExtractor;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig.OWLtoSKOSTxParams;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConverter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Optional;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLOntology;
import ru.avicomp.ontapi.OntManagers;
import ru.avicomp.ontapi.OntologyManager;


public class VersionedOntologyTest {


  String owl =
      "<rdf:RDF xmlns=\"http://org.test/labelsTest\"\n"
      + "     xml:base=\"http://org.test/labelsTest\"\n"
      + "     xmlns:dct=\"http://purl.org/dc/terms/\"\n"
      + "     xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
      + "     xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
      + "     xmlns:xml=\"http://www.w3.org/XML/1998/namespace\"\n"
      + "     xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n"
      + "     xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"\n"
      + "     xmlns:sm=\"http://www.omg.org/techprocess/ab/SpecificationMetadata/\"\n"
      + "     xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n"
      + "     xmlns:lcc-lr=\"http://www.omg.org/spec/LCC/Languages/LanguageRepresentation/\"\n"
      + "     xmlns:api4kp=\"http://org.test/labelsTest\">\n"
      + "    <owl:Ontology rdf:about=\"http://org.test/labelsTest\">\n"
      + "        <owl:versionIRI rdf:resource=\"http://org.test/20190108/labelsTest\"/>\n"
      + "    </owl:Ontology>\n"
      + "    \n"
      + "<!-- http://org.test/labelsTest#Child -->\n"
      + "\n"
      + "    <owl:Class rdf:about=\"http://org.test/labelsTest#Child\">\n"
      + "        <rdfs:subClassOf rdf:resource=\"http://org.test/labelsTest#Parent\"/>\n"
      + "    </owl:Class>\n"
      + "    \n"
      + "\n"
      + "\n"
      + "    <!-- http://org.test/labelsTest#GrandChild -->\n"
      + "\n"
      + "    <owl:Class rdf:about=\"http://org.test/labelsTest#GrandChild\">\n"
      + "        <rdfs:subClassOf rdf:resource=\"http://org.test/labelsTest#Child\"/>\n"
      + "    </owl:Class>\n"
      + "    \n"
      + "\n"
      + "\n"
      + "    <!-- http://org.test/labelsTest#Parent -->\n"
      + "\n"
      + "    <owl:Class rdf:about=\"http://org.test/labelsTest#Parent\"/>    \n"
      + "\n"
      + "</rdf:RDF>\n";

  @Test
  public void testOWLtoTerms() throws IOException {

    String owlPath = "/version.rdf";
    String entityURI = "http://org.test/labelsTest#Parent";
    String targetNS = "http://test.skos.foo/Test";

    OntologyManager manager = OntManagers.createONT();

    Owl2SkosConfig cfg = new Owl2SkosConfig()
        .with(OWLtoSKOSTxParams.TGT_NAMESPACE, targetNS)
        .with(OWLtoSKOSTxParams.FLATTEN, false)
        .with(OWLtoSKOSTxParams.ADD_IMPORTS, false);

    Optional<Model> skosModel = new MireotExtractor()
        .fetch(
            new ByteArrayInputStream(owl.getBytes()),
            //VersionedOntologyTest.class.getResourceAsStream(owlPath),
            URI.create(entityURI),
            new MireotConfig()).flatMap((extract) -> new Owl2SkosConverter().apply(extract, cfg));

    Optional<OWLOntology> skosOntology = skosModel.map(Model::getGraph)
        .map(manager::addOntology);

    SkosTerminologyAbstractor.ConceptGraph graph = new SkosTerminologyAbstractor(skosOntology.get(),
        true).traverse();

    Collection<ConceptScheme<Term>> schemes = graph.getConceptSchemes();
    assertEquals(1, schemes.size());
    schemes.forEach((s) -> assertNotNull(s.getVersionId()));

  }

}