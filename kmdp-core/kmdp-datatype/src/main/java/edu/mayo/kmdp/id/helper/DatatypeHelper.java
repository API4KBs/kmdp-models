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
package edu.mayo.kmdp.id.helper;


import edu.mayo.kmdp.registry.Registry;
import edu.mayo.kmdp.util.DateTimeUtil;
import edu.mayo.kmdp.util.URIUtil;
import edu.mayo.kmdp.util.Util;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.id.VersionIdentifier;
import org.omg.spec.api4kp._20200801.id.VersionPatterns;
import org.omg.spec.api4kp._20200801.id.VersionTagType;

public class DatatypeHelper {

  protected DatatypeHelper() {

  }

  private static final Pattern VERSIONS_RX = Pattern.compile("^(.*/)?(.*)/versions/(.+)$");
  private static final Pattern SEMVER_RX = Pattern.compile("^(\\d+\\.)?(\\d+\\.)?(\\*|\\d+)$");


  public static SemanticIdentifier ns(final String nsUri) {
    return SemanticIdentifier.newId(URI.create(nsUri));
  }

  public static SemanticIdentifier ns(final String nsUri, String label, String version) {
    return SemanticIdentifier.newId(URI.create(nsUri), version);
  }

  private static URI ensureResolved(String termUri) {
    String uri = termUri;
    if (uri.matches("\\w+:.+")) {
      String candidatePfx = uri.substring(0, uri.indexOf(':'));
      String base = Registry.getNamespaceURIForPrefix(candidatePfx).orElse(null);
      if (base != null) {
        uri = base + "#" + termUri.substring(termUri.lastIndexOf(':') + 1);
      }
    }
    return URI.create(uri);
  }


  /**
   * Returns the default id/version separator based on the URI pattern
   * urn:uuid use ":", while http-based URIs use "/versions"
   * @param id the URI for which to get the id/version separator
   * @return the string used to separate the id from the version tag for this kind of id
   */
  public static String getVersionSeparator(String id) {
    return Registry.isGlobalIdentifier(id)
        ? ":"
        : "/versions/";
  }

  public static VersionTagType tag(final String tag) {
    if (tag.matches("\\d+")) {
      return VersionTagType.SEQUENTIAL;
    }
    if (DateTimeUtil.validateDate(tag)) {
      return VersionTagType.TIMESTAMP;
    }
    Matcher matcher = SEMVER_RX.matcher(tag);
    if (matcher.matches()) {
      return VersionTagType.SEM_VER;
    } else {
      return VersionTagType.GENERIC;
    }
  }

  public static String versionOf(URI versionedIdentifier) {
    if (versionedIdentifier == null) {
      return null;
    }
    String idStr = versionedIdentifier.toString();

    if (Registry.isGlobalIdentifier(versionedIdentifier)) {
      int end = idStr.lastIndexOf(':');
      return idStr.substring(end + 1);
    }

    VersionIdentifier vid = toVersionIdentifier(idStr);
    return vid.getVersionTag();
  }

  public static String tagOf(URI identifier) {
    if (identifier == null) {
      return null;
    }
    // '#' based URIs
    if (identifier.getFragment() != null) {
      return identifier.getFragment();
    }

    String idStr = identifier.toString();
    // 'urn:uuid:' identifiers
    if (Registry.isGlobalIdentifier(identifier)) {
      int start = idStr.indexOf(':');
      int end = idStr.lastIndexOf(':');
      // handle urn:uuid:tag vs urn:uuid:tag:version
      return end >= start
          ? idStr.substring(start, end)
          : idStr.substring(start);
    } else {
      // '/' identifiers
      return idStr.contains("/")
          ? idStr.substring(idStr.lastIndexOf('/') + 1)
          : idStr;
    }
  }

  public static VersionIdentifier toVersionIdentifier(URI versionId) {
    return versionId != null ? toVersionIdentifier(versionId.toString()) : null;
  }

  public static VersionIdentifier vid(String tag, String version) {
    return SemanticIdentifier.newId(tag, version);
  }

