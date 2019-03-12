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
package edu.mayo.kmdp.terms.skos.plugin;

import edu.mayo.kmdp.terms.mireot.MireotExtractor;
import edu.mayo.kmdp.terms.skosifier.Modes;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConverter;
import edu.mayo.kmdp.util.NameUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.OWL2;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import ru.avicomp.ontapi.OntManagers;
import ru.avicomp.ontapi.OntologyManager;

import java.util.Optional;

import static edu.mayo.kmdp.terms.util.JenaUtil.applyVersionToURI;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class VersioningTest {

  @Test
  public void testVersionDetection() {

    MireotExtractor extractor = new MireotExtractor(
        VersioningTest.class.getResourceAsStream("/version.rdf"));
    Optional<Model> model = extractor.fetch("http://my.org.test/onto#Foo", true);

    assertTrue(model.isPresent());

    OntologyManager om = OntManagers.createONT();
    OWLOntology x = om.addOntology(model.get().getGraph());

    Optional<IRI> iri = x.getOntologyID().getOntologyIRI()
        .filter((i) -> "http://my.org.test/onto".equals(i.getIRIString()));
    assertTrue(iri.isPresent());

    Optional<IRI> viri = x.getOntologyID().getVersionIRI()
        .filter((v) -> "http://my.org.test/onto/1.0.4".equals(v.getIRIString()));
    assertTrue(viri.isPresent());

    assertEquals(1, x.classesInSignature().count());
  }

  @Test
  public void testVersionPropagation() {
    String ontoURI = "http://foo.com/test#";

    MireotExtractor extractor = new MireotExtractor(
        VersioningTest.class.getResourceAsStream("/version.rdf"));
    Optional<Model> mireot = extractor.fetch("http://my.org.test/onto#Foo", true);
    assertTrue(mireot.isPresent());

    Owl2SkosConverter converter = new Owl2SkosConverter(ontoURI, Modes.SKOS);
    Optional<Model> skos = converter.run(mireot.get(), false, false);

    assertTrue(skos.isPresent() && skos.get() instanceof OntModel);

    OntModel onto = (OntModel) skos.get();
    Ontology o = onto.getOntology(ontoURI);

    assertNotNull(o);

    Statement v = onto.getProperty(o, OWL2.versionIRI);
    assertNotNull(v);
    assertEquals("http://foo.com/test/1.0.4", v.getObject().toString());
  }

  @Test
  public void testVersionFragmentExtraction() {
    assertEquals("",
        NameUtils.strip("",
            ""));
    assertEquals("abc",
        NameUtils.strip("",
            "abc"));
    assertEquals("",
        NameUtils.strip("abc",
            ""));

    assertEquals("a",
        NameUtils.strip("xx",
            "axx"));

    assertEquals("abc",
        NameUtils.strip("xx",
            "xxabc"));

    assertEquals("abc",
        NameUtils.strip("xx",
            "xabcx"));

    assertEquals("/v1",
        NameUtils.strip("http://foo.bar/test",
            "http://foo.bar/test/v1"));
    assertEquals("v1/",
        NameUtils.strip("http://foo.bar/test",
            "http://foo.bar/v1/test"));

    assertEquals("2012/REC--20120405",
        NameUtils.strip("http://www.w3.org/TR/xmlschema11-1/",
            "http://www.w3.org/TR/2012/REC-xmlschema11-1-20120405/"));

    assertEquals("2.0",
        NameUtils.strip("http://www.example.com/my/",
            "http://www.example.com/my/2.0"));

    assertEquals("2.0/",
        NameUtils.strip("http://www.example.com/my/",
            "http://www.example.com/2.0/my"));

  }

  @Test
  public void testVersionFragmentApplication() {
    assertEquals("http://foo.com/sa/2012",
        applyVersionToURI("http://foo.com/sa",
            "2012"));

    assertEquals("http://foo.com/sa/2012/",
        applyVersionToURI("http://foo.com/sa/",
            "2012/"));

    assertEquals("http://foo.com/sa/2012",
        applyVersionToURI("http://foo.com/sa/",
            "/2012"));

    assertEquals("http://foo.com/sa/2012/",
        applyVersionToURI("http://foo.com/sa#",
            "2012/"));


  }

}
