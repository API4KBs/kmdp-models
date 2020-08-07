package org.omg.spec.api4kp._1_0.id;

import static edu.mayo.kmdp.registry.Registry.BASE_UUID_URN;
import static org.omg.spec.api4kp._1_0.id.IdentifierConstants.SEMVER_RX;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.zafarkhaja.semver.UnexpectedCharacterException;
import com.github.zafarkhaja.semver.Version;
import edu.mayo.kmdp.util.DateTimeUtil;
import edu.mayo.kmdp.util.URIUtil;
import edu.mayo.kmdp.util.Util;
import java.net.URI;
import java.util.regex.Matcher;

/**
 * Handle versionIdentifier data.
 */
public interface VersionIdentifier extends Identifier {

  static String toSemVer(String versionTag) {
    Matcher matcher = IdentifierConstants.SEMVER_FULL.matcher(versionTag);
    if (matcher.matches()) {
      return versionTag;
    }
    if (versionTag.matches("\\d+")) {
      return Version.forIntegers(Integer.parseInt(versionTag)).toString();
    }
    if (versionTag.matches("(\\d+)\\.(\\d+)")) {
      int idx = versionTag.indexOf('.');
      return Version.forIntegers(
          Integer.parseInt(versionTag.substring(0,idx)),
              Integer.parseInt(versionTag.substring(idx+1)))
          .toString();
    }
    return IdentifierConstants.VERSION_ZERO + "-" + versionTag;
  }

  String getVersionTag();

  @JsonIgnore
  default Version getSemanticVersionTag() {
    try {
      return Version.valueOf(getVersionTag());
    } catch (UnexpectedCharacterException ue) {
      return Version.valueOf(toSemVer(getVersionTag()));
    }
  }

  @JsonIgnore
  default URI getVersionId() {
    if (getVersionTag() == null) {
      return null;
    }
    URI uri = getResourceId();
    StringBuilder sb = new StringBuilder();
    sb.append(URIUtil.normalizeURI(uri).toString())
        .append(getVersionSeparator(uri.toString()))
        .append(getVersionTag());
    if (!Util.isEmpty(uri.getFragment())) {
      sb.append("#").append(uri.getFragment());
    }
    return URI.create(sb.toString());
  }

  /**
   * return the version type based on the format of the string
   *
   * @return VersionTagType enum for the format
   */
  @JsonIgnore
  default VersionTagType getVersionFormat() {
    String versionTag = getVersionTag();
    Matcher matcher = SEMVER_RX.matcher(versionTag);
    if (matcher.matches()) {
      return VersionTagType.SEM_VER;
    }
    if (versionTag.matches("\\d+")) {
      return VersionTagType.SEQUENTIAL;
    }
    if (DateTimeUtil.validateDate(versionTag)) {
      return VersionTagType.TIMESTAMP;
    } else {
      return VersionTagType.GENERIC;
    }
  }

  /**
   * Returns the default id/version separator based on the URI pattern urn:uuid use ":", while
   * http-based URIs use "/versions"
   *
   * @param id the URI for which to get the id/version separator
   * @return the string used to separate the id from the version tag for this kind of id
   */
  @JsonIgnore
  default String getVersionSeparator(String id) {
    return id.startsWith(BASE_UUID_URN)
        ? ":"
        : "/versions/";
  }


}
