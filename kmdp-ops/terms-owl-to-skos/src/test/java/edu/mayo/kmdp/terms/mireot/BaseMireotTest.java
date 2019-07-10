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
package edu.mayo.kmdp.terms.mireot;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;

public abstract class BaseMireotTest {

  InputStream stream(String sourcePath) {
    InputStream s = SelectResourcesTest.class.getResourceAsStream(sourcePath);
    if (s == null) {
      fail("Unable to load " + sourcePath);
    }
    return s;
  }

  Resource r(String baseUri, String localName) {
    return ResourceFactory.createResource(fix(baseUri, "/") + localName);
  }

  Resource h(String baseUri, String localName) {
    return ResourceFactory.createResource(fix(baseUri, "#") + localName);
  }

  String fix(String baseUri, String delim) {
    return baseUri.endsWith(delim) ? baseUri : baseUri + delim;
  }

  public Set<Resource> getResources(String baseUri, Model model) {
    Set<Resource> resources = new HashSet<>();
    ResIterator iter = model.listSubjects();
    while (iter.hasNext()) {
      Resource res = iter.nextResource();
      if (! res.toString().equals(baseUri)) {
        // exclude the ontology itself
        resources.add(res);
      }
    }
    return resources;
  }

  public Set<Resource> getIndividuals(String baseUri, Model model) {
    Set<Resource> resources = new HashSet<>();
    ResIterator iter = model.listSubjectsWithProperty(RDF.type, OWL2.NamedIndividual);
    while (iter.hasNext()) {
      Resource res = iter.nextResource();
        resources.add(res);
    }
    return resources;
  }
}