  public static VersionIdentifier toVersionIdentifier(String versionId) {
    URI uri = URI.create(versionId);
    String tag = uri.getFragment();
    String version = null;
    boolean hasTag = !Util.isEmpty(tag);

    uri = URIUtil.normalizeURI(uri);
    Matcher m = VERSIONS_RX.matcher(uri.toString());
    if (m.matches()) {
      version = m.group(3);
      if (!hasTag) {
        tag = m.group(2);
      }
    } else if (!hasTag) {
      tag = uri.toString();
      int index = tag.lastIndexOf('/');
      if (index >= 0) {
        tag = tag.substring(index + 1);
      } else {
        if (Registry.isGlobalIdentifier(tag)) {
          // TODO FIXME
          version = versionOf(uri);
          tag = tagOf(uri);
        }
      }
    }
    return SemanticIdentifier.newId(tag, version);
  }


  public static String seedUUIDentifier(String seed) {
    return UUID.nameUUIDFromBytes(seed.getBytes()).toString();
  }

  public static UUID seedUUID(String seed) {
    return UUID.nameUUIDFromBytes(seed.getBytes());
  }


  public static <T extends Term, X> Optional<T> resolveTerm(final X val, T[] values,
      Function<Term, X> getter) {
    return Arrays.stream(values)
        .filter(x -> getter.apply(x).equals(val))
        .findAny();
  }

  public static <T extends Term, X> Optional<T> resolveAliases(final X val, T[] values,
      Function<T, List<X>> getter) {
    return Arrays.stream(values)
        .filter(x -> getter.apply(x).stream().anyMatch(t -> t.equals(val)))
        .findAny();
  }

  public static <T extends Term> Map<UUID, T> indexByUUID(T[] values) {
    return Collections.unmodifiableMap(Arrays.stream(values)
        .collect(Collectors.toConcurrentMap(Term::getUuid, Function.identity())));
  }


  public static Optional<String> encodeConcept(Term trm) {
    if (trm == null) {
      return Optional.empty();
    }

    String ns = trm.getNamespaceUri() != null
        ? trm.getNamespaceUri().toString()
        : "urn:";

    String effectiveTag = trm.getUuid() != null
        ? trm.getUuid().toString()
        : trm.getTag();

    String qualifiedNs = URIUtil.normalizeURI(trm.getResourceId()).toString();

    if (Registry.isGlobalIdentifier(ns)) {
      return Optional.of(
          String.format("%s | %s |",
              trm.getResourceId().toString(),
              trm.getName()));
    } else if (ns.startsWith(qualifiedNs)) {
      return Optional.of(
          String.format("%s#%s | %s |",
              ns,
              effectiveTag,
              trm.getName()));
    } else {
      return Optional.of(
          String.format("{%s} %s#%s | %s |",
              ns,
              qualifiedNs,
              effectiveTag,
              trm.getName()));
    }
  }


  public static URI getDefaultVersionId(URI resourceId, String versionTag) {
    return getDefaultVersionId(
        resourceId,
        versionTag,
        Registry.isGlobalIdentifier(resourceId)
            ? VersionPatterns.URN_STYLE
            : VersionPatterns.KMDP_STYLE);
  }

  public static URI getDefaultVersionId(URI resourceId, String versionTag, VersionPatterns style) {
    if (versionTag == null) {
      return null;
    }
    URI uri = resourceId;
    StringBuilder sb = new StringBuilder();
    switch (style) {
      case W3C_STYLE:
        String baseUri = URIUtil.normalizeURI(uri).toString();
        int split = baseUri.lastIndexOf('/');
        sb.append(baseUri, 0, split)
            .append('/')
            .append(versionTag)
            .append(baseUri.substring(split));
        break;
      case URN_STYLE:
        sb.append(uri.toString())
            .append(":")
            .append(versionTag);
        break;
      case KMDP_STYLE:
        sb.append(URIUtil.normalizeURI(uri).toString())
            .append(getVersionSeparator(uri.toString()))
            .append(versionTag);
        if (!Util.isEmpty(uri.getFragment())) {
          sb.append("#").append(uri.getFragment());
        }
        break;
    }
    return URI.create(sb.toString());
  }
}
