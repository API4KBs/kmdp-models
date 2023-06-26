/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp.registry;

import static edu.mayo.kmdp.registry.RegistryUtil.findLatestLexicographically;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.xml.catalog.CatalogFeatures;
import javax.xml.catalog.CatalogManager;
import javax.xml.catalog.CatalogResolver;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

public class Registry {

  public static final String ONTOLOGY_VER = "LATEST";

  public static final String REGISTRY_URI = "https://www.omg.org/spec/API4KP/api4kp-registry";

  public static final String MAYO_ASSETS_BASE_URI = "https://clinicalknowledgemanagement.mayo.edu/assets/";
  public static final URI MAYO_ASSETS_BASE_URI_URI = URI.create(MAYO_ASSETS_BASE_URI);
  public static final String MAYO_ARTIFACTS_BASE_URI = "https://clinicalknowledgemanagement.mayo.edu/artifacts/";
  public static final URI MAYO_ARTIFACTS_BASE_URI_URI = URI.create(MAYO_ARTIFACTS_BASE_URI);

  public static final String URN = "urn:";
  public static final String UUID_URN = "urn:uuid:";
  public static final URI UUID_URN_URI = URI.create(UUID_URN);
  public static final String DID_URN = "did:mcid:";
  public static final URI DID_URN_URI = URI.create(DID_URN);

  public static final String KNOWLEDGE_ASSET_URI = "https://www.omg.org/spec/API4KP/api4kp/KnowledgeAsset";

  public static final List<String> ID_SCHEMES = List.of("urn", "did");
  public static final List<String> HTTP_SCHEMES = List.of("http", "https");

  private static CatalogResolver xcat;

  private static final Logger logger = LoggerFactory.getLogger(Registry.class);

  private static final Map<String, String> prefixToNamespaceMap = new HashMap<>();
  private static final Map<String, String> namespaceToPrefixMap = new HashMap<>();
  private static final Map<String, String> languagSchemas = new HashMap<>();

  protected Registry() {

  }

  public static boolean isGlobalIdentifier(String uri) {
    return uri != null && ID_SCHEMES.stream().anyMatch(uri::startsWith);
  }

  public static boolean isGlobalIdentifier(URI uri) {
    return uri != null && ID_SCHEMES.stream().anyMatch(x -> uri.getScheme().equals(x));
  }

  public static boolean isHttpIdentifier(String uri) {
    return uri != null && HTTP_SCHEMES.stream().anyMatch(uri::startsWith);
  }

  public static boolean isHttpIdentifier(URI uri) {
    return uri != null && HTTP_SCHEMES.stream().anyMatch(x -> uri.getScheme().equals(x));
  }

  public static URI mapAssetToArtifactNamespace(URI assetNS) {
    if (assetNS == null
        || assetNS == MAYO_ASSETS_BASE_URI_URI
        || MAYO_ASSETS_BASE_URI.equals(assetNS.toString())) {
      return MAYO_ARTIFACTS_BASE_URI_URI;
    }
    return Registry.DID_URN_URI;
  }

  public static String mapAssetToArtifactNamespace(String assetNS) {
    if (assetNS != null && assetNS.equals(MAYO_ASSETS_BASE_URI)) {
      return MAYO_ARTIFACTS_BASE_URI;
    }
    return Registry.DID_URN;
  }

  static {

    try {
      xcat = CatalogManager.catalogResolver(
          CatalogFeatures.defaults(),
          Registry.class.getResource(getCatalogRef()).toURI());

      String xmlPrefixesQry = RegistryUtil.read("/xmlNSprefixes.sparql");
      String xmlSchemasQry = RegistryUtil.read("/xmlSchemas.sparql");
      String path = xcat.resolve(REGISTRY_URI, null).getSystemId();

      Model registryGraph = ModelFactory.createDefaultModel()
          .read(openStream(path), null);
      registryGraph = ModelFactory.createInfModel(ReasonerRegistry.getOWLMicroReasoner(),
          registryGraph);

      populatePrefixMap(RegistryUtil.askQuery(xmlPrefixesQry, registryGraph));

      RegistryUtil.askQuery(xmlSchemasQry, registryGraph).forEach(
          m -> languagSchemas.put(m.get("L"), m.get("NS"))
      );
    } catch (IOException | URISyntaxException e) {
      logger.error(e.getMessage(), e);
    }

  }

  public static String getCatalogRef() {
    return getCatalogVersion(ONTOLOGY_VER);
  }

  public static String getCatalogVersion(String version) {
    return String.format("/%s/meta-catalog.xml", version);
  }

  private static InputStream openStream(String path) throws IOException {
    URL url = new URL(path);
    if ("file".equals(url.getProtocol())) {
      return Registry.class.getResourceAsStream(url.getFile());
    } else {
      return url.openStream();
    }
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
    versions.forEach((key, value) -> {
      if (!key.isEmpty() && !value.isEmpty() && !prefixToNamespaceMap.containsKey(key)) {
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
      logger.error(e.getMessage(), e);
      return Optional.empty();
    }
  }

  public static Optional<String> getCatalog(URI lang) {
    return Optional.ofNullable(
            xcat.resolveEntity(lang.toString(), lang.toString()))
        .map(InputSource::getSystemId)
        .map(URI::create)
        .map(URI::getPath)
        .map(path -> "/xsd" + path);
  }

  public static Optional<String> getValidationSchema(URI lang) {
    return Optional.ofNullable(languagSchemas.get(lang.toString()));
  }

  public static Collection<String> listPrefixes() {
    return new HashSet<>(prefixToNamespaceMap.keySet());
  }
}
