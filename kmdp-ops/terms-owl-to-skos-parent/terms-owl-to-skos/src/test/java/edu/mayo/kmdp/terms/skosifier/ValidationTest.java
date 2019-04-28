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

import static edu.mayo.kmdp.util.Util.uuid;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.terms.mireot.MireotConfig;
import edu.mayo.kmdp.terms.mireot.MireotConfig.MireotParameters;
import edu.mayo.kmdp.terms.mireot.MireotExtractor;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig.OWLtoSKOSTxParams;
import edu.mayo.kmdp.util.JenaUtil;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.PrintUtil;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ValidationTest extends TestBase {

  private static String NS = "http://my.edu/test";

  @BeforeAll
  public static void init() {
    TestBase.init();
    PrintUtil.registerPrefix("tgt", NS + "#");
  }

  @Test
  public void testResultNoInference() {

    Owl2SkosConfig cfg = new Owl2SkosConfig()
        .with(OWLtoSKOSTxParams.TGT_NAMESPACE,NS)
        .with(OWLtoSKOSTxParams.MODE,Modes.CON.name());

    Model m = run(singletonList("/ontology/singleClass.owl"),cfg);

    JenaUtil.iterateAndStreamModel(m, System.out, PrintUtil::print);

    Resource klass = m.getResource(NS + "#" + uuid("Klass"));
    assertNotNull(klass);
    assertNotNull(m.getProperty(klass, SKOS.inScheme));
    assertNotNull(m.getProperty(klass, SKOS.broader));
    assertNotNull(m.getProperty(klass, RDF.type));

    String schemeId = UUID.nameUUIDFromBytes("test_Scheme".getBytes()).toString();
    Resource top = m.getResource(NS + "#" + schemeId + "_Top");

    assertNotNull(top);
    assertNotNull(m.getProperty(top, RDF.type));

    Resource cs = m.getResource(NS + "#" + schemeId);
    assertNotNull(cs);
    assertNotNull(m.getProperty(cs, SKOS.hasTopConcept));
    assertNotNull(m.getProperty(cs, RDF.type));

    //m.write( System.out );
  }

  @Test
  public void testResultWithInference() {

    Owl2SkosConfig cfg = new Owl2SkosConfig()
        .with(OWLtoSKOSTxParams.TGT_NAMESPACE,NS)
        .with(OWLtoSKOSTxParams.MODE,Modes.CON.name())
        .with(OWLtoSKOSTxParams.VALIDATE,Boolean.TRUE);

    Model m = run(singletonList("/ontology/singleClass.owl"),cfg);

    Resource klass = m.getResource(NS + "#" + uuid("Klass"));
    assertNotNull(klass);
    assertNotNull(m.getProperty(klass, SKOS.inScheme));
    assertNotNull(m.getProperty(klass, SKOS.broader));
    assertNotNull(m.getProperty(klass, RDF.type));

    String schemeId = UUID.nameUUIDFromBytes("test_Scheme".getBytes()).toString();
    Resource top = m.getResource(NS + "#" + schemeId + "_Top");
    assertNotNull(top);
    assertNotNull(m.getProperty(top, RDF.type));

    Resource cs = m.getResource(NS + "#" + schemeId);
    assertNotNull(cs);
    assertNotNull(m.getProperty(cs, SKOS.hasTopConcept));
    assertNotNull(m.getProperty(cs, RDF.type));

  }


  @Test
  public void testVersioned() {


    MireotConfig mfg = new MireotConfig()
        .with(MireotParameters.BASE_URI, "http://org.test/labelsTest");
    Owl2SkosConfig cfg = new Owl2SkosConfig()
        .with(OWLtoSKOSTxParams.TGT_NAMESPACE,NS)
        .with(OWLtoSKOSTxParams.MODE,Modes.SKOS.name());

    Optional<Model> model = new MireotExtractor().fetch(
        ValidationTest.class.getResourceAsStream("/ontology/version.rdf"),
        URI.create("http://org.test/labelsTest#Parent"),
        mfg)
        .flatMap((m) -> new Owl2SkosConverter().apply(m,cfg) );

    assertTrue(model.isPresent());

    StmtIterator s = model.get().listStatements(ResourceFactory.createResource(NS),
        OWL2.versionIRI,
        (Resource) null);

    assertTrue(s.hasNext());
    Statement st = s.nextStatement();
    assertTrue(st.getObject().toString().contains("20190108"));
  }
}
