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

import static edu.mayo.kmdp.terms.generator.SkosTerminologyAbstractor.SKOS_NAMESPACE;
import static edu.mayo.kmdp.util.CodeGenTestBase.ensureSuccessCompile;
import static edu.mayo.kmdp.util.CodeGenTestBase.getNamedClass;
import static edu.mayo.kmdp.util.CodeGenTestBase.initFolder;
import static edu.mayo.kmdp.util.CodeGenTestBase.showDirContent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.example.MockTermsDirectory;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig.EnumGenerationParams;
import edu.mayo.kmdp.terms.mireot.MireotConfig;
import edu.mayo.kmdp.terms.mireot.MireotExtractor;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig.OWLtoSKOSTxParams;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConverter;
import edu.mayo.kmdp.util.NameUtils;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import ru.avicomp.ontapi.OntManagers;
import ru.avicomp.ontapi.OntologyManager;


public class Owl2Skos2TermsTest {

  @TempDir
  public Path tmp;

  @Test
  public void testOWLtoTerms() throws IOException {
    File folder = tmp.toFile();

    String owlPath = "/cito.rdf";
    String entityURI = "http://purl.org/spar/cito/cites";
    String targetNS = "http://test.skos.foo/Cito";

    File src = initFolder(folder,"src");
    File tgt = initFolder(folder,"tgt");

    OntologyManager manager = OntManagers.createONT();

    Owl2SkosConfig cfg = new Owl2SkosConfig()
        .with(OWLtoSKOSTxParams.TGT_NAMESPACE, targetNS)
        .with(OWLtoSKOSTxParams.FLATTEN, false)
        .with(OWLtoSKOSTxParams.ADD_IMPORTS, true);

    Optional<Model> skosModel = new MireotExtractor()
        .fetch(Owl2Skos2TermsTest.class.getResourceAsStream(owlPath),
            URI.create(entityURI),
            new MireotConfig()).flatMap((extract) -> new Owl2SkosConverter().apply(extract, cfg));


    if (!skosModel.isPresent()) {
      fail("Unable to generate skos model");
    }
    if (!manager.contains(IRI.create(SKOS_NAMESPACE))) {
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

    SkosTerminologyAbstractor.ConceptGraph graph = new SkosTerminologyAbstractor(skosOntology.get(),
        true).traverse();

    new JavaEnumTermsGenerator().generate(graph,
        new EnumGenerationConfig()
            .with(EnumGenerationParams.TERMS_PROVIDER, MockTermsDirectory.provider)
            .with(EnumGenerationParams.PACKAGE_NAME,
                NameUtils.nameSpaceURIToPackage(URI.create(targetNS))),
        src);

    showDirContent(folder);
    ensureSuccessCompile(src, src, tgt);

    try {
      Class<?> scheme = getNamedClass("foo.skos.test.cito.Cito", tgt);
      assertTrue(scheme.isEnum());

      Field ns = scheme.getField("schemeID");
      assertEquals("Cito_Scheme", ns.get(null));

      Term cd = Term.class.cast(scheme.getEnumConstants()[0]);
      assertEquals("cites", cd.getTag());


    } catch (IllegalAccessException | NoSuchFieldException e) {
      e.printStackTrace();
    }

  }

}
