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

import org.omg.spec.api4kp._20200801.id.Term;
import edu.mayo.kmdp.terms.ConceptScheme;
import edu.mayo.kmdp.terms.generator.config.SkosAbstractionConfig;
import edu.mayo.kmdp.terms.generator.config.SkosAbstractionConfig.SkosAbstractionParameters;
import edu.mayo.kmdp.terms.generator.internal.ConceptGraph;
import edu.mayo.kmdp.terms.mireot.MireotConfig;
import edu.mayo.kmdp.terms.mireot.MireotExtractor;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig.OWLtoSKOSTxParams;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConverter;
import edu.mayo.kmdp.util.DateTimeUtil;
import edu.mayo.kmdp.util.URIUtil;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLOntology;
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
  public void testOWLtoTerms() {

    String entityURI = "http://org.test/labelsTest#Parent";
    String targetNS = "http://test.skos.foo/Test";

    OntologyManager manager = TestHelper.initManager();

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

    assertTrue(skosOntology.isPresent());
    ConceptGraph graph = new SkosTerminologyAbstractor()
        .traverse(skosOntology.get(), new SkosAbstractionConfig()
            .with(SkosAbstractionParameters.VERSION_PATTERN, ".*/(.*)/.*$")
            .with(SkosAbstractionParameters.REASON, true));

    Collection<ConceptScheme<Term>> schemes = graph.getConceptSchemes();
    assertEquals(1, schemes.size());
    schemes.forEach((s) -> assertNotNull(s.getVersionId()));

    ConceptScheme<Term> scheme = schemes.iterator().next();
    String versionTag = scheme.getVersionTag();
    assertEquals("20190108", versionTag);

    assertEquals("2019-01-08", DateTimeUtil.serializeAsDate(scheme.getEstablishedOn()));
  }

  @Test
  public void testVersionTagPattern() {
    String versionPattern = ".*/(.*)/$";
    URI uri = URI.create("https://o.m.e/t/Stuff/SNAPSHOT/#123");
    uri = URIUtil.normalizeURI(uri);
    Matcher m = Pattern.compile(versionPattern).matcher(uri.toString());
    assertTrue(m.matches());
    assertEquals(1,m.groupCount());
    assertEquals("SNAPSHOT",m.group(1));
  }

}
