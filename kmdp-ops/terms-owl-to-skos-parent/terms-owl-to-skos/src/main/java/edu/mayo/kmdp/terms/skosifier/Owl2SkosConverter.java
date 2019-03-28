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

import edu.mayo.kmdp.util.JenaUtil;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static edu.mayo.kmdp.terms.util.JenaUtil.applyVersionToURI;
import static edu.mayo.kmdp.terms.util.JenaUtil.detectVersionFragment;
import static edu.mayo.kmdp.util.Util.removeLastChar;


public class Owl2SkosConverter extends ConverterInitBase {

  public static final String SKOS_NAMESPACE = "http://www.w3.org/2004/02/skos/core#";
  public static final String OLEX = "http://www.w3.org/ns/lemon/ontolex#";

  private static Model schema;
  private static InfModel schemaInferred;
  private final boolean olex;


  public static Optional<Model> convert(Model source, String targetNamespace, Modes mode,
      boolean verify, boolean flatten) {
    return new Owl2SkosConverter(targetNamespace, mode).run(source, verify, flatten);
  }

  public static Optional<Model> convert(Model source, String targetNamespace, boolean verify,
      boolean flatten) {
    return convert(source, targetNamespace, Modes.SKOS, verify, flatten);
  }

  public static Optional<Model> convert(Model source, String targetNamespace) {
    return convert(source, targetNamespace, false, false);
  }

  public static Optional<Model> convert(InputStream source, String targetNamespace) {
    return convert(JenaUtil.loadModel(source, targetNamespace), targetNamespace, false, false);
  }


  public Owl2SkosConverter(String targetNamespace, Modes... modes) {
    this.baseURI = targetNamespace;

    if (modes == null || modes.length == 0) {
      modes = new Modes[]{Modes.SKOS};
    }
    this.arQueries = getQueriesForModes(modes);
    this.olex = Arrays.stream(modes).anyMatch((m) -> m.olex);
  }


  public Optional<Model> run(final Model source, boolean validate, boolean flatten) {
    return postProcess(applyQueries(source),
        detectVersionFragment(source).orElse(null),
        validate,
        flatten);
  }


  public Optional<Model> run(final List<String> sources,
      boolean validate,
      boolean flatten) {
    Model result = sources.stream()
        .map(Owl2SkosConverter::createSourceModel)
        .map(this::applyQueries)
        .reduce(ModelFactory.createDefaultModel(), Model::add);

    return postProcess(result, detectVersionFragment(result).orElse(null), validate, flatten);
  }

  protected Optional<Model> postProcess(Model model,
      String versionFragment,
      boolean validate,
      boolean flatten) {

    Model result = null;
    if (validate) {
      InfModel inf = infer(model, getSchema());
      ValidityReport report = inf.validate();
      if (!report.isValid()) {
        debug(inf, report);
      }
      result = report.isValid() ? inf.getRawModel() : null;
    } else {
      result = model;
    }
    return Optional.ofNullable(toSKOSOntologyModel(result, versionFragment, flatten));
  }

  private OntModel toSKOSOntologyModel(Model result, String versionFragment, boolean flatten) {
    OntModel ontModel = ModelFactory.createOntologyModel();

    Ontology ont = ontModel.createOntology(this.baseURI);
    if (versionFragment != null && !versionFragment.isEmpty()) {
      ont.addProperty(OWL2.versionIRI,
          ResourceFactory.createResource(applyVersionToURI(baseURI, versionFragment)));
    }

    if (this.olex) {
      ont.addImport(ontModel.createResource(OLEX));
    }
    ont.addImport(ontModel.createResource(removeLastChar(SKOS_NAMESPACE)));

    if (flatten) {
      prefetch(ontModel, this.olex);
    } else {
      ontModel.loadImports();
    }

    ontModel.add(result);
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
        //System.out.println(rep.toString());
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

  private Model applyQueries(final Model ontModel) {
    return arQueries.stream()
        .map((q) -> QueryExecutionFactory.create(q, ontModel).execConstruct())
        .reduce(Model::add)
        .orElse(ModelFactory.createDefaultModel());
  }

}
