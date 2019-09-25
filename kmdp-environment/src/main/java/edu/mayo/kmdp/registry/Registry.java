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

import static edu.mayo.kmdp.registry.RegistryUtil.findLatestLexicographically;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.xerces.util.XMLCatalogResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Registry {

  public static final String PATH = "/ontologies/API4KP/informative/api4kp-registry.rdf";

  public static final String MAYO_ASSETS_BASE_URI = "https://clinicalknowledgemanagement.mayo.edu/assets/";
  public static final String BASE_UUID_URN = "urn:uuid:";

  private static XMLCatalogResolver xcat;

  private static Model registryGraph;

  private static Logger logger = LoggerFactory.getLogger(Registry.class);

  private static Map<String, String> prefixToNamespaceMap = new HashMap<>();
  private static Map<String, String> namespaceToPrefixMap = new HashMap<>();
  private static Map<String, String> languagSchemas = new HashMap<>();

  protected Registry() {

  }

  static {
    xcat = new XMLCatalogResolver(new String[] {Registry.class.getResource("/meta-catalog.xml").toString()});

    String xmlPrefixesQry = RegistryUtil.read("/xmlNSprefixes.sparql");
    String xmlSchemasQry = RegistryUtil.read("/xmlSchemas.sparql");

    registryGraph = ModelFactory.createOntologyModel()
        .read(Registry.class.getResourceAsStream(PATH),null);
    registryGraph = ModelFactory.createInfModel(ReasonerRegistry.getOWLMicroReasoner(),registryGraph);

    populatePrefixMap(RegistryUtil.askQuery(xmlPrefixesQry, registryGraph));

    RegistryUtil.askQuery(xmlSchemasQry, registryGraph).forEach(
        m-> languagSchemas.put(m.get("L"), m.get("NS"))
    );

 }

  private static void populatePrefixMap(List<Map<String, String>> askQuery) {
    askQuery.forEach(
        m -> prefixToNamespaceMap.put(m.get("P"), m.get("NS"))
    );
    askQuery.forEach(
        m -> namespaceToPrefixMap.put(m.get("NS"), m.get("Code"))
    );

    Map<String, Set<String>> versions = new HashMap<>();
    askQuery.forEach(l -> {
      String code = l.get("Code");
      String ver = l.get("Ver");
      if (ver != null && !ver.isEmpty()) {
        Set<String> verSet = versions.computeIfAbsent(code, c -> new HashSet<>());
        verSet.add(ver);
      }
    });
    versions.forEach((key,value) -> {
       if (!key.isEmpty() && !value.isEmpty() && ! prefixToNamespaceMap.containsKey(key)) {
         String lastKey = key + "-" + findLatestLexicographically(value);
         prefixToNamespaceMap.put(key, prefixToNamespaceMap.get(lastKey));
       }
    });
  }


  public static Optional<String> getNamespaceURIForPrefix(String pfx) {
    return Optional.ofNullable(prefixToNamespaceMap.get(pfx));
  }

  public static Optional<String> getPrefixforNamespace(String namespace) {
    return Optional.ofNullable(namespaceToPrefixMap.get(namespace));
  }

  public static Optional<String> getPrefixforNamespace(URI namespace) {
    try {
      return getPrefixforNamespace(new URI(namespace.getScheme(),
          namespace.getAuthority(),
          namespace.getPath(),
          null,
          null).toString());
    } catch (URISyntaxException e) {
      logger.error(e.getMessage(),e);
      return Optional.empty();
    }
  }

  public static Optional<String> getCatalog(URI lang) {
    try {
      return Optional.ofNullable(xcat.resolvePublic(lang.toString(),null))
          .map(URI::create)
          .map(URI::getPath)
          .map(path -> "/xsd"+path);
    } catch (IOException e) {
      logger.error(e.getMessage(),e);
      return Optional.empty();
    }
  }

  public static Optional<String> getValidationSchema(URI lang) {
    return Optional.ofNullable(languagSchemas.get(lang.toString()));
  }

  public static Collection<String> listPrefixes() {
    return new HashSet<>(prefixToNamespaceMap.keySet());
  }
}
