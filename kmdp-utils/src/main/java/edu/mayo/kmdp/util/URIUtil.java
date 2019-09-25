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

import edu.mayo.kmdp.registry.Registry;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.w3c.dom.Element;

import javax.management.RuntimeErrorException;
import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;

public class URIUtil {
  
  public static final Logger logger = LoggerFactory.getLogger(URIUtil.class);

  private URIUtil() {}

  public static URI normalizeURI(URI uri) {
    return fromNamespacedFragment(null, uri);
  }

  public static URI fromNamespacedFragment(String fragment, URI uri) {
    Objects.requireNonNull(uri);
    try {
      if ("urn".equals(uri.getScheme())) {
        return uri;
      }
      return new URI(uri.getScheme(),
          uri.getAuthority(),
          uri.getPath(),
          null,
          fragment);
    } catch (URISyntaxException e) {
      logger.error(e.getMessage(),e);
      throw new RuntimeErrorException(new Error(e.getMessage()));
    }
  }

  public static String normalizeURIString(URI uri) {
    return Objects.requireNonNull(normalizeURI(uri)).toString();
  }


  public static Optional<URI> asUri(String str) {
    if (Util.isEmpty(str)) {
      return Optional.empty();
    }
    try {
      return Optional.of(URI.create(str));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public static Optional<QName> toQName(URI uri) {
    if (uri == null || Util.isEmpty(uri.getFragment())) {
      return Optional.empty();
    } else {
      return Optional.of(new QName(normalizeURIString(uri),
          NameUtils.toElementName(uri.getFragment()),
          Registry.getPrefixforNamespace(uri).orElse(null)));
    }
  }

  public static String toURIString(String namespaceURI, String localName) {
    return Util.isEmpty(namespaceURI)
        ? localName
        : URI.create(namespaceURI + "#" + localName).toString();
  }

  public static QName parseQName(String prefixedLocal) {
    int idx = prefixedLocal.indexOf(':');
    String prefix = prefixedLocal.substring(0, idx);
    return new QName(Registry.getNamespaceURIForPrefix(prefix)
        .orElse(null),
        prefixedLocal.substring(idx + 1),
        prefix);
  }

  public static QName parseQName(String prefixedLocal, Element el) {
    int idx = prefixedLocal.indexOf(':');
    String prefix = prefixedLocal.substring(0, idx);
    return new QName(el.lookupNamespaceURI(prefix),
        prefixedLocal.substring(idx + 1),
        prefix);
  }

  public static String toPrefixedName(QName qId) {
    String prefix = Util.isEmpty(qId.getPrefix())
        ? Registry.getPrefixforNamespace(qId.getNamespaceURI()).orElse("")
        : qId.getPrefix();
    return Util.isEmpty(prefix)
        ? qId.getLocalPart()
        : prefix + ":" + qId.getLocalPart();
  }

  public static String toPrefixedName(URI uri) {
    return toQName(uri)
        .map(URIUtil::toPrefixedName)
        .orElse(uri.getFragment());
  }

  public static URL asURL(String str) {
    try {
      return new URL(str);
    } catch (MalformedURLException e) {
      return null;
    }
  }

  public static boolean isUri(String id) {
    return asUri(id).isPresent();
  }

  public static URI detectNamespace(URI uri) {
    if (uri.getFragment() != null) {
      return normalizeURI(uri);
    } else {
      String str = uri.toString();
      str = str.substring(0, str.lastIndexOf('/'));
      return asUri(str)
          .orElseThrow(UnsupportedOperationException::new);
    }
  }

  public static String detectLocalName(URI uri) {
    if (uri.getFragment() != null) {
      return uri.getFragment();
    } else {
      String str = uri.toString();
      return str.substring(str.lastIndexOf('/') + 1);
    }
  }
}
