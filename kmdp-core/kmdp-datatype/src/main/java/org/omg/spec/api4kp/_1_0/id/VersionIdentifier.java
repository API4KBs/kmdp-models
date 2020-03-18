package org.omg.spec.api4kp._1_0.id;

import static edu.mayo.kmdp.registry.Registry.BASE_UUID_URN;

import com.github.zafarkhaja.semver.Version;
import edu.mayo.kmdp.util.DateTimeUtil;
import java.net.URI;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.omg.spec.api4kp._1_0.id.VersionTagType;

public interface VersionIdentifier extends Identifier {

  String VERSIONS = "/versions/";
  Pattern SEMVER_RX = Pattern.compile("^(\\d+\\.)?(\\d+\\.)?(\\*|\\d+)$");

  String getVersionTag();

  default Version getSemanticVersionTag() {
    return Version.valueOf(getVersionTag());
  }

  default URI getVersionId() {
    URI uri = getResourceId();
    return URI.create(uri + getVersionSeparator(uri.toString()) + getVersionTag());
  }

  URI getNamespace();

  UUID getUuid();

  // TODO: What is URI for version formats???
  default URI getVersionFormat() {
    String tag = getTag();
    if (tag.matches("\\d+")) {
      System.out.println("is " + VersionTagType.SEQUENTIAL);
      return null;
    }
    if (DateTimeUtil.isDate(tag)) {
      System.out.println("is " + VersionTagType.TIMESTAMP);
      return null;
    }
    Matcher matcher = SEMVER_RX.matcher(tag);
    if (matcher.matches()) {
      System.out.println("is " + VersionTagType.SEM_VER);
      return null;
    } else {
      System.out.println("is " + VersionTagType.GENERIC);
      return null;
    }
  }

  /**
   * Returns the default id/version separator based on the URI pattern urn:uuid use ":", while
   * http-based URIs use "/versions"
   *
   * @param id the URI for which to get the id/version separator
   * @return the string used to separate the id from the version tag for this kind of id
   */
  default String getVersionSeparator(String id) {
    return id.startsWith(BASE_UUID_URN)
        ? ":"
        : "/versions/";
  }

}
