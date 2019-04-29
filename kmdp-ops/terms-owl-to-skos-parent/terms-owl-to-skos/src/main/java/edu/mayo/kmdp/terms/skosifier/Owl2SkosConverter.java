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
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.SKOS;


public class Owl2SkosConverter extends ConverterInitBase implements
    BiFunction<Model, Owl2SkosConfig, Optional<Model>> {

  public static final String SKOS_NAMESPACE = SKOS.getURI();
  public static final String OLEX = "http://www.w3.org/ns/lemon/ontolex#";

  private static Model schema;
  private static InfModel schemaInferred;

  @Override
  public Optional<Model> apply(Model source, Owl2SkosConfig cfg) {
    Modes mode = cfg.get(OWLtoSKOSTxParams.MODE).map(Modes::valueOf).orElse(Modes.SKOS);
    return postProcess(
        applyQueries(source, mode, cfg),
        detectVersionFragment(source).orElse(null),
        mode,
        cfg);
  }

  public Optional<Model> run(final List<String> sources, Owl2SkosConfig cfg) {
    Modes mode = cfg.get(OWLtoSKOSTxParams.MODE).map(Modes::valueOf).orElse(Modes.SKOS);
    Model result = sources.stream()
        .map(Owl2SkosConverter::createSourceModel)
        .map((s) -> applyQueries(s, mode, cfg))
        .reduce(ModelFactory.createDefaultModel(), Model::add);

    return postProcess(result, detectVersionFragment(result).orElse(null), mode, cfg);
  }

  protected Optional<Model> postProcess(Model model,
      String versionFragment,
      Modes mode,
      Owl2SkosConfig cfg) {

    Model result = null;
    if (cfg.getTyped(OWLtoSKOSTxParams.VALIDATE)) {
      InfModel inf = infer(model, getSchema());
      ValidityReport report = inf.validate();
      if (!report.isValid()) {
        debug(inf, report);
      }
      result = report.isValid() ? inf.getRawModel() : null;
    } else {
      result = model;
    }
    return Optional.ofNullable(toSKOSOntologyModel(result, versionFragment, mode, cfg));
  }


  private OntModel toSKOSOntologyModel(Model result, String versionFragment, Modes modes,
      Owl2SkosConfig cfg) {
    OntModel ontModel = ModelFactory.createOntologyModel();
    String baseUri = cfg.getTyped(OWLtoSKOSTxParams.TGT_NAMESPACE);

    Ontology ont = ontModel.createOntology(baseUri);
    if (versionFragment != null && !versionFragment.isEmpty()) {
      ont.addProperty(OWL2.versionIRI,
          ResourceFactory.createResource(applyVersionToURI(baseUri, versionFragment)));
    }

    if (modes.usesOlex) {
      ont.addImport(ontModel.createResource(OLEX));
    }
    ont.addImport(ontModel.createResource(removeLastChar(SKOS_NAMESPACE)));

    if (cfg.getTyped(OWLtoSKOSTxParams.FLATTEN)) {
      prefetch(ontModel, modes.usesOlex);
    } else if (cfg.getTyped(OWLtoSKOSTxParams.ADD_IMPORTS)) {
      ontModel.loadImports();
    }

    ontModel.add(result);

    new HierarchySealer().close(ontModel);

    return ontModel;
  }


  private static void prefetch(OntModel ontModel, boolean olex) {
    prefetchFromLocal(ontModel, SKOS_NAMESPACE, "/ontology/skos.rdf");
    if (olex) {
      prefetchFromLocal(ontModel, OLEX, "/ontology/ontolex.owl");
    }
  }

  private static void prefetchFromLocal(OntModel ontModel, String namespace, String path) {
    // try to resolve the local copy, to avoid fetching from the web in case the build is offline
    InputStream is = Owl2SkosConverter.class.getResourceAsStream(path);
    try {
      if (is != null && is.available() > 0) {
        OntModel extraModel = ModelFactory.createOntologyModel();
        extraModel.read(is, namespace);
        ontModel.add(extraModel);
      }
    } catch (IOException e) {
      // do nothing
    }
  }


  private void debug(InfModel inf, ValidityReport report) {
    report.getReports().forEachRemaining((rep) -> {
      if (rep.isError()) {
        System.err.println(rep.toString());
      }
    });
  }

  private InfModel infer(Model result, Model schema) {
    Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
    reasoner = reasoner.bindSchema(schema);
    return ModelFactory.createInfModel(reasoner, result);
  }


  private static Model getSchema() {
    return getSchema(false);
  }

  private static Model getSchema(boolean withOlex) {
    if (schema == null) {
      schema = ModelFactory.createOntologyModel();

      prefetch((OntModel) schema, withOlex);
    }
    return schema;
  }


  private static InfModel getSchemaInferred() {
    if (schemaInferred == null) {
      schemaInferred = ModelFactory.createInfModel(ReasonerRegistry.getOWLReasoner(), getSchema());
    }
    return schemaInferred;
  }

  private static Model createSourceModel(String ref) {
    Model sourceOntology = ModelFactory.createDefaultModel();
    sourceOntology.add(JenaUtil.loadModel(ref));
    return sourceOntology;
  }

  private Model loadSourceModel(InputStream ref, String base) {
    Model sourceOntology = ModelFactory.createDefaultModel();
    sourceOntology.add(JenaUtil.loadModel(ref, base));
    return sourceOntology;
  }

  private Model applyQueries(final Model ontModel, final Modes modes, final Owl2SkosConfig cfg) {
    return getQueriesForModes(modes, cfg).stream()
        .map((q) -> QueryExecutionFactory.create(q, ontModel).execConstruct())
        .reduce(Model::add)
        .orElse(ModelFactory.createDefaultModel());
  }

}
