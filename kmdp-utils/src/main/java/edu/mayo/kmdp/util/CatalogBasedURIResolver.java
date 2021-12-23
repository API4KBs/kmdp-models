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
import static edu.mayo.kmdp.util.Util.isEmpty;
import static edu.mayo.kmdp.util.Util.isNotEmpty;
import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import javax.xml.catalog.CatalogFeatures;
import javax.xml.catalog.CatalogFeatures.Feature;
import javax.xml.catalog.CatalogManager;
import javax.xml.catalog.CatalogResolver;
import javax.xml.stream.XMLResolver;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Utility class that uses an XML {@link CatalogResolver} to perform URI to URL mappings, i.e. to
 * resolve identifiers of documents to locations where copies of those documents can be found.
 * <p>
 * PUBLIC identifiers are usually standard, permanent, and transparent to location. SYSTEM
 * identifiers can be absolute or relative, and scoped to the (virtual) machine processing the
 * information. URIs can also be aliased to each other. See <href a="https://xerces.apache.org/xml-commons/components/resolver/resolver-article.html/>
 * for details.
 * <p>
 * The resolution is used by various frameworks, including processors of XML schemas (e.g. JaxB, XSD
 * Validators), XML documents (including XSLT. for imports, includes), but can also be used to build
 * artifact/model/document dependency handlers (e.g. OWL import mappers)
 * <p>
 * This class implements the key interfaces of CatalogResolver - {@link URIResolver}, {@link
 * LSResourceResolver}, {@link EntityResolver} and {@link XMLResolver} but not CatalogResolver
 * itself due to a clash between the interfaces in terms of Exception handling.
 * <p>
 * This class also supplements the native CatalogResolver implementation, adding support for
 * relative URIs and classpath based resolution.
 * <p>
 * This wrapper is primarily designed to support systems where resources are either generated
 * locally, or pre-fetched and cached, so that downstream computations do not have to rely on
 * possibly unstable/unreliable network connections. For this reason, whenever URI are resolved to
 * system URI, that system URI is expected to resolve to an actual file/document/stream, or the
 * resolution will fail. In other words, this class will support purely remote resources, or
 * resources with an actual local copy, but will fail in case of unresolvable local references.
 */
