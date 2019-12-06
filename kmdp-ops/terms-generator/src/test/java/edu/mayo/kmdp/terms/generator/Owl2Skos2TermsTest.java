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

import static edu.mayo.kmdp.util.CodeGenTestBase.ensureSuccessCompile;
import static edu.mayo.kmdp.util.CodeGenTestBase.getNamedClass;
import static edu.mayo.kmdp.util.CodeGenTestBase.initFolder;
import static edu.mayo.kmdp.util.Util.uuid;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.MockTermsJsonAdapter;
import edu.mayo.kmdp.terms.MockTermsXMLAdapter;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig.EnumGenerationParams;
import edu.mayo.kmdp.terms.generator.config.SkosAbstractionConfig;
import edu.mayo.kmdp.terms.generator.config.SkosAbstractionConfig.SkosAbstractionParameters;
import edu.mayo.kmdp.terms.generator.internal.ConceptGraph;
import edu.mayo.kmdp.terms.mireot.MireotConfig;
import edu.mayo.kmdp.terms.mireot.MireotExtractor;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig.OWLtoSKOSTxParams;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConverter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.SKOS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import ru.avicomp.ontapi.OntologyManager;


class Owl2Skos2TermsTest {

  @TempDir
  public Path tmp;

  @Test
  void testOWLtoTerms() {
    File folder = tmp.toFile();

    String owlPath = "/cito.rdf";
    String entityURI = "http://purl.org/spar/cito/cites";
    String targetNS = "http://test.skos.foo/Cito";

    File src = initFolder(folder,"src");
    File tgt = initFolder(folder,"tgt");

    OntologyManager manager = TestHelper.initManager();

    Owl2SkosConfig cfg = new Owl2SkosConfig()
        .with(OWLtoSKOSTxParams.TGT_NAMESPACE, targetNS)
        .with(OWLtoSKOSTxParams.FLATTEN, false)
        .with(OWLtoSKOSTxParams.TOP_CONCEPT_NAME, "Cito")
        .with(OWLtoSKOSTxParams.ADD_IMPORTS, true);

    Optional<Model> skosModel = new MireotExtractor()
        .fetch(Owl2Skos2TermsTest.class.getResourceAsStream(owlPath),
            URI.create(entityURI),
            new MireotConfig()).flatMap((extract) -> new Owl2SkosConverter().apply(extract, cfg));


    if (!skosModel.isPresent()) {
      fail("Unable to generate skos model");
    }
    if (!manager.contains(IRI.create(SKOS.uri))) {
//			try {
//				InputStream skos = Owl2Skos2TermsTest.class.getResourceAsStream( "/ontology/skos.rdf" );
//				org.openrdf.model.Model skosModel = ModelFactory.createOntologyModel().read(  )
//			} catch ( OWLOntologyCreationException e ) {
//				e.printStackTrace();
//				fail( e.getMessage() );
//			}
    }

    Optional<OWLOntology> skosOntology = skosModel.map(Model::getGraph)
        .map(manager::addOntology);

    assertTrue(skosOntology.isPresent());
    ConceptGraph graph = new SkosTerminologyAbstractor()
        .traverse(skosOntology.get(),new SkosAbstractionConfig()
            .with(SkosAbstractionParameters.REASON,false));

    new JavaEnumTermsGenerator().generate(graph,
        new EnumGenerationConfig()
            .with(EnumGenerationParams.JSON_ADAPTER, MockTermsJsonAdapter.class.getName())
            .with(EnumGenerationParams.XML_ADAPTER, MockTermsXMLAdapter.class.getName()),
        src);

//    showDirContent(folder);
    ensureSuccessCompile(src, src, tgt);

    try {
      Class<?> scheme = getNamedClass("foo.skos.test.cito.Cito", tgt);
      assertTrue(scheme.isEnum());

      Field ns = scheme.getField("SCHEME_ID");
      assertEquals(uuid("Cito").toString(), ns.get(null));

      Term cd = (Term) scheme.getEnumConstants()[0];
      assertEquals("cites", cd.getTag());


    } catch (IllegalAccessException | NoSuchFieldException e) {
      fail(e.getMessage());
    }

  }

}
