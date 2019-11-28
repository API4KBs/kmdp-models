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

import static edu.mayo.kmdp.util.URIUtil.asURL;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.lib.RelativeURIResolver;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CatalogBasedURIResolver implements RelativeURIResolver {

  private static Logger logger = LoggerFactory.getLogger(CatalogBasedURIResolver.class);

  private String loc;

  private CatalogResolver xcat;

  public String getLoc() {
    return loc;
  }

  public void setLoc(String loc) {
    this.loc = loc;
  }

  public CatalogBasedURIResolver withLoc(String loc) {
    setLoc(loc);
    return this;
  }

  public CatalogBasedURIResolver(URL catalog) {
    xcat = XMLUtil.catalogResolver(catalog);
  }

  public CatalogBasedURIResolver(String... catalog) {
    xcat = XMLUtil.catalogResolver(catalog);
  }

  @Override
  public String makeAbsolute(String href, String base) throws TransformerException {

    try {
      URI uri = new URI(href);
      if (uri.isAbsolute()) {
        return href;
      }
    } catch (URISyntaxException e) {
      logger.error(e.getMessage(),e);
    }

    try {
      return xcat.getCatalog().resolvePublic(href, href);
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public Source dereference(String uri) throws TransformerException {
    try {
      File f = new File(new URI(uri));
      assert f.exists();
      FileInputStream fis = new FileInputStream(f);
      return new StreamSource(fis);
    } catch (URISyntaxException | FileNotFoundException e) {
      logger.error(e.getMessage(),e);
    }
    return null;
  }

  @Override
  public Source resolve(String href, String base) throws TransformerException {
    try {
      String resolved = xcat.getCatalog().resolvePublic(href, href);
      File f = new File(new URL(resolved).toURI());
      assert f.exists();
      FileInputStream fis = new FileInputStream(f);
      StreamSource src = new StreamSource(fis);
      src.setSystemId(resolved);
      src.setPublicId(resolved);
      return src;
    } catch (IOException | URISyntaxException e) {
      logger.error(e.getMessage(),e);
      return null;
    }
  }


  /**
   * Handle the case of undistinguished file paths, urls and jar resources
   * Leverages an XML Catalog for customizations
   * @param path
   * @param catalogURL
   * @return The InputStream the catalog resource points to, if any
   */
  public static Optional<InputStream> resolveFilePath(String path, URL catalogURL) {
    return resolveFilePathToURL(path, catalogURL).flatMap(CatalogBasedURIResolver::openStream);
  }

  private static Optional<InputStream> openStream(URL url) {
    try {
      return Optional.ofNullable(url.openStream());
    } catch (IOException e) {
      logger.error(e.getMessage(),e);
      return Optional.empty();
    }
  }

  /**
   * Handle the case of undistinguished file paths, urls and jar resources
   * Leverages an XML Catalog for customizations
   * @param path
   * @param catalogURL
   * @return The catalog-mapped URL, if any
   */
  public static Optional<URL> resolveFilePathToURL(String path, URL catalogURL) {
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
    URL url;
    if ((url = asURL(path)) == null) {
      return Optional.empty();
    }
    try {
      if ("file".equals(url.getProtocol()) && new File(url.toURI()).exists()) {
        return Optional.of(url);
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

  private static Optional<URL> tryResolveFromCatalog(String path, URL catalogURL) {
    CatalogResolver resolver = XMLUtil.catalogResolver(catalogURL);
    try {
      String resolved = resolver.getCatalog().resolveURI(path);
      if (resolved != null) {
        return resolveFilePathToURL(resolved, catalogURL);
      }
    } catch (IOException e) {
      throw new CatalogResolutionErrorException(e);
    }
    return Optional.empty();
  }

  private static Optional<URL> tryResolveFromFile(File f) {
    try {
      return Optional.of(f.toURI().toURL());
    } catch (MalformedURLException e) {
      logger.error(e.getMessage(),e);
      return Optional.empty();
    }
  }

  /**
   * Catalog-less version, typically used to look up catalogs in the first place :)
   * @param path
   * @return The URL of the file the relative path maps to, if any
   */
  public static Optional<URL> resolveFilePathToURL(String path) {
    return resolveFilePathToURL(path, null);
  }

  public static class CatalogResolutionErrorException extends RuntimeException {
    public CatalogResolutionErrorException(Exception e) {
      super(e);
    }
  }

}
