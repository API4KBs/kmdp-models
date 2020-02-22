/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp.id.helper;


import static edu.mayo.kmdp.registry.Registry.BASE_UUID_URN;
import static edu.mayo.kmdp.util.Util.ensureUUIDFormat;

import edu.mayo.kmdp.id.Identifier;
import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.id.VersionedIdentifier;
import edu.mayo.kmdp.id.adapter.URIId;
import edu.mayo.kmdp.registry.Registry;
import edu.mayo.kmdp.util.DateTimeUtil;
import edu.mayo.kmdp.util.NameUtils;
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
import javax.xml.namespace.QName;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.Pointer;
import org.omg.spec.api4kp._1_0.identifiers.QualifiedIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.UUIDentifier;
import org.omg.spec.api4kp._1_0.identifiers.VersionIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.VersionTagType;

public class DatatypeHelper {

  protected DatatypeHelper() {

  }

  private static final Pattern VERSIONS_RX = Pattern.compile("^(.*/)?(.*)/versions/(.+)$");
  private static final Pattern SEMVER_RX = Pattern.compile("^(\\d+\\.)?(\\d+\\.)?(\\*|\\d+)$");


  public static NamespaceIdentifier ns(final String nsUri) {
    return new NamespaceIdentifier().withId(URI.create(nsUri));
  }

  public static NamespaceIdentifier ns(final String nsUri, String label) {
    return new NamespaceIdentifier().withId(URI.create(nsUri)).withLabel(label);
  }

  public static NamespaceIdentifier ns(final String nsUri, String label, String version) {
    return new NamespaceIdentifier().withId(URI.create(nsUri))
        .withLabel(label)
        .withVersion(version);
  }

  public static ConceptIdentifier trm(final String termUri) {
    return trm(termUri, null, termUri);
  }

  public static ConceptIdentifier trm(final String termUri, final String label) {
    return trm(termUri, label, termUri);
  }

  public static ConceptIdentifier trm(final String termUri, final String label,
      final String refUri) {
    URI u = ensureResolved(termUri);
    String l = ensureLabel(label, u);
    URI r = Util.isEmpty(refUri) ? u : ensureResolved(refUri);
    return new ConceptIdentifier()
        .withConceptId(u)
        .withLabel(l)
        .withRef(r);
  }

