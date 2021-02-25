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
package edu.mayo.kmdp.terms.skosifier;

import static edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig.OWLtoSKOSTxParams.VERSION_POS;
import static edu.mayo.kmdp.terms.util.JenaUtil.applyVersionToURI;
import static edu.mayo.kmdp.terms.util.JenaUtil.detectVersionFragment;
import static edu.mayo.kmdp.util.Util.removeLastChar;

import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig.OWLtoSKOSTxParams;
import edu.mayo.kmdp.util.JenaUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.SKOS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Owl2SkosConverter extends ConverterInitBase implements
    BiFunction<Model, Owl2SkosConfig, Optional<Model>> {

  private static final String SKOS_NAMESPACE = SKOS.getURI();
  private static final String DC_NAMESPACE = DCTerms.getURI();
  private static final String OLEX = "http://www.w3.org/ns/lemon/ontolex#";

  private static Logger logger = LoggerFactory.getLogger(Owl2SkosConverter.class);

  @Override
  public Optional<Model> apply(Model source, Owl2SkosConfig cfg) {
    Modes mode = cfg.get(OWLtoSKOSTxParams.MODE)
        .map(Modes::valueOf)
        .orElse(Modes.SKOS);
    String versionFragment = detectVersionFragment(source).orElse(null);

    return postProcess(
        applyQueries(source, mode, cfg),
        versionFragment,
        mode,
        cfg);
  }

  public Optional<Model> run(final List<String> sources, Owl2SkosConfig cfg) {
    Modes mode = cfg.get(OWLtoSKOSTxParams.MODE)
        .map(Modes::valueOf)
        .orElse(Modes.SKOS);

    Model result = initModel(sources,mode, cfg);
    String versionFragment = detectVersionFragment(result).orElse(null);

    return postProcess(
        result,
        versionFragment,
        mode,
        cfg);
  }

  private Optional<Model> postProcess(Model result, String versionFragment, Modes mode,
      Owl2SkosConfig cfg) {
    result = validate(result, cfg);
    OntModel ontModel = createOntModel(versionFragment,mode,cfg);
    ontModel.add(result);

    new HierarchySealer().close(ontModel);
    return Optional.of(ontModel);
  }

  private Model validate(Model model, Owl2SkosConfig cfg) {
    Model result;
    if (cfg.getTyped(OWLtoSKOSTxParams.VALIDATE)) {
      InfModel inf = infer(model, ModelFactory.createDefaultModel());
      ValidityReport report = inf.validate();
      if (!report.isValid()) {
        debug(report);
      }
      result = report.isValid() ? inf.getRawModel() : null;
    } else {
      result = model;
    }
    return result;
  }

  private Model initModel(List<String> sources, Modes mode, Owl2SkosConfig cfg) {
    return sources.stream()
        .map(Owl2SkosConverter::createSourceModel)
        .map(s -> applyQueries(s, mode, cfg))
        .reduce(ModelFactory.createDefaultModel(), Model::add);
  }


  private OntModel createOntModel(String versionFragment, Modes modes,
      Owl2SkosConfig cfg) {
    OntModel ontModel = ModelFactory.createOntologyModel();
    Ontology ont = createOntology(
        ontModel,
        versionFragment,
        cfg.getTyped(OWLtoSKOSTxParams.TGT_NAMESPACE),
        cfg);
    loadImports(ont, ontModel, modes, cfg);
    return ontModel;
  }

  private Ontology createOntology(OntModel ontModel, String versionFragment, String baseUri,
      Owl2SkosConfig cfg) {
    Ontology ont = ontModel.createOntology(baseUri);
    if (versionFragment != null && !versionFragment.isEmpty()) {
      ont.addProperty(OWL2.versionIRI,
          ResourceFactory.createResource(applyVersionToURI(baseUri, versionFragment, cfg.getTyped(VERSION_POS))));
    }
    return ont;
  }

  private void loadImports(Ontology ont, OntModel ontModel, Modes modes, Owl2SkosConfig cfg) {
    if (cfg.getTyped(OWLtoSKOSTxParams.ADD_IMPORTS)) {

      ont.addImport(ontModel.createResource(removeLastChar(SKOS_NAMESPACE)));
      prefetchFromLocal(ontModel, SKOS_NAMESPACE, "/ontology/skos.rdf");

      if (modes.usesOlex) {
        ont.addImport(ontModel.createResource(OLEX));
        prefetchFromLocal(ontModel, OLEX, "/ontology/ontolex.owl");
      }
      if (modes.usedDC) {
        ont.addImport(ontModel.createResource(DC_NAMESPACE));
        prefetchFromLocal(ontModel, DC_NAMESPACE, "/ontology/dcterms.rdf");
      }

    } else {
      ont.addImport(ontModel.createResource(removeLastChar(SKOS_NAMESPACE)));
    }
  }


  private static void prefetchFromLocal(OntModel ontModel, String namespace, String path) {
    // try to resolve the local copy, to avoid fetching from the web in case the build is offline
    InputStream is = Owl2SkosConverter.class.getResourceAsStream(path);
    try {
      if (is != null && is.available() > 0) {
        Model imported = ModelFactory.createOntologyModel().read(is, namespace);
        ontModel.addSubModel(imported);
      }
    } catch (IOException e) {
      // do nothing
    }
  }


  private void debug(ValidityReport report) {
    report.getReports().forEachRemaining(rep -> {
      if (rep.isError()) {
        logger.error(rep.toString());
      }
    });
  }

  private InfModel infer(Model result, Model schema) {
    Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
    reasoner = reasoner.bindSchema(schema);
    return ModelFactory.createInfModel(reasoner, result);
  }


  private static Model createSourceModel(String ref) {
    Model sourceOntology = ModelFactory.createDefaultModel();
    sourceOntology.add(JenaUtil.loadModel(ref));
    return sourceOntology;
  }

  private Model applyQueries(final Model ontModel, final Modes modes, final Owl2SkosConfig cfg) {
    return getQueriesForModes(modes, cfg).stream()
        .map(q -> QueryExecutionFactory.create(q, ontModel).execConstruct())
        .reduce(Model::add)
        .orElse(ModelFactory.createDefaultModel());
  }

}
