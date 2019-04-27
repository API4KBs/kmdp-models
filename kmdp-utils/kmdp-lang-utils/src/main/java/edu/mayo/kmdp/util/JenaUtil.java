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

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.PrintUtil;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public abstract class JenaUtil {

  static {
    FileManager.get().addLocatorClassLoader(JenaUtil.class.getClassLoader());
    FileManager.get().addLocatorClassLoader(Thread.currentThread().getContextClassLoader());
  }

  private JenaUtil() {

  }

  public static Integer sizeOf(Model schema) {
    return schema.listStatements().toSet().size();
  }


  public static <T> Set<Map<String, T>> askQuery(Model model, String query,
      Function<RDFNode, T> mapper) {
    ResultSet results = QueryExecutionFactory.create(query,
        model).execSelect();
    Set<Map<String, T>> answers = new HashSet<>();
    while (results.hasNext()) {
      QuerySolution sol = results.next();
      answers.add(results.getResultVars().stream()
          .collect(Collectors.toMap(Function.identity(), (k) -> mapper.apply(sol.get(k)))));
    }
    return answers;
  }

  public static Model construct(Model model, Query query) {
    return QueryExecutionFactory.create(query,
        model).execConstruct();
  }

  public static Set<Resource> askQuery(Model model, Query selectQuery) {
    org.apache.jena.query.ResultSet results = QueryExecutionFactory.create(selectQuery,
        model).execSelect();

    Set<Resource> answers = new HashSet<>();
    if (results.hasNext()) {
      results.forEachRemaining((sol) -> {
        if (sol.varNames().hasNext()) {
          answers.add(sol.getResource(sol.varNames().next()));
        }
      });
    } else {
      System.err.println("WARNING :: empty query ");
    }
    return answers;
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
      e.printStackTrace();
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


  public static Model toSystemOut(Model target) {
    // for testing purpose
    return iterateAndStreamModel(target, System.out, PrintUtil::print);
  }


  public static Optional<Model> fromJsonLD(String json) {
    try {
      Model m = ModelFactory.createDefaultModel();
      try (StringReader reader = new StringReader(json)) {
        m.read(reader, null, "JSON-LD");
      }
      return Optional.of(m);
    } catch (Exception e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  public static Optional<Model> toTriples(Object obj) {
    Model m = ModelFactory.createDefaultModel();
    return JSonUtil.writeJson(obj, JSonLDUtil.initLDModule(), JSonUtil.defaultProperties())
        .flatMap(Util::asString)
        .map(StringReader::new)
        .map((json) -> m.read(json, null, "JSON-LD"));
  }

  public static Statement obj_a(String subjURI, String propURI, String objURI) {
    return new StatementImpl(new ResourceImpl(subjURI),
        new PropertyImpl(propURI),
        new ResourceImpl(objURI));
  }

  public static Statement obj_a(String subjURI, Property prop, Resource obj) {
    return new StatementImpl(new ResourceImpl(subjURI), prop, obj);
  }

  public static Statement dat_a(String subjURI, String propURI, String val) {
    return new StatementImpl(new ResourceImpl(subjURI),
        new PropertyImpl(propURI),
        ResourceFactory.createStringLiteral(val));
  }
  public static Statement dat_a(String subjURI, Property prop, String val) {
    return new StatementImpl(new ResourceImpl(subjURI),
        prop,
        ResourceFactory.createStringLiteral(val));
  }
}