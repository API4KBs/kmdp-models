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
package edu.mayo.kmdp.util;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.apache.jena.rdf.model.ResourceFactory.createStatement;
import static org.apache.jena.rdf.model.ResourceFactory.createStringLiteral;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.PrintUtil;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public abstract class JenaUtil {
  
  private static Logger logger = LoggerFactory.getLogger(JenaUtil.class);

  static {
    FileManager.get().addLocatorClassLoader(JenaUtil.class.getClassLoader());
    FileManager.get().addLocatorClassLoader(Thread.currentThread().getContextClassLoader());
  }

  private JenaUtil() {

  }

  public static Integer sizeOf(Model schema) {
    return schema.listStatements().toSet().size();
  }


  public static <T> Set<Map<String, T>> askQuery(Model model, String queryStr,
      Function<RDFNode, T> mapper) {
    return askQuery(model, QueryFactory.create(queryStr),mapper);
  }

  public static <T> Set<Map<String, T>> askQuery(Model model, Query query,
      Function<RDFNode, T> mapper) {
    try (QueryExecution queryExec = QueryExecutionFactory.create(query, model)) {
      ResultSet results = queryExec.execSelect();
      Set<Map<String, T>> answers = new HashSet<>();
      while (results.hasNext()) {
        QuerySolution sol = results.next();
        answers.add(results.getResultVars().stream()
            .collect(Collectors.toMap(Function.identity(), k -> mapper.apply(sol.get(k)))));
      }
      return answers;
    }
  }

  public static Set<Map<String,String>> askQueryResults(Model model, Query query) {
    return askQuery(model,query,RDFNode::toString);
  }

  public static Model construct(Model model, Query query) {
    try (QueryExecution queryExec = QueryExecutionFactory.create(query, model)) {
      return queryExec.execConstruct();
    }
  }

  public static Set<Resource> askQuery(Model model, Query selectQuery) {
    try (QueryExecution queryExec = QueryExecutionFactory.create(selectQuery, model)) {
      org.apache.jena.query.ResultSet results = queryExec.execSelect();

      Set<Resource> answers = new HashSet<>();
      if (results.hasNext()) {
        results.forEachRemaining(sol -> {
          if (sol.varNames().hasNext()) {
            answers.add(sol.getResource(sol.varNames().next()));
          }
        });
      } else {
        logger.error("WARNING :: empty query ");
      }
      return answers;
    }
  }



  public static Set<Pair<RDFNode, RDFNode>> askBinaryQuery(Model model, Query selectQuery) {
    final Set<Pair<RDFNode, RDFNode>> total = new HashSet<>();
    try (QueryExecution queryExec = QueryExecutionFactory.create(selectQuery, model)) {
      org.apache.jena.query.ResultSet results = queryExec.execSelect();
      if (results.getResultVars().size() != 2) {
        throw new IllegalStateException(
            "Binary query expected, but found # of vars = " + results.getResultVars().size());
      }
      if (results.hasNext()) {
        results.forEachRemaining(sol -> {
          Iterator<String> vars = selectQuery.getResultVars().iterator();
          Pair<RDFNode, RDFNode> pair =
              new ImmutablePair<>(sol.get(vars.next()),sol.get(vars.next()));
          total.add(pair);
        });
      }
      return total;
    }
  }


  public static String read(String sourcePath) {
    if (sourcePath.isEmpty()) {
      return "";
    }
    File f = new File(sourcePath);
    if (f.exists()) {
      return FileManager.get().readWholeFileAsUTF8(sourcePath);
    } else {
      return FileManager.get().readWholeFileAsUTF8(JenaUtil.class.getResourceAsStream(sourcePath));
    }
  }

  public static Model loadModel(String modelRef) {
    URL url = JenaUtil.class.getResource(modelRef);
    try {
      return FileManager.get().loadModel(url.toURI().toString());
    } catch (URISyntaxException e) {
      logger.error(e.getMessage(),e);
    }
    return ModelFactory.createDefaultModel();
  }

  public static Model loadModel(InputStream modelRef, String base) {
    return ModelFactory.createDefaultModel().read(modelRef, base);
  }


  public static Model iterateAndStreamModel(Model target, PrintStream os,
      Function<Statement, String> formatter) {
    StmtIterator iter = target.listStatements();
    while (iter.hasNext()) {
      Statement stmt = iter.nextStatement();
      String subjNS = stmt.getSubject().getNameSpace();
      if (!(RDF.getURI().equals(subjNS)
      || RDFS.getURI().equals(subjNS)
      || OWL.getURI().equals(subjNS))) {
        os.println(formatter.apply(stmt));
      }
    }
    return target;
  }

  public static String asString(Model target) {
    // for testing purpose
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    iterateAndStreamModel(target, ps, PrintUtil::print);
    return new String(baos.toByteArray());
  }


  public static Optional<Model> fromJsonLD(String json) {
    try {
      Model m = ModelFactory.createDefaultModel();
      try (StringReader reader = new StringReader(json)) {
        m.read(reader, null, "JSON-LD");
      }
      return Optional.of(m);
    } catch (Exception e) {
      logger.error(e.getMessage(),e);
      return Optional.empty();
    }
  }

  public static Optional<Model> toTriples(Object obj) {
    Model m = ModelFactory.createDefaultModel();
    return JSonUtil.writeJson(obj, JSonLDUtil.initLDModule(), JSonUtil.defaultProperties())
        .flatMap(Util::asString)
        .map(StringReader::new)
        .map(json -> m.read(json, null, "JSON-LD"));
  }

  public static Statement objA(String subjURI, String propURI, String objURI) {
    return createStatement(createResource(subjURI),
        createProperty(propURI),
        createResource(objURI));
  }

  public static Statement objA(String subjURI, Property prop, Resource obj) {
    return createStatement(createResource(subjURI), prop, obj);
  }

  public static Statement objA(String subjURI, Property prop, String obj) {
    return createStatement(createResource(subjURI), prop, createResource(obj));
  }

  public static Statement datA(String subjURI, String propURI, String val) {
    return createStatement(createResource(subjURI),
        createProperty(propURI),
        createStringLiteral(val));
  }
  public static Statement datA(String subjURI, Property prop, String val) {
    return createStatement(createResource(subjURI),
        prop,
        createStringLiteral(val));
  }

  public static Statement objA(Resource subj, Property prop, String val) {
    return createStatement(subj,
        prop,
        createResource(val));
  }
  public static Statement objA(Resource subj, Property prop, Resource obj) {
    return createStatement(subj,
        prop,
        obj);
  }

  public static Statement datA(Resource subj, Property prop, String val) {
    return createStatement(subj,
        prop,
        createStringLiteral(val));
  }

  public static String fromStatementsToString(List<Statement> statementList) {
    Model model = ModelFactory.createDefaultModel();
    model.add(statementList);
    StringWriter stringWriter = new StringWriter();
    model.write(stringWriter, "TURTLE");
    model.close();
    return stringWriter.toString();
  }
}
