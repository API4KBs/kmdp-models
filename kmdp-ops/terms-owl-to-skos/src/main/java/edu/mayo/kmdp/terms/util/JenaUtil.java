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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
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

  /**
   * Copies the 'ontology' axioms from an OntModel to a regular Model
   * @param root
   * @param ontologyModel
   */
  public static void addOntologyAxioms(Model root, OntModel ontologyModel) {
    Ontology onto = ontologyModel.listOntologies().next();
    List<Statement> ontos =
        ontologyModel.listStatements()
            .filterKeep(st -> st.getSubject().getURI() != null && st.getSubject().getURI().equals(onto.getURI())).toList();
    root.add(ontos);
  }

  /**
   * Copies the 'ontology' axioms from an OntModel to a regular Model
   * @param root
   * @param ontologyModel
   */
  public static void addOntologyAxioms(Model root, Model ontologyModel) {
    List<Resource> ontologies = ontologyModel.listStatements()
        .filterKeep(st -> st.getPredicate().equals(RDF.type) && st.getObject().equals(OWL2.Ontology))
        .mapWith(Statement::getSubject)
        .toList();

    List<Statement> ontos =
        ontologyModel.listStatements()
            .filterKeep(st -> st.getSubject().getURI() != null && ontologies.contains(st.getSubject())).toList();
    root.add(ontos);
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
        .filterDrop(s -> source.contains(null, OWL2.imports, s.getSubject()))
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


  public static String applyVersionToURI(String baseURI, String versionFragment, int index, String sep) {
    URI uri = URI.create(baseURI);
    boolean hasPath = uri.getPath() != null;

    String core = hasPath ? uri.getPath() : uri.getSchemeSpecificPart();
    List<String> hops = new LinkedList<>(Arrays.asList(core.split(sep)));

    boolean needsInitialPadding = hops.isEmpty();
    boolean needFinalPadding = core.endsWith(sep);
    boolean reverse = index < 0;

    if (needsInitialPadding) {
      // ensure the host and path will be separated
      hops.add("");
    }

    if (reverse) {
      // reverse the index
      index = hops.size() - 1 + index;
    }
    // prevent under/overflow
    index = Math.max(1, Math.min(index + 1, hops.size()));

    if (needFinalPadding) {
      // ensure a trailing separator will be added back at the end
      hops.add("");
      if (reverse) {
        index++;
      }
    }

    String effectiveVersionFragment = versionFragment;
    if (versionFragment.startsWith(sep)) {
      effectiveVersionFragment = effectiveVersionFragment.substring(sep.length());
    }
    if (versionFragment.endsWith(sep)) {
      effectiveVersionFragment = effectiveVersionFragment.substring(0,effectiveVersionFragment.lastIndexOf(sep));
    }
    // insert the version fragment in the path
    hops.add(index, effectiveVersionFragment);

    try {
      URI versionUri = hasPath
          ? new URI(uri.getScheme(), uri.getHost(), String.join(sep, hops), uri.getFragment())
          : new URI(uri.getScheme(), String.join(sep, hops), uri.getFragment());
      return versionUri.toString();
    } catch (URISyntaxException e) {
      e.printStackTrace();
      return null;
    }
  }


  public static String applyVersionToURI(String baseURI, String versionFragment) {
    return applyVersionToURI(baseURI, versionFragment, Integer.MAX_VALUE - 1);
  }

  public static String applyVersionToURI(String baseURI, String versionFragment, int pos) {
    String sep = baseURI.startsWith("urn") ? ":" : "/";
    return applyVersionToURI(baseURI, versionFragment, pos, sep);
  }

}
