/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp.terms.skos.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.SKOS;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.search.EntitySearcher;

class SkosGeneratorPluginTest {

  private String localName;
  private File temp;


  @TempDir
  Path tmpFolder;

  private SkosGeneratorPlugin init() {
    this.localName = "skosTest.rdf";
    this.temp = new File(tmpFolder.toFile(), "temp");

    SkosGeneratorPlugin mojo = new SkosGeneratorPlugin();

    mojo.setEntityOnly(false);
    mojo.setReason(false);

    mojo.setOwlSourceURL("/cito.rdf");
    mojo.setTargetURI("http://purl.org/spar/cito/cites");

    mojo.setSkosNamespace("http://test.skos.foo");
    mojo.setOutputDirectory(temp);
    mojo.setSkosOutputFiles(Collections.singletonList(localName));

    return mojo;
  }

  @Test
  void testUsingTempFolder() {
    SkosGeneratorPlugin mojo = init();

    try {
      mojo.execute();
    } catch (MojoExecutionException e) {
      fail(e.getMessage());
    }

    OWLOntology onto = asOntology();
    assertNotNull(onto);
    OWLDataFactory odf = onto.getOWLOntologyManager().getOWLDataFactory();
    Set<OWLIndividual> inds = EntitySearcher
        .getIndividuals(odf.getOWLClass(SKOS.Concept.getURI()), onto)
        .collect(Collectors.toSet());

    assertEquals(45, inds.size());
  }

  private OWLOntology asOntology() {
    File f = new File(temp.getAbsolutePath() + File.separator + localName);

    try {
      OWLOntologyLoaderConfiguration conf = new OWLOntologyLoaderConfiguration()
          .setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT)
          .addIgnoredImport(IRI.create(DCTerms.getURI()));

      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      return manager.loadOntologyFromOntologyDocument(
          new FileDocumentSource(f),
          conf);
    } catch (OWLOntologyCreationException e) {
      fail(e.getMessage());
    }
    return null;
  }

}
