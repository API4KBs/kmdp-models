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
package edu.mayo.kmdp.util;

import static edu.mayo.kmdp.util.URIUtil.asURL;
import static edu.mayo.kmdp.util.Util.isNotEmpty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import javax.xml.catalog.CatalogException;
import javax.xml.catalog.CatalogFeatures;
import javax.xml.catalog.CatalogFeatures.Feature;
import javax.xml.catalog.CatalogManager;
import javax.xml.catalog.CatalogResolver;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class CatalogBasedURIResolver implements URIResolver, LSResourceResolver, EntityResolver {

  private static final Logger logger = LoggerFactory.getLogger(CatalogBasedURIResolver.class);

  private final URL absoluteLocationBase;

  private final CatalogResolver xcat;


  public static CatalogResolver catalogResolver(URI... catalogs) {
    return CatalogManager.catalogResolver(
        CatalogFeatures.builder()
            .with(Feature.PREFER, "public")
            .with(Feature.DEFER, "false")
            .with(Feature.RESOLVE, "continue")
            .build(),
        catalogs);
  }

  public static CatalogResolver catalogResolver(String... catalogRelativePaths) {
    return catalogResolver(
        Arrays.stream(catalogRelativePaths)
            .map(XMLUtil::asFileURL)
            .map(URIUtil::asURI)
            .filter(Objects::nonNull)
            .toArray(URI[]::new));
  }

  public CatalogBasedURIResolver(URL baseLoc, String catalogURLs) {
    this.absoluteLocationBase = baseLoc;
    this.xcat = isNotEmpty(catalogURLs)
        ? catalogResolver(catalogURLs.split(","))
        : null;
  }

  public CatalogBasedURIResolver(URL baseLoc, CatalogResolver catalogResolver) {
    this.xcat = catalogResolver;
    this.absoluteLocationBase = baseLoc;
  }

  public CatalogBasedURIResolver(CatalogResolver catalogResolver) {
    this.xcat = catalogResolver;
    this.absoluteLocationBase = null;
  }

  public String makeAbsolute(String href, URL base) {
    if (href == null) {
      return null;
    }
    URI uri = URI.create(href);
    if (uri.isAbsolute()) {
      return href;
    }

    URL root = getBaseURL(base);
    if (root == null) {
      return href;
    }

    URI rootUri = null;
    try {
      rootUri = root.toURI();
      return rootUri.resolve(href).toString();
    } catch (URISyntaxException e) {
      return null;
    }
  }

  private URL getBaseURL(String resolutionContextBase) {
    return getBaseURL(asURL(resolutionContextBase));
  }

  private URL getBaseURL(URL resolutionContextBase) {
    // use the resolution context first
    if (resolutionContextBase != null) {
      return resolutionContextBase;
    }
    // use the fixed location provided at construction time
    if (absoluteLocationBase != null) {
      return absoluteLocationBase;
    }
    // use the system root declared in the catalog itself
    if (xcat != null) {
      Source catalogBase = xcat.resolve(".", "");
      if (catalogBase != null && catalogBase.getSystemId() != null) {
        String systemBase = catalogBase.getSystemId();
        // this will include the catalog itself, so strip
        return asURL(systemBase.substring(0, systemBase.lastIndexOf('/') + 1));
      }
    }
    // else?
    return null;
  }

  public Source dereferenceFileUri(String uri) {
    try {
      File f = new File(new URI(uri));
      assert f.exists();
      FileInputStream fis = new FileInputStream(f);
      StreamSource ss = new StreamSource(fis);
      ss.setSystemId(uri);
      return ss;
    } catch (URISyntaxException | FileNotFoundException e) {
      logger.error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public Source resolve(String href, String base) throws TransformerException {
    try {
      if (xcat != null) {
        Source src = xcat.resolve(href, base);
        if (src != null) {
          return src;
        }
      }
    } catch (CatalogException ce) {
      // try to recover
    }

    try {
      Optional<URL> tentativeURL = tryResolveUsingPathAsURL(href);
      if (tentativeURL.isPresent()) {
        StreamSource ss = new StreamSource(tentativeURL.get().openStream());
        ss.setSystemId(tentativeURL.get().toString());
        return ss;
      }

      String absolute = makeAbsolute(href, getBaseURL(base));
      Optional<URL> tentativeAbsoluteURL = tryResolveUsingPathAsURL(absolute);
      if (tentativeAbsoluteURL.isPresent()) {
        StreamSource ss = new StreamSource(tentativeAbsoluteURL.get().openStream());
        ss.setSystemId(tentativeAbsoluteURL.get().toString());
        return ss;
      }

      return null;
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
    return null;
  }


  /**
   * Handle the case of undistinguished file paths, urls and jar resources Leverages an XML Catalog
   * for customizations
   *
   * @param path
   * @param catalogURL
   * @return The InputStream the catalog resource points to, if any
   */
  public static Optional<InputStream> resolveFilePath(String path, URI catalogURL) {
    return resolveFilePathToURL(path, catalogURL).flatMap(CatalogBasedURIResolver::openStream);
  }

  private static Optional<InputStream> openStream(URL url) {
    try {
      return Optional.ofNullable(url.openStream());
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      return Optional.empty();
    }
  }

  /**
   * Handle the case of undistinguished file paths, urls and jar resources Leverages an XML Catalog
   * for customizations
   *
   * @param path
   * @param catalogURL
   * @return The catalog-mapped URL, if any
   */
  public static Optional<URL> resolveFilePathToURL(String path, URI catalogURL) {
    File f = new File(FileUtil.asPlatformSpecific(path));
    Optional<URL> resolved;

    if (f.exists()) {
      resolved = tryResolveFromFile(f);
      if (resolved.isPresent()) {
        return resolved;
      }
    }

    if (catalogURL != null) {
      resolved = tryResolveFromCatalog(path, catalogURL);
      if (resolved.isPresent()) {
        return resolved;
      }
    }

    return tryResolveUsingPathAsURL(path);
  }

  private static Optional<URL> tryResolveUsingPathAsURL(String path) {
    URL url = asURL(path);
    if ((url ) == null) {
      return Optional.empty();
    }
    try {
      if ("file".equals(url.getProtocol())) {
        URI fileUri = url.toURI();
        if (Files.exists(Path.of(fileUri))) {
          return Optional.of(url);
        }
      }
    } catch (URISyntaxException e) {
      // do nothing
    }

    try {
      // try classpath (e.g. jars)
      url = CatalogBasedURIResolver.class.getResource(path.substring(5));
      if (url != null) {
        try (InputStream stream = url.openStream()) {
          if (stream != null && stream.available() > 0) {
            return Optional.of(url);
          }
        }
      }
    } catch (IOException e) {
      throw new CatalogResolutionErrorException(e);
    }
    return Optional.empty();
  }

  private static Optional<URL> tryResolveFromCatalog(String path, URI catalogURL) {
    CatalogResolver resolver = catalogResolver(catalogURL);
    try {
      InputSource resolvedSrc = resolver.resolveEntity(path, "");
      if (resolvedSrc != null && resolvedSrc.getSystemId() != null) {
        return resolveFilePathToURL(resolvedSrc.getSystemId(), catalogURL);
      }
    } catch (Exception e) {
      throw new CatalogResolutionErrorException(e);
    }
    return Optional.empty();
  }

  private static Optional<URL> tryResolveFromFile(File f) {
    try {
      return Optional.of(f.toURI().toURL());
    } catch (MalformedURLException e) {
      logger.error(e.getMessage(), e);
      return Optional.empty();
    }
  }

  /**
   * Catalog-less version, typically used to look up catalogs in the first place :)
   *
   * @param path
   * @return The URL of the file the relative path maps to, if any
   */
  public static Optional<URL> resolveFilePathToURL(String path) {
    return resolveFilePathToURL(path, null);
  }

  @Override
  public LSInput resolveResource(
      String type,
      String namespaceUri,
      String publicId,
      String systemId,
      String baseUri) {
    try {
      String absoluteSystemId = makeAbsolute(systemId, new URL(baseUri));
      absoluteSystemId = absoluteSystemId == null ? namespaceUri : absoluteSystemId;
      return xcat.resolveResource(type, namespaceUri, publicId, absoluteSystemId, baseUri);
    } catch (Exception e) {
      logger.error(e.getMessage(),e);
      return null;
    }
  }

  @Override
  public InputSource resolveEntity(String publicId, String systemId) {
    // systemId cannot be null
    InputSource src = xcat.resolveEntity(publicId, systemId != null ? systemId : "");
    if (src != null) {
      return src;
    }

    try {
      Source s = resolve(systemId, null);
      return XMLUtil.sourceToInputSource(s);
    } catch (TransformerException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static class CatalogResolutionErrorException extends RuntimeException {

    public CatalogResolutionErrorException(Exception e) {
      super(e);
    }
  }

}
