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
package edu.mayo.kmdp.registry;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

public class Registry {

  private static final String path = "/ontologies/API4KP/informative/api4kp-registry.owl";

  public static final String MAYO_ASSETS_BASE_URI = "https://clinicalknowledgemanagement.mayo.edu/assets/";

  private static Model registry;
  private static Map<String, String> prefixToNamespaceMap = new HashMap<>();
  private static Map<String, String> languagSchemas = new HashMap<>();


  static {
    try {
      String xmlPrefixesQry = new Scanner(
          Registry.class.getResource("/xmlNSprefixes.sparql").openStream(),
          "UTF-8").useDelimiter("\\A").next();
      String xmlSchemasQry = new Scanner(
          Registry.class.getResource("/xmlSchemas.sparql").openStream(),
          "UTF-8").useDelimiter("\\A").next();
      registry = ModelFactory.createDefaultModel().read(Registry.class.getResourceAsStream(path),
          "http://edu.mayo.kmdp/registry");

      Query query = QueryFactory.create(xmlPrefixesQry);
      QueryExecution qexec = QueryExecutionFactory.create(query, registry);
      ResultSet rs = qexec.execSelect();
      rs.forEachRemaining((qs) -> {
        prefixToNamespaceMap.put(qs.get("?P").asLiteral().toString(),
            qs.get("?NS").asLiteral().toString());
      });

      query = QueryFactory.create(xmlSchemasQry);
      qexec = QueryExecutionFactory.create(query, registry);
      rs = qexec.execSelect();
      rs.forEachRemaining((qs) -> {
        languagSchemas.put(qs.get("?L").asResource().getURI(),
            qs.get("?NS").asLiteral().toString());
      });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  public static Optional<String> getNamespaceURIForPrefix(String pfx) {
    return Optional.ofNullable(prefixToNamespaceMap.get(pfx));
  }

  public static Optional<String> getPrefixforNamespace(String namespace) {
    if (namespace == null) {
      return Optional.empty();
    }
    return prefixToNamespaceMap.keySet()
        .stream()
        .filter(key -> namespace.equals(prefixToNamespaceMap.get(key)))
        .findFirst();
  }

  public static Optional<String> getPrefixforNamespace(URI namespace) {
    try {
      return getPrefixforNamespace(new URI(namespace.getScheme(),
          namespace.getAuthority(),
          namespace.getPath(),
          null,
          null).toString());
    } catch (URISyntaxException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  public static List<String> getCatalogs(URI lang) {
    switch (lang.toString()) {
      case "https://www.omg.org/spec/DMN/1.1":
      case "http://www.omg.org/spec/DMN/1.1":
        return Arrays.asList("dmn-catalog.xml");
      case "https://www.omg.org/spec/CMMN/1.1":
        return Arrays.asList("cmmn-catalog.xml");
      case "urn:hl7-org:knowledgeartifact:r1":
      case "http://hl7.org/KNART/1.3":
        return Arrays.asList("knart-catalog.xml");
      case "urn:hl7-org:elm:r1":
      case "http://hl7.org/ELM/1.2":
        return Arrays.asList("cql-catalog.xml");
      case "http://www.omg.org/spec/API4KP/1.0":
        return Arrays.asList("api4kp-catalog.xml");
      case "http://kmdp.mayo.edu/metadata/surrogate":
        return Arrays.asList("km-metadata-catalog.xml",
            "terms-catalog.xml",
            "api4kp-catalog.xml");
      default:
        throw new IllegalStateException();
    }
  }


  public static Optional<String> getValidationSchema(URI lang) {
    return Optional.ofNullable(languagSchemas.get(lang.toString()));
  }
}
