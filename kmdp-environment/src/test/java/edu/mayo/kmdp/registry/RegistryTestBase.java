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
package edu.mayo.kmdp.registry;

import static edu.mayo.kmdp.registry.Registry.REGISTRY_URI;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.apache.jena.vocabulary.XSD;
import org.apache.xerces.util.XMLCatalogResolver;

public class RegistryTestBase {

  static final String PREAMBLE = "" +
      "PREFIX rdf: <" + RDF.getURI() + "> \n" +
      "PREFIX owl: <" + OWL.getURI() + "> \n" +
      "PREFIX xsd: <" + XSD.getURI() + ">\n " +
      "PREFIX rdfs: <" + RDFS.getURI() + "> \n" +
      "PREFIX skos: <" + SKOS.getURI() + "> \n" +
      "PREFIX dct: <http://purl.org/dc/terms/> \n" +
      "PREFIX dc: <http://purl.org/dc/elements/1.1/> \n" +
      "PREFIX olex: <http://www.w3.org/ns/lemon/ontolex#> \n" +
      "PREFIX know: <https://www.omg.org/spec/API4KP/api4kp/> \n" +
      "PREFIX dol: <http://www.omg.org/spec/DOL/DOL-terms/> \n";


  static Model initRegistry(String catalogVersion) {
    XMLCatalogResolver xcat = new XMLCatalogResolver(
        new String[]{Registry.class.getResource(Registry.getCatalogVersion(catalogVersion)).toString()});
    try {
      String path = xcat.resolveURI(REGISTRY_URI);
      assertNotNull(path);
      path = path.replace("file:","");
      Model registry = ModelFactory.createOntologyModel()
          .read(RegistryTestBase.class.getResourceAsStream(path), null);
      assertNotNull(registry);
      return registry;
    } catch (IOException e) {
      fail(e.getMessage());
      return null;
    }
  }

}