  private static String ensureLabel(String label, URI u) {
    if (!Util.isEmpty(label)) {
      return label;
    }
    return Util.isEmpty(u.getFragment())
        ? u.getPath().substring(u.getPath().lastIndexOf('/') + 1)
        : u.getFragment();
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

  public static URIIdentifier uri(final String id, final String versionTag) {
    return new URIIdentifier()
        .withUri(URI.create(id))
        .withVersionId(
            Util.isEmpty(versionTag) ? null : URI.create(id + getVersionSeparator(id) + versionTag));
  }

  /**
   * Returns the default id/version separator based on the URI pattern
   * urn:uuid use ":", while http-based URIs use "/versions"
   * @param id the URI for which to get the id/version separator
   * @return the string used to separate the id from the version tag for this kind of id
   */
  public static String getVersionSeparator(String id) {
    return id.startsWith(BASE_UUID_URN)
        ? ":"
        : "/versions/";
  }


  public static URIIdentifier uri(String base, String id, String versionTag) {
    boolean needsSeparator = !(base.endsWith("#") || base.endsWith("/") || base.endsWith(":"));
    return uri(base + (needsSeparator ? "/" : "") + id, versionTag);
  }

  public static URIIdentifier vuri(final String uri, final String versionUri) {
    return
        new URIIdentifier()
            .withUri(URI.create(uri))
            .withVersionId(versionUri != null ? URI.create(versionUri) : null);
  }

  public static VersionedIdentifier deRef(Pointer ptr) {
    return toVersionIdentifier(ptr.getEntityRef());
  }

  public static URIIdentifier uri(final String id) {
    return uri(id, null);
  }

  public static VersionTagType tag(final String tag) {
    if (tag.matches("\\d+")) {
      return VersionTagType.SEQUENTIAL;
    }
    if (DateTimeUtil.isDate(tag)) {
      return VersionTagType.TIMESTAMP;
    }
    Matcher matcher = SEMVER_RX.matcher(tag);
    if (matcher.matches()) {
      return VersionTagType.SEM_VER;
    } else {
      return VersionTagType.GENERIC;
    }
  }

  public static QualifiedIdentifier name(final String n) {
    String pfx = n.substring(0, n.indexOf(':'));
    String name = n.substring(n.indexOf(':') + 1);
    String uri = Registry.getNamespaceURIForPrefix(pfx).orElse("");

    return new QualifiedIdentifier().withQName(new QName(uri, name, pfx));
  }

  public static String versionOf(URI versionedIdentifier) {
    // TODO fixme...
    return versionOf(new URIIdentifier().withUri(versionedIdentifier));
  }

  public static String versionOf(URIId versionedIdentifier) {
    if (versionedIdentifier == null) {
      return null;
    }
    String idStr = versionedIdentifier.getVersionId() != null
        ? versionedIdentifier.getVersionId().toString()
        : versionedIdentifier.getUri().toString();

    if (idStr.startsWith(BASE_UUID_URN)) {
      int start = BASE_UUID_URN.length();
      int end = idStr.lastIndexOf(':');
      // need at least two ":"
      if (end > start) {
        return idStr.substring(end + 1);
      }
    }

    String ver = null;
    VersionedIdentifier vid = toVersionIdentifier(versionedIdentifier.getVersionId());
    if (vid != null) {
      ver = vid.getVersion();
    }
    // still open for improvement...
    if (ver == null) {
      ver = NameUtils.strip(versionedIdentifier.getUri().toString(),
          versionedIdentifier.getVersionId().toString(),
          '/', '#');
    }
    return ver;
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
    if (idStr.startsWith(BASE_UUID_URN)) {
      int start = BASE_UUID_URN.length();
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


  public static VersionIdentifier toVersionIdentifier(URIIdentifier uri) {
    return uri != null
        ? toVersionIdentifier(tryGetVersionedUri(uri))
        : null;
  }

  public static URI tryGetVersionedUri(URIIdentifier uri) {
    return uri.getVersionId() != null ? uri.getVersionId() : uri.getUri();
  }

  public static VersionIdentifier toVersionIdentifier(URI versionId) {
    return versionId != null ? toVersionIdentifier(versionId.toString()) : null;
  }

  public static VersionIdentifier vid(String tag, String version) {
    return new VersionIdentifier()
        .withTag(tag)
        .withVersion(version);
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
        if (tag.startsWith(BASE_UUID_URN)) {
          // TODO FIXME
          version = versionOf(uri);
          tag = tagOf(uri);
        }
      }
    }
    return new VersionIdentifier().withTag(tag).withVersion(version);
  }

  public static QualifiedIdentifier toQualifiedIdentifier(URIIdentifier id) {
    return new QualifiedIdentifier().withQName(URIUtil.toQName(id.getVersionId() != null
        ? id.getVersionId()
        : id.getUri())
        .orElseThrow(IllegalArgumentException::new));
  }

  public static QualifiedIdentifier toQualifiedIdentifier(ConceptIdentifier id) {
    return new QualifiedIdentifier().withQName(URIUtil.toQName(id.getRef())
        .orElseThrow(IllegalArgumentException::new));
  }

  public static QualifiedIdentifier toQualifiedIdentifier(URI id) {
    return new QualifiedIdentifier().withQName(URIUtil.toQName(id)
        .orElseThrow(IllegalArgumentException::new));
  }

  public static String toPrefixedName(QualifiedIdentifier qId) {
    return URIUtil.toPrefixedName(qId.getQName());
  }

  public static Optional<UUIDentifier> toUUIDentifier(Identifier cid) {
    return ensureUUIDFormat(cid.getTag())
        .map(uuidStr -> new UUIDentifier().withTag(uuidStr));
  }

  public static Optional<URIIdentifier> toURIIDentifier(UUIDentifier uid) {
    UUID uuid = uid.getUUID();
    if (uuid != null) {
      return Optional
          .ofNullable(new URIIdentifier().withUri(URI.create(BASE_UUID_URN + uuid.toString())));
    } else {
      return Optional.empty();
    }
  }

  /**
   * Create a URIIdentifier from the versionedId in the String. If the versionedId is not
   * actually versioned, create URIIdentifer with null version.
   *
   * @param versionedId URI String
   * @return URIIdentifier from the versionedId
   */
  public static URIIdentifier toURIIDentifier(String versionedId) {
    Matcher m = VERSIONS_RX.matcher(versionedId);
    if (m.matches()) {
      return vuri(m.group(1) + m.group(2), versionedId);
    } else {
      return vuri(versionedId, null);
    }
  }

  public static String seedUUIDentifier(String seed) {
    return UUID.nameUUIDFromBytes(seed.getBytes()).toString();
  }

  public static UUID seedUUID(String seed) {
    return UUID.nameUUIDFromBytes(seed.getBytes());
  }

  public static NamespaceIdentifier toNamespace(URIIdentifier schemeURI, String label) {
    URI base = schemeURI.getUri();

    return new NamespaceIdentifier()
        .withId(URIUtil.normalizeURI(base))
        .withLabel(label)
        .withTag(base.getFragment())
        .withVersion(DatatypeHelper.versionOf(schemeURI));
  }


  public static ConceptIdentifier toConceptIdentifier(Term v) {
    if (v == null) {
      return null;
    }
    if (v instanceof ConceptIdentifier) {
      return (ConceptIdentifier) v;
    }
    return new org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier()
        .withRef(v.getRef())
        .withConceptUUID(v.getConceptUUID())
        .withLabel(v.getLabel())
        .withTag(v.getTag())
        .withConceptId(v.getConceptId())
        .withNamespace((NamespaceIdentifier) ((NamespaceIdentifier) v.getNamespace()).clone());
  }

  public static ConceptIdentifier toUnqualifiedConceptIdentifier(Term v) {
    if (v == null) {
      return null;
    }
    if (v instanceof ConceptIdentifier) {
      return (ConceptIdentifier) v;
    }
    return new org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier()
        .withConceptUUID(v.getConceptUUID())
        .withLabel(v.getLabel())
        .withTag(v.getTag())
        .withConceptId(v.getConceptId());
  }


  public static <T extends Term, X> Optional<T> resolveTerm(final X val, T[] values,
      Function<Term, X> getter) {
    return Arrays.stream(values)
        .filter(x -> val.equals(getter.apply(x)))
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
        .collect(Collectors.toConcurrentMap(Term::getConceptUUID, Function.identity())));
  }


  public static Optional<String> encodeConcept(Term trm) {
    if (trm == null) {
      return Optional.empty();
    }

    String ns = trm.getNamespace() != null
        ? ((NamespaceIdentifier) trm.getNamespace()).getId().toString()
        : "urn:";

    String effectiveTag = trm.getConceptUUID() != null
        ? trm.getConceptUUID().toString()
        : trm.getTag();

    String qualifiedNs = URIUtil.normalizeURI(trm.getConceptId()).toString();

    if (ns.startsWith(qualifiedNs)) {
      return Optional.of(
          String.format("%s#%s | %s |",
              ns,
              effectiveTag,
              trm.getLabel()));
    } else {
      return Optional.of(
          String.format("{%s} %s#%s | %s |",
              ns,
              qualifiedNs,
              effectiveTag,
              trm.getLabel()));
    }
  }
}
