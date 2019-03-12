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

import net.sf.saxon.lib.RelativeURIResolver;
import org.apache.xerces.util.XMLCatalogResolver;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

import static edu.mayo.kmdp.util.URIUtil.asURL;
import static edu.mayo.kmdp.util.XMLUtil.catalogResolver;

public class CatalogBasedURIResolver implements RelativeURIResolver {

  private String loc;

  private XMLCatalogResolver xcat;

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
    xcat = catalogResolver(catalog);
  }

  public CatalogBasedURIResolver(String... catalog) {
    xcat = catalogResolver(catalog);
  }

  @Override
  public String makeAbsolute(String href, String base) throws TransformerException {

    try {
      URI uri = new URI(href);
      if (uri.isAbsolute()) {
        return href;
      }
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    try {
      String abs = xcat.resolvePublic(href, href);
      return abs;
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
      StreamSource src = new StreamSource(fis);
      return src;
    } catch (URISyntaxException | FileNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public Source resolve(String href, String base) throws TransformerException {
    try {
      String resolved = xcat.resolvePublic(href, href);
      File f = new File(new URL(resolved).toURI());
      assert f.exists();
      FileInputStream fis = new FileInputStream(f);
      StreamSource src = new StreamSource(fis);
      src.setSystemId(resolved);
      src.setPublicId(resolved);
      return src;
    } catch (IOException | URISyntaxException e) {
      e.printStackTrace();
      return null;
    }
  }


  /**
   * Handle the case of undistinguished file paths, urls and jar resources
   * Leverages an XML Catalog for customizations
   * TODO everything should be consolidated into URLs
   * @param path
   * @param catalogURL
   * @return
   */
  public static Optional<InputStream> resolveFilePath(String path, URL catalogURL) {
    return resolveFilePathToURL(path, catalogURL).flatMap(CatalogBasedURIResolver::openStream);
  }

  private static Optional<InputStream> openStream(URL url) {
    try {
      return Optional.ofNullable(url.openStream());
    } catch (IOException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  /**
   * Handle the case of undistinguished file paths, urls and jar resources
   * Leverages an XML Catalog for customizations
   * TODO everything should be consolidated into URLs
   * @param path
   * @param catalogURL
   * @return
   */
  public static Optional<URL> resolveFilePathToURL(String path, URL catalogURL) {
    File f = new File(FileUtil.asPlatformSpecific(path));
    if (f.exists()) {
      try {
        return Optional.of(f.toURI().toURL());
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
    }

    if (catalogURL != null) {
      XMLCatalogResolver resolver = XMLUtil.catalogResolver(catalogURL);
      try {
        String resolved = resolver.resolveURI(path);
        if (resolved != null) {
          return resolveFilePathToURL(resolved, catalogURL);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    URL url;
    if ((url = asURL(path)) != null) {
      try {
        if (new File(url.toURI()).exists()) {
          return Optional.ofNullable(url);
        } else {
          // try classpath (e.g. jars)
          url = CatalogBasedURIResolver.class.getResource(path.substring(5));
          if (url != null) {
            InputStream stream = url.openStream();
            if (stream != null && stream.available() > 0) {
              return Optional.ofNullable(url);
            }
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      } catch (URISyntaxException e) {
        // do nothing
      }
    }
    return Optional.empty();
  }

  /**
   * Catalog-less version, typically used to look up catalogs in the first place :)
   * @param path
   * @return
   */
  public static Optional<URL> resolveFilePathToURL(String path) {
    return resolveFilePathToURL(path, null);
  }

}
