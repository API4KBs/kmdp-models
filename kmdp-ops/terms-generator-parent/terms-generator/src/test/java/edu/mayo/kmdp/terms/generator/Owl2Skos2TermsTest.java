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
import edu.mayo.kmdp.terms.example.MockTermsDirectory;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationParams;
import edu.mayo.kmdp.util.NameUtils;
import org.apache.jena.rdf.model.Model;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;
import org.junit.rules.TemporaryFolder;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import ru.avicomp.ontapi.OntManagers;
import ru.avicomp.ontapi.OntologyManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Optional;

import static edu.mayo.kmdp.terms.generator.SkosTerminologyAbstractor.SKOS_NAMESPACE;
import static edu.mayo.kmdp.terms.mireot.MireotExtractor.extract;
import static edu.mayo.kmdp.terms.skosifier.Owl2SkosConverter.convert;
import static edu.mayo.kmdp.util.CodeGenTestBase.ensureSuccessCompile;
import static edu.mayo.kmdp.util.CodeGenTestBase.getNamedClass;
import static edu.mayo.kmdp.util.CodeGenTestBase.showDirContent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


@EnableRuleMigrationSupport
public class Owl2Skos2TermsTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void testOWLtoTerms() throws IOException {

    String owlPath = "/cito.rdf";
    String entityURI = "http://purl.org/spar/cito/cites";
    String targetNS = "http://test.skos.foo/Cito";

    File src = folder.newFolder("src");
    File tgt = folder.newFolder("tgt");

    OntologyManager manager = OntManagers.createONT();

    Optional<Model> skosModel = extract(Owl2Skos2TermsTest.class.getResourceAsStream(owlPath),
        entityURI).flatMap((extract) -> convert(extract,
        targetNS,
        false,
        true));

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
