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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.jena.vocabulary.SKOS;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.search.EntitySearcher;

public class SkosGeneratorPluginTest {

  private String localName;
  private File temp;


  @TempDir
  Path tmpFolder;

  private SkosGeneratorPlugin init() {
    this.localName = "skosTest.rdf";
    this.temp = new File(tmpFolder.toFile(),"temp");

    SkosGeneratorPlugin mojo = new SkosGeneratorPlugin();

    mojo.setEntityOnly(false);
    mojo.setReason(false);

    mojo.setOwlFile("/cito.rdf");
    mojo.setTargetURI("http://purl.org/spar/cito/cites");

    mojo.setSkosNamespace("http://test.skos.foo");
    mojo.setOutputDirectory(temp);
    mojo.setSkosOutputFile(localName);

    return mojo;
  }

  @Test
  public void testUsingTempFolder() throws IOException {
    SkosGeneratorPlugin mojo = init();

    try {
      mojo.execute();
    } catch (MojoExecutionException e) {
      fail(e.getMessage());
    }

    OWLOntology onto = asOntology();
    OWLDataFactory odf = onto.getOWLOntologyManager().getOWLDataFactory();
    Set<OWLIndividual> inds = EntitySearcher
        .getIndividuals(odf.getOWLClass(SKOS.Concept.getURI()), onto)
        .collect(Collectors.toSet());

//    inds.stream()
//        .forEach((i) -> System.out.println(i.asOWLNamedIndividual().getIRI().getShortForm()));

    assertEquals(46, inds.size());
  }

  private OWLOntology asOntology() {
    File f = new File(temp.getAbsolutePath() + File.separator + localName);

    try {
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      return manager.loadOntologyFromOntologyDocument(f);
    } catch (OWLOntologyCreationException e) {
      fail(e.getMessage());
    }
    return null;
  }

}
