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
package edu.mayo.kmdp.terms.util;

import static edu.mayo.kmdp.util.NameUtils.strip;

import edu.mayo.kmdp.terms.mireot.EntityTypes;
import java.util.Optional;
import java.util.Set;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;

public class JenaUtil {

  static {
    FileManager.get().addLocatorClassLoader(edu.mayo.kmdp.util.JenaUtil.class.getClassLoader());
    FileManager.get().addLocatorClassLoader(Thread.currentThread().getContextClassLoader());
  }

  private JenaUtil() { }

  public static EntityTypes detectType(String targetURI, Model source) {
    if (source.contains(source.createResource(targetURI),
        RDF.type,
        OWL2.NamedIndividual)) {
      return EntityTypes.INST;
    } else if (source.contains(source.createResource(targetURI),
        RDF.type,
        OWL2.Class)) {
      return EntityTypes.CLASS;
    } else if (source.contains(source.createResource(targetURI),
        RDF.type,
        OWL2.ObjectProperty)) {
      return EntityTypes.OBJ_PROP;
    } else if (source.contains(source.createResource(targetURI),
        RDF.type,
        OWL2.DatatypeProperty)) {
      return EntityTypes.DATA_PROP;
    } else {
      return EntityTypes.UNKNOWN;
    }
  }

  public static Optional<String> detectVersionIRI(Model source, String ontologyURI) {
    Set<String> versions = source
        .listStatements(ResourceFactory.createResource(ontologyURI), OWL2.versionIRI,
            (RDFNode) null)
        .mapWith(s -> s.getObject().toString())
        .toSet();
    if (versions.size() > 1) {
      throw new UnsupportedOperationException(
          "Found multiple version IRIs for ontology " + ontologyURI + " :: " + versions);
    }
    return versions.isEmpty() ? Optional.empty() : Optional.ofNullable(versions.iterator().next());
  }


  public static Optional<String> detectVersionFragment(Model source) {
    Optional<String> ontologyUri = detectOntologyIRI(source);
    if (ontologyUri.isPresent()) {
      String uri = ontologyUri.get();
      Optional<String> versionUri = detectVersionIRI(source, uri);
      if (versionUri.isPresent()) {
        String vuri = versionUri.get();
        return Optional.of(strip(uri, vuri));
      }
    }
    return Optional.empty();
  }


  public static Optional<String> detectOntologyIRI(Model source) {
    return detectOntologyIRI(source, null);
  }

  public static Optional<String> detectOntologyIRI(Model source, String baseURIHint) {
    String baseURI;
    Set<String> uris = source.listStatements(null, RDF.type, OWL.Ontology)
        .mapWith(s -> s.getSubject().toString())
        .toSet();

    if (uris.size() > 1) {
      throw new UnsupportedOperationException("Unable to handle multiple ontologies :: " + uris);
    }

    if (uris.isEmpty() && baseURIHint != null) {
      baseURI = source.getNsPrefixMap().getOrDefault("", baseURIHint);
    } else {
      baseURI = uris.isEmpty() ? null : uris.iterator().next();
    }
    return Optional.ofNullable(baseURI);
  }


  public static String applyVersionToURI(String baseURI, String versionFragment) {
    StringBuilder sb = new StringBuilder();
    sb.append(baseURI.endsWith("#")
        ? baseURI.substring(0, baseURI.length() - 1)
        : baseURI);
    if (!(sb.toString().endsWith("/") || versionFragment.startsWith("/"))) {
      sb.append("/");
    }
    if (sb.toString().endsWith("/") && versionFragment.startsWith("/")) {
      sb.append(versionFragment.substring(1));
    } else {
      sb.append(versionFragment);
    }
    return sb.toString();
  }

}
