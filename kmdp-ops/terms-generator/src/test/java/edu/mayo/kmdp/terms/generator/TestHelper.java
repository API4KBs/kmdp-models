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

import edu.mayo.kmdp.util.URIUtil;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.SKOS;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import ru.avicomp.ontapi.OntManagers;
import ru.avicomp.ontapi.OntologyManager;

public class TestHelper {

  public static OntologyManager initManager() {
    OntologyManager manager = OntManagers.createONT();
    mappers().forEach(manager.getIRIMappers()::add);
    return manager;
  }


  public static OWLOntologyManager initOWLManager() {
    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    mappers().forEach(manager.getIRIMappers()::add);
    return manager;
  }

  private static List<OWLOntologyIRIMapper> mappers() {
    return Arrays.asList(
        (OWLOntologyIRIMapper) ontologyIRI ->
            ontologyIRI.equals(IRI.create(URIUtil.normalizeURI(URI.create(SKOS.getURI()))))
                ? IRI.create(TestHelper.class.getResource("/skos.rdf"))
                : null,
        (OWLOntologyIRIMapper) ontologyIRI ->
            ontologyIRI.equals(IRI.create(URIUtil.normalizeURI(URI.create(DCTerms.getURI()))))
                ? IRI.create(TestHelper.class.getResource("/dcterms.rdf"))
                : null
    );
  }

}
