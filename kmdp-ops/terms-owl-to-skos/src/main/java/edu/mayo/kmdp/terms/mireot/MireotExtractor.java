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

import static edu.mayo.kmdp.terms.util.JenaUtil.detectOntologyIRI;
import static edu.mayo.kmdp.terms.util.JenaUtil.detectType;
import static edu.mayo.kmdp.terms.util.JenaUtil.detectVersionIRI;

import edu.mayo.kmdp.terms.mireot.MireotConfig.MireotParameters;
import edu.mayo.kmdp.util.JenaUtil;
import edu.mayo.kmdp.util.Util;
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class MireotExtractor {

  private static final Logger logger = LogManager.getLogger(MireotExtractor.class);

  private static final String MIREOT_PATH = "/query/mireot/mireot.sparql";
  private static final ParameterizedSparqlString MIREOT = new ParameterizedSparqlString(
      JenaUtil.read(MIREOT_PATH));


  public Optional<Model> fetch(InputStream in, URI targetUri, MireotConfig cfg) {
    String base = cfg.getTyped(MireotParameters.BASE_URI);

    Model source = ModelFactory.createDefaultModel().read(in, base);

    if (Util.isEmpty(base)) {
      base = detectOntologyIRI(source, base).orElse(base);
    }
    final URI baseUri = Util.isEmpty(base) ? null : URI.create(base);
    final URI versionUri = detectVersionIRI(source, base).map(URI::create).orElse(null);

    Optional<Model> result = (cfg.getTyped(MireotParameters.ENTITY_ONLY)
        ? fetchResource(source, targetUri, baseUri)
        : fetchResources(source, targetUri, baseUri, cfg)
    );

    return result.map(model -> asOntology(model, baseUri, versionUri));
  }


  Optional<Model> fetchResources(Model source, URI rootEntityUri, URI baseUri, MireotConfig cfg) {
    EntityTypes type = cfg.getTyped(MireotParameters.ENTITY_TYPE);

    if (type == EntityTypes.INST &&
        source.contains(source.createResource(rootEntityUri.toString()),
            RDF.type,
            OWL2.NamedIndividual)) {
      return fetchResource(source, rootEntityUri, baseUri);
    }

    return Optional.ofNullable(
        extract(source, rootEntityUri, baseUri, cfg).stream()
            .map(x -> fetchResource(source, URI.create(x.getURI()), baseUri))
            .flatMap(Util::trimStream)
            .reduce(ModelFactory.createDefaultModel(), Model::add));
  }

  Optional<Model> fetchResource(Model source, URI entityURI, URI baseUri) {
    ParameterizedSparqlString pss = MIREOT.copy();

    pss.setParam("?X", NodeFactory.createURI(entityURI.toString()));
    pss.setParam("?baseUri", NodeFactory.createURI(baseUri.toString()));

    return Optional.of(JenaUtil.construct(source, pss.asQuery()));
  }


  Set<Resource> extract(Model source, URI rootEntityUri, URI baseUri, MireotConfig cfg) {
    EntityTypes type = cfg.getTyped(MireotParameters.ENTITY_TYPE);
    if (type == null || type == EntityTypes.UNKNOWN) {
      type = detectType(rootEntityUri.toString(),source);
      if (type == null || type == EntityTypes.UNKNOWN) {
        logger.warn("WARNING: Cannot Determine Entity Type during MIREOT-ing, URI is likely incorrect");
        return new HashSet<>();
      }
    }
    Integer min = cfg.getTyped(MireotParameters.MIN_DEPTH);
    Integer max = cfg.getTyped(MireotParameters.MAX_DEPTH);

    ParameterizedSparqlString pss = new ParameterizedSparqlString(type.query, baseUri.toString());

    // count is 1-based, rather than 0-based
    pss.setParam("?focus", NodeFactory.createURI(rootEntityUri.toString()));
    pss.setLiteral("?n", min + 1);
    pss.setLiteral("?m", max < 0 ? Integer.MAX_VALUE : max + 1);

    return JenaUtil.askQuery(source, pss.asQuery());
  }


  private Model asOntology(Model model, URI baseUri, URI versionUri) {
    if (baseUri != null) {
      OntModel om = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, model);

      Ontology o = om.createOntology(baseUri.toString());

      if (versionUri != null) {
        o.addProperty(OWL2.versionIRI, ResourceFactory.createResource(versionUri.toString()));
      }
      return om;
    } else {
      return model;
    }
  }

}
