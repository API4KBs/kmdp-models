package org.omg.spec.api4kp._1_0.id;

import static edu.mayo.kmdp.registry.Registry.BASE_UUID_URN;
import static org.omg.spec.api4kp._1_0.id.IdentifierConstants.SEMVER_RX;
import static org.omg.spec.api4kp._1_0.id.IdentifierConstants.VERSION_LATEST;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.zafarkhaja.semver.Version;
import edu.mayo.kmdp.util.DateTimeUtil;
import java.net.URI;
import java.util.UUID;
import java.util.regex.Matcher;

/**
 * Handle versionIdentifier data.
 */
public interface VersionIdentifier extends Identifier {

  String getVersionTag();

  @JsonIgnore
  default Version getSemanticVersionTag() {
    return Version.valueOf(getVersionTag());
  }

  @JsonIgnore
  default URI getVersionId() {
    URI uri = getResourceId();
    return URI.create(uri + getVersionSeparator(uri.toString()) + getVersionTag());
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
    if (DateTimeUtil.isDate(versionTag)) {
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

  @JsonIgnore
  default UUID getVersionUuid() {
    byte[] r = this.getResourceId().toString().getBytes();
    byte[] v = (this.getVersionTag() != null)
        ? this.getVersionTag().getBytes()
        : VERSION_LATEST.getBytes();
    byte[] x = new byte[r.length + v.length];
    System.arraycopy(r, 0, x, 0, r.length);
    System.arraycopy(v, 0, x, r.length, v.length);
    return UUID.nameUUIDFromBytes(x);
  }

}