public class CatalogBasedURIResolver
    implements URIResolver, LSResourceResolver, EntityResolver, XMLResolver {

  /**
   * The delegate {@link CatalogResolver}
   */
  protected final CatalogResolver xcat;

  /**
   * Factory method that builds a native Java {@link CatalogResolver}, wrapping one or more {@link
   * javax.xml.catalog.Catalog} retrieved from the given URIs, which are assumed to be directly
   * dereferenceable.
   * <p>
   * Configures the Resolver as follows: will try to map public IDs first, will load all catalogs at
   * once, and will not throw exceptions in case an entry cannot be resolved.
   * <p>
   * The {@link CatalogResolver} can be used as-is, or wrapped in a {@link CatalogBasedURIResolver}
   *
   * @param catalogs the URIs pointing to the Catalog documents
   * @return a pre-configured CatalogResolver wrapping the resolved Catalogs
   */
  public static CatalogResolver catalogResolver(URI... catalogs) {
    if (catalogs == null || Arrays.stream(catalogs).anyMatch(Objects::isNull)) {
      return null;
    }
    return CatalogManager.catalogResolver(
        CatalogFeatures.builder()
            .with(Feature.PREFER, "public")
            .with(Feature.DEFER, "false")
            .with(Feature.RESOLVE, "continue")
            .build(),
        catalogs);
  }

  /**
   * Factory method that builds a native Java {@link CatalogResolver}. Assumes the Catalogs are
   * referenced by means of URL (Strings) relative to the application's classpath. The URLs will be
   * resolved using {@link Class#getResource(String)}, mapped to URIs and then used to build a
   * {@link CatalogResolver}
   *
   * @param catalogRelativePaths The
   * @return a pre-configured CatalogResolver wrapping the resolved Catalogs
   * @see CatalogBasedURIResolver#catalogResolver(URI...)
   */
  public static CatalogResolver catalogResolver(String... catalogRelativePaths) {
    if (catalogRelativePaths == null || catalogRelativePaths.length == 0) {
      return null;
    }
    return catalogResolver(
        Arrays.stream(catalogRelativePaths)
            .map(XMLUtil::asFileURL)
            .map(URIUtil::asURI)
            .filter(Objects::nonNull)
            .toArray(URI[]::new));
  }

  /**
   * Constructor.
   * <p>
   * Wraps a {@link CatalogResolver}
   *
   * @param catalogResolver the delegate {@link CatalogResolver}
   */
  public CatalogBasedURIResolver(CatalogResolver catalogResolver) {
    this.xcat = catalogResolver;
  }

  /**
   * Constructor. Combines the construction of a {@link CatalogResolver} with the wrapping into a
   * {@link CatalogBasedURIResolver}
   *
   * @param catalogURLs the URLs used to build a {@link CatalogManager}
   * @see CatalogBasedURIResolver#catalogResolver(String...)
   */
  public CatalogBasedURIResolver(String catalogURLs) {
    this(isNotEmpty(catalogURLs)
        ? catalogResolver(catalogURLs.split(","))
        : null);
  }

  /**
   * Constructor. Creates and wraps a {@link CatalogResolver} using the provided URIs as Catalog
   * sources
   *
   * @param catalogURIs the URIs used to build a {@link CatalogManager}
   */
  public CatalogBasedURIResolver(URI... catalogURIs) {
    this(catalogResolver(catalogURIs));
  }


  /*
      XML import/include/document() URI Resolver Interface
  */

  /**
   * Invoked when processing XML documents with external references, when processing imports,
   * includes or document() functions.
   * <p>
   * <li>
   *   <ul>Will use a catalog to remap the href URI, and lookup the resulting URI in the classpath if the URI is local</ul>
   *   <ul>ELSE, will tenatively consider href a system URI, and look it up in the context of the classpath</ul>
   *   <ul>ELSE, will tentatively consider href relative to base, and look it up in the context of the classpath</ul>
   *   <ul>ELSE, will throw a {@link TransformerException}</ul>
   * </li>
   *
   * @param href the reference to the external document, absolute or relative
   * @param base the base URI, used to make href absolute, when href is relative
   * @return a {@link Source}, where the URI is resolved if local to the system processing the
   * information
   * @throws TransformerException if the resolution fails
   */
  @Override
  public Source resolve(String href, String base) throws TransformerException {
    return innerResolve(href, base)
        .orElseThrow(() ->
            new TransformerException(format(
                "Unable to resolve href %s in the context of base %s", href, base)));
  }

  /**
   * Adapter implementation of {@link #resolve(String, String)} that does not throw a {@link
   * TransformerException}, but returns {@link Optional#empty()} instead
   *
   * @param href the reference to the external document, absolute or relative
   * @param base the base URI, used to make href absolute, when href is relative
   * @return a {@link Source}, where the URI is resolved if local to the system processing the
   */
  protected Optional<Source> innerResolve(String href, String base) {
    return delegateResolve(href, base)
        .or(() -> tryResolveLocal(href))
        .or(() -> tryResolveLocal(makeAbsolute(href, base)));
  }

  /**
   * Uses the internal {@link CatalogResolver}, if any, to resolve the href/base pair. If the result
   * is a system-specific Source, tries to open a connection to the system URI
   *
   * @param href the reference to the external document, absolute or relative
   * @param base the base URI, used to make href absolute, when href is relative
   * @return if sucessful, a Source - public, or system-specific with a connected InputStream
   */
  protected Optional<Source> delegateResolve(String href, String base) {
    if (href == null || isEmpty(base) || xcat == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(xcat.resolve(href, base))
        .flatMap(this::ensureRemoteOrConnected);
  }

  /*
      XML SAX parser Entity Resolver Interface
  */

  /**
   * Allows a SAX processor to resolve external entities, denoted by either a system agnostic or a
   * system specific identifier.
   * <p>
   * Will delegate to the internal {@link CatalogResolver}. If unable to resolve, will try to {@link
   * #resolve(String, String)} the systemId, and then the publicId, as absolute URIs, and only
   * return null if no strategy is successful. Note that {@link InputSource} is mapped to {@link
   * Source} in the process.
   *
   * @param publicId the (nullable) system-agnostic identifier of the resource to be resolved
   * @param systemId the system-specific identifier of the resource to be resolved
   * @return An InputSource object describing the new input source, or null to request that the
   * parser open a regular URI connection to the system identifier. Note that this implementation
   * @see XMLUtil#sourceToInputSource(Source)
   */
  @Override
  public InputSource resolveEntity(String publicId, String systemId) {
    // systemId cannot be null
    return delegateResolveEntity(publicId, systemId)
        .or(() -> innerResolve(systemId, "").map(XMLUtil::sourceToInputSource))
        .or(() -> innerResolve(publicId, "").map(XMLUtil::sourceToInputSource))
        .orElse(null);
  }


  protected Optional<InputSource> delegateResolveEntity(String publicId, String systemId) {
    if (xcat == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(
            xcat.resolveEntity(publicId, systemId != null ? systemId : ""))
        .flatMap(this::ensureRemoteOrConnected);
  }

  /*
      XML Resolver Interface
  */

  /**
   * {@link XMLResolver} interface to resolves external entities, restricted to return {@link
   * InputStream} connected to the resolved resource.
   * <p>
   * Delegates to the internal {@link CatalogResolver}. If failed, it tries to {@link
   * #resolveEntity(String, String)} using the public/system ids, and then to {@link
   * #resolve(String, String)} using the systemId/baseUri pair, reconciling the results to return an
   * InputStream. Note that the fallback strategies are provided by this class, to enforce the
   * resolution of local resources.
   *
   * @param publicId  the (nullable) system-agnostic identifier of the resource to be resolved
   * @param systemId  the system-specific identifier of the resource to be resolved
   * @param baseUri   the absolute baseURI associated with the system identifier
   * @param namespace the (target) namespace of the resource to be resolved
   * @return an Inputstream connected to the resource, or null. Null will force the client to try to
   * connect to the resource directly (note: this class will always try to use mediation)
   */
  @Override
  public InputStream resolveEntity(
      String publicId, String systemId, String baseUri, String namespace) {
    return delegateResolveEntity(publicId, systemId, baseUri, namespace)
        .or(() -> Optional.ofNullable(resolveEntity(publicId, systemId))
            .flatMap(s -> Optional.ofNullable(s.getByteStream())))
        .or(() -> innerResolve(systemId, baseUri)
            .map(XMLUtil::sourceToInputSource)
            .flatMap(s -> Optional.ofNullable(s.getByteStream())))
        .orElse(null);
  }

  /**
   * Internal implementation of {@link #resolveEntity(String, String, String, String)} that
   * delegates to the {@link CatalogResolver}, if any
   *
   * @param publicId  the (nullable) system-agnostic identifier of the resource to be resolved
   * @param systemId  the system-specific identifier of the resource to be resolved
   * @param baseUri   the absolute baseURI associated with the system identifier
   * @param namespace the (target) namespace of the resource to be resolved
   * @return an Inputstream connected to the resource, or null. Null will force the client to try to
   */
  protected Optional<InputStream> delegateResolveEntity(
      String publicId, String systemId, String baseUri, String namespace) {
    if (xcat == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(xcat.resolveEntity(publicId, systemId, baseUri, namespace));
  }


  /*
      XML DOM LS Resource Resolver Interface
  */

  /**
   * DOM interface used for the resolution of external (XML related) resources, invoked during the
   * processing (parsing) of a 'current' resource.
   * <p>
   * Delegates to the underlying catalog. In case of failure, tries to use the namespaceUri as a
   * publicId surrogate
   *
   * @param type         URI that denotes the resource type, e.g. Document vs Schema vs other
   * @param namespaceUri the target Namespace of the referenced resource
   * @param publicId     the public URI of the referenced resource
   * @param systemId     the system URI of the referenced resource
   * @param baseUri      the absolute URI of the current resource
   * @return An LSInput, or null if unable to resolve, or null to force the client to open a direct
   * connection to the external resource, without mediation (note: this class will always try to use
   * mediation)
   */
  @Override
  public LSInput resolveResource(
      String type,
      String namespaceUri,
      String publicId,
      String systemId,
      String baseUri) {
    if (xcat == null) {
      return null;
    }
    return
        // delegate as-is
        delegateResolveResource(type, namespaceUri, publicId, systemId, baseUri)
            .or(() ->
                // assume namespace coincides with publicId
                delegateResolveResource(type, namespaceUri, namespaceUri, systemId, baseUri))
            .orElse(null);
  }

  /**
   * Internal implementation of {@link #resolveResource(String, String, String, String, String)}
   * that relies on the internal {@link CatalogResolver}
   * <p>
   * Handles exceptions and wraps the result in Optional
   *
   * @param type         URI that denotes the resource type, e.g. Document vs Schema vs other
   * @param namespaceUri the target Namespace of the referenced resource
   * @param publicId     the public URI of the referenced resource
   * @param systemId     the system URI of the referenced resource
   * @param baseUri      the absolute URI of the current resource
   * @return an LSInput, if successful
   */
  public Optional<LSInput> delegateResolveResource(
      String type,
      String namespaceUri,
      String publicId,
      String systemId,
      String baseUri) {
    if (xcat == null) {
      return Optional.empty();
    }
    try {
      return Optional.ofNullable(
          xcat.resolveResource(
              type,
              namespaceUri,
              publicId != null ? publicId : "",
              systemId != null ? systemId : "",
              baseUri));
    } catch (Exception e) {
      return Optional.empty();
    }
  }


  /* ****************************************************************************************** */


  /**
   * Ensures that a URI is absolute.
   * <p>
   * If a URI is absolute, returns the URI itself. Otherwise, resolves the relative URI in the
   * context of the given base URI.
   *
   * @param href the URI to be made absolute, as a String
   * @param base a base, absolute URL used to resolve relative URIs
   * @return An absolute URI
   */
  protected String makeAbsolute(String href, String base) {
    if (href == null || base == null) {
      return null;
    }

    URI uri = URI.create(href);
    if (uri.isAbsolute()) {
      return href;
    }

    return URI.create(base).resolve(href).toString();
  }

  /**
   * Given a {@link Source} instance, if the source has a systemId, ensures that the source wraps an
   * InputStream connected to the local resource.
   *
   * @param src a {@link Source}
   * @return a Source connected to a local copy of the referenced resource, if the resource has a
   * system ID and the ID can be resolved. If no systemId is present, returns the original Source,
   * preserving public/remote references. Returns {@link Optional#empty()} is a system ID is
   * present, but the system ID cannot be resolved locally.
   */
  protected Optional<Source> ensureRemoteOrConnected(Source src) {
    if (src == null) {
      return Optional.empty();
    }
    if (src.getSystemId() == null) {
      return Optional.of(src);
    }
    return tryResolveLocal(src.getSystemId());
  }

  /**
   * Given a {@link InputSource} instance, if the source has a systemId, ensures that the source
   * wraps an InputStream connected to the local resource.
   *
   * @param src a {@link InputSource}
   * @return an InputSource connected to a local copy of the referenced resource, if the resource
   * has a system ID and the ID can be resolved. If no systemId is present, returns the original
   * Source, preserving public/remote references. Returns {@link Optional#empty()} is a system ID is
   * present, but the system ID cannot be resolved locally.
   */
  protected Optional<InputSource> ensureRemoteOrConnected(InputSource src) {
    if (src == null) {
      return Optional.empty();
    }
    if (src.getSystemId() == null) {
      return Optional.of(src);
    }
    return tryResolveLocal(src.getSystemId())
        .map(XMLUtil::sourceToInputSource);
  }


  /**
   * Creates a {@link StreamSource} implementation of {@link Source} from a URL stream. Handles
   * nulls and catches exceptions, via Optional
   *
   * @param url the source URL to open a stream from
   * @return An Optional Source
   */
  protected Optional<Source> openStreamSource(URL url) {
    return Optional.ofNullable(url)
        .flatMap(u -> this.tryOpenStream(u)
            .map(is -> {
              StreamSource ss = new StreamSource(is);
              ss.setSystemId(u.toString());
              return ss;
            }));
  }

  /**
   * Exception-safe wrapper for URL#openStream
   *
   * @param url the URL to open a stream from
   * @return An Optional Inpustream
   */
  protected Optional<InputStream> tryOpenStream(URL url) {
    try {
      if (url == null) {
        return Optional.empty();
      }
      return Optional.ofNullable(url.openStream());
    } catch (IOException e) {
      return Optional.empty();
    }
  }


  /**
   * Tentatively resolves a URI (string), interpreted as a reference to a file on the classpath.
   *
   * @param path the path - an absolute file/jar URI, or a relative classpath URI
   * @return a {@link Source} connected to the file resource, if successful
   */
  protected Optional<Source> tryResolveLocal(String path) {
    if (path == null) {
      return Optional.empty();
    }
    try {
      Path filePath = path.startsWith("file:")
          ? Path.of(URI.create(path))
          : Path.of(path);
      if (Files.exists(filePath)) {
        return Optional.of(filePath.toUri().toURL())
            .flatMap(this::openStreamSource);
      } else {
        return Optional.ofNullable(
                CatalogBasedURIResolver.class.getResource(path.replace("file:", "")))
            .flatMap(this::openStreamSource);
      }
    } catch (Exception e) {
      // assume not a file URI, fail gracefully
    }
    return Optional.empty();
  }


  /* FIXME */
  protected URL getBaseURL(String urlString) {
    URL resolutionContextBase = asURL(urlString);

    // use the resolution context first
    if (resolutionContextBase != null) {
      return resolutionContextBase;
    }
    // else?
    return null;
  }


}
