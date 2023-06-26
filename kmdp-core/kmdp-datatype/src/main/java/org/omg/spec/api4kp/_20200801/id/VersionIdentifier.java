package org.omg.spec.api4kp._20200801.id;


import static edu.mayo.kmdp.registry.Registry.DID_URN;
import static edu.mayo.kmdp.registry.Registry.DID_URN_URI;
import static org.omg.spec.api4kp._20200801.id.IdentifierConstants.SEMVER_FULL;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.zafarkhaja.semver.UnexpectedCharacterException;
import com.github.zafarkhaja.semver.Version;
import edu.mayo.kmdp.util.DateTimeUtil;
import java.net.URI;
import java.util.regex.Matcher;

/**
 * Handle versionIdentifier data.
 */
public interface VersionIdentifier extends Identifier {

  static String toSemVer(String versionTag) {
    if (versionTag == null) {
      return null;
    }
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
              Integer.parseInt(versionTag.substring(0, idx)),
              Integer.parseInt(versionTag.substring(idx + 1)))
          .toString();
    }
    int dashIdx = versionTag.indexOf('-');
    if (dashIdx >= 0) {
      return toSemVer(versionTag.substring(0, dashIdx)) + "-" + versionTag.substring(dashIdx + 1);
    }
    return IdentifierConstants.VERSION_ZERO + "-" + versionTag;
  }

  static Version semVerOf(String versionTag) {
    if (versionTag == null) {
      return null;
    }
    try {
      return Version.valueOf(versionTag);
    } catch (UnexpectedCharacterException ue) {
      try {
        return Version.valueOf(toSemVer(versionTag));
      } catch (Exception e) {
        return null;
      }
    }
  }

  static Version semVerOf(VersionIdentifier vid) {
    if (vid == null) {
      return null;
    }
    return semVerOf(vid.getVersionTag());
  }

  /**
   * Predicate
   * @param vid
   * @return true if vid denotes an anonymous SNAPSHOT
   */
  static boolean isSnapshot(VersionIdentifier vid) {
    return vid != null && isSnapshot(vid.getVersionTag());
  }

  /**
   * Predicate
   * @param versionTag
   * @return true if versionTag denotes an anonymous SNAPSHOT
   */
  static boolean isSnapshot(String versionTag) {
    return versionTag != null && versionTag.endsWith(IdentifierConstants.SNAPSHOT);
  }

  /**
   * Predicate
   * @param vid
   * @return true if vid denotes a stable, non-SNAPSHOT version
   */
  static boolean isStable(VersionIdentifier vid) {
    return !isSnapshot(vid);
  }

  /**
   * Predicate
   * @param versionTag
   * @return true if versionTag denotes a stable, non-SNAPSHOT version
   */
  static boolean isStable(String versionTag) {
    return !isSnapshot(versionTag);
  }

  URI getVersionId();

  String getVersionTag();

  @JsonIgnore
  default Version getSemanticVersionTag() {
    return semVerOf(this);
  }

  /**
   * return the version type based on the format of the string
   *
   * @return VersionTagType enum for the format
   */
  @JsonIgnore
  default VersionTagType getVersionFormat() {
    String versionTag = getVersionTag();
    return detectVersionTag(versionTag);
  }

  /**
   * Assuming a versionTag is provided, detects the format
   *
   * @param versionTag
   * @return
   */
  static VersionTagType detectVersionTag(String versionTag) {
    Matcher matcher = SEMVER_FULL.matcher(versionTag);
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
    return id.startsWith(DID_URN)
        ? ":"
        : "/versions/";
  }

}
