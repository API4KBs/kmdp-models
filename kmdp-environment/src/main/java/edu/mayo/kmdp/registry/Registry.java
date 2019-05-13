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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class Registry {

  public static final String path = "/ontologies/API4KP/informative/api4kp-registry.owl";

  public static final String MAYO_ASSETS_BASE_URI = "https://clinicalknowledgemanagement.mayo.edu/assets/";

  private static Model registry;



  private static BiMap<String, String> prefixToNamespaceMap = HashBiMap.create();
  private static Map<String, String> languagSchemas = new HashMap<>();


  static {
    String xmlPrefixesQry = RegistryUtil.read("/xmlNSprefixes.sparql");
    String xmlSchemasQry = RegistryUtil.read("/xmlSchemas.sparql");

    registry = ModelFactory.createOntologyModel()
        .read(Registry.class.getResourceAsStream(path),null);

    RegistryUtil.askQuery(xmlPrefixesQry, registry).forEach(
        (m) -> {
          prefixToNamespaceMap.put(m.get("P"), m.get("NS"));
        }
    );

    RegistryUtil.askQuery(xmlSchemasQry, registry).forEach(
        (m) -> {
          languagSchemas.put(m.get("L"), m.get("NS"));
        }
    );

 }


  public static Optional<String> getNamespaceURIForPrefix(String pfx) {
    return Optional.ofNullable(prefixToNamespaceMap.get(pfx));
  }

  public static Optional<String> getPrefixforNamespace(String namespace) {
    return Optional.ofNullable(prefixToNamespaceMap.inverse().get(namespace));
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
        return Arrays.asList("dmn-catalog.xml");
      case "https://www.omg.org/spec/CMMN/1.1":
        return Arrays.asList("cmmn-catalog.xml");
      case "urn:hl7-org:knowledgeartifact:r1":
        return Arrays.asList("knart-catalog.xml");
      case "urn:hl7-org:elm:r1":
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
