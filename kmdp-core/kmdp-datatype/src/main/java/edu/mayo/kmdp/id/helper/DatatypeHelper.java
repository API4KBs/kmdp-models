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
package edu.mayo.kmdp.id.helper;


import static edu.mayo.kmdp.util.Util.ensureUUIDFormat;

import edu.mayo.kmdp.id.VersionedIdentifier;
import edu.mayo.kmdp.registry.Registry;
import edu.mayo.kmdp.util.URIUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.kmdp.util.adapters.DateAdapter;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    return trm(termUri,null,termUri);
  }

  public static ConceptIdentifier trm(final String termUri, final String label) {
    return trm(termUri,label,termUri);
  }

  public static ConceptIdentifier trm(final String termUri, final String label, final String refUri) {
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
            Util.isEmpty(versionTag) ? null : URI.create(id + "/versions/" + versionTag));
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
    if (DateAdapter.instance().isDate(tag)) {
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

  public static String versionOf(URI versionedIdentifier, URI identifier) {
    if (versionedIdentifier == null) {
      return null;
    }
    // Can probably be refactored to be more efficient...
    return toVersionIdentifier(versionedIdentifier).getVersion();
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
        // Not sure what to do in this case, probably nothing...
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

  public static Optional<UUIDentifier> toUUIDentifier(ConceptIdentifier cid) {
    return ensureUUIDFormat(cid.getTag())
        .map(uuidStr -> new UUIDentifier().withTag(uuidStr));
  }

  public static Optional<URIIdentifier> toURIIDentifier(UUIDentifier uid) {
    UUID uuid = uid.getUUID();
    if (uuid != null) {
      return Optional
          .ofNullable(new URIIdentifier().withUri(URI.create(Registry.BASE_UUID_URN + uuid.toString())));
    } else {
      return Optional.empty();
    }
  }

  public static String seedUUIDentifier(String seed) {
    return UUID.nameUUIDFromBytes(seed.getBytes()).toString();
  }

  public static UUID seedUUID(String seed) {
    return UUID.nameUUIDFromBytes(seed.getBytes());
  }
}
