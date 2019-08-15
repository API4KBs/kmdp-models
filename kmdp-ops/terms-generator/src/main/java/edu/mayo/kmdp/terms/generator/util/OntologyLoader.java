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
package edu.mayo.kmdp.terms.generator.util;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class OntologyLoader {


  public OWLOntology loadOntology(String[] resources) throws OWLOntologyCreationException {
    return loadOntology(resources, null);
  }

  public OWLOntology loadOntology(String[] resources, OWLOntologyIRIMapper catalog, IRI... ignoredImports)
      throws OWLOntologyCreationException {
    OWLOntology ontology = null;
    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

    if (catalog != null) {
      manager.getIRIMappers().add(catalog);
    }

    OWLOntologyLoaderConfiguration cfg = manager.getOntologyLoaderConfiguration().clearIgnoredImports();
    for (IRI iri: ignoredImports) {
      // The add clones the configuration, need to reassign
      cfg = cfg.addIgnoredImport(iri);
    }
    manager.setOntologyLoaderConfiguration(cfg);


    for (String res : resources) {
      ontology = loadOntologyPiece(res, manager);
    }

    return ontology;
  }

  private OWLOntology loadOntologyPiece(String file, OWLOntologyManager manager)
      throws OWLOntologyCreationException {
    InputStream inputStream = null;
    OWLOntology onto;

    File res = new File(file);

    try {
      if (!res.exists()) {
        inputStream = OntologyLoader.class.getResourceAsStream(file);
      } else {
        inputStream = new FileInputStream(res);
      }
      onto = manager.loadOntologyFromOntologyDocument(inputStream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    return onto;
  }

}
