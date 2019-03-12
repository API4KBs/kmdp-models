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

import edu.mayo.kmdp.util.JenaUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;

import java.io.InputStream;
import java.util.Optional;
import java.util.Set;

import static edu.mayo.kmdp.terms.util.JenaUtil.detectOntologyIRI;
import static edu.mayo.kmdp.terms.util.JenaUtil.detectType;
import static edu.mayo.kmdp.terms.util.JenaUtil.detectVersionIRI;


public class MireotExtractor {

  private String baseURI;
  private String versionURI;
  private Model source;

  public static Optional<Model> extract(InputStream source, String entityURI, boolean entityOnly) {
    return extract(source, entityURI, entityOnly, 0, -1);
  }

  public static Optional<Model> extract(InputStream source, String entityURI, boolean entityOnly,
      int min, int max) {
    return new MireotExtractor(source).fetch(entityURI, entityOnly, min, max);
  }

  public static Optional<Model> extract(InputStream source, String entityURI) {
    return extract(source, entityURI, 0, -1);
  }

  public static Optional<Model> extract(InputStream source, String entityURI, int min, int max) {
    return new MireotExtractor(source).fetch(entityURI, false, min, max);
  }

  public static Optional<Model> extract(InputStream source, String entityURI, EntityTypes type,
      int min, int max) {
    return new MireotExtractor(source).fetch(entityURI, type, false, min, max);
  }

  private final static String mireotPath = "/query/mireot/mireot.sparql";
  private final static ParameterizedSparqlString mireot = new ParameterizedSparqlString(
      JenaUtil.read(mireotPath));


  public MireotExtractor(InputStream in) {
    this(in, "");
  }

  public MireotExtractor(InputStream in, String baseURI) {
    source = ModelFactory.createDefaultModel().read(in, baseURI);
    this.baseURI = detectOntologyIRI(source, baseURI).orElse(null);
    if (this.baseURI != null) {
      this.versionURI = detectVersionIRI(source, this.baseURI).orElse(null);
    }
  }

  public Optional<Model> fetchResources(String rootEntityURI, EntityTypes type) {
    return fetchResources(rootEntityURI, type, 0, -1);
  }

  public Optional<Model> fetchResources(String rootEntityURI, EntityTypes type, int minDepth,
      int maxDepth) {

    if (type == EntityTypes.INST &&
        source.contains(source.createResource(rootEntityURI),
            RDF.type,
            OWL2.NamedIndividual)) {
      return fetchResource(rootEntityURI);
    }

    return Optional.ofNullable(extract(rootEntityURI, type, minDepth, maxDepth).stream()
        .map(this::fetchResource)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .reduce(ModelFactory.createDefaultModel(),
            Model::add));
  }

  public Optional<Model> fetch(String targetURI, boolean entityOnly) {
    return fetch(targetURI, entityOnly, 0, -1);
  }

  public Optional<Model> fetch(String targetURI, boolean entityOnly, int minDepth, int maxDepth) {
    return fetch(targetURI, null, entityOnly, minDepth, maxDepth);
  }

  public Optional<Model> fetch(String targetURI, EntityTypes kind, boolean entityOnly, int minDepth,
      int maxDepth) {
    return (entityOnly ? fetchResource(targetURI) : fetchResources(
        targetURI,
        kind == null || kind == EntityTypes.UNKNOWN ? detectType(targetURI, source) : kind,
        minDepth,
        maxDepth)
    ).map(this::asOntology);
  }

  private Model asOntology(Model model) {
    if (this.baseURI != null) {
      OntModel om = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, model);

      Ontology o = om.createOntology(this.baseURI);

      if (this.versionURI != null) {
        o.addProperty(OWL2.versionIRI, ResourceFactory.createResource(this.versionURI));
      }
      return om;
    } else {
      return model;
    }
  }


  public Optional<Model> fetchResource(Resource res) {
    return fetchResource(res.asNode());
  }

  public Optional<Model> fetchResource(String entityURI) {
    return fetchResource(NodeFactory.createURI(entityURI));
  }

  public Optional<Model> fetchResource(Node entity) {
    ParameterizedSparqlString pss = mireot.copy();

    pss.setParam("?X", entity);
    pss.setParam("?baseUri", NodeFactory.createURI(baseURI));

//		System.out.println( pss.toString() );

    return Optional.of(JenaUtil.construct(source, pss.asQuery()));
  }


  public Set<Resource> extract(String rootUri, EntityTypes type) {
    return extract(rootUri, type, 0, -1);
  }

  public Set<Resource> extract(String rootUri, EntityTypes type, int min, int max) {
    ParameterizedSparqlString pss = new ParameterizedSparqlString(type.query, baseURI);

    // count is 1-based, rather than 0-based
    pss.setParam("?focus", NodeFactory.createURI(rootUri));
    pss.setLiteral("?n", min + 1);
    pss.setLiteral("?m", max < 0 ? Integer.MAX_VALUE : max + 1);

    return JenaUtil.askQuery(source, pss.asQuery());
  }

  public String getBaseURI() {
    return baseURI;
  }

}
