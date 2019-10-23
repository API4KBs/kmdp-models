package edu.mayo.kmdp.id;

import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;

public interface Series<T extends Versionable> extends Versionable {

  static boolean isSame(Versionable v0, Versionable v1) {
    if (v0 == null || v0.getVersionIdentifier() == null
        || v0.getVersionIdentifier().getTag() == null
        || v1 == null || v1.getVersionIdentifier() == null) {
      return false;
    }
    return v0.getVersionIdentifier().getTag().equals(v1.getVersionIdentifier().getTag());
  }

  static boolean isDifferentVersion(Versionable v0, Versionable v1) {
    if (!isSame(v0,v1)) {
      return false;
    }
    String ver1 = v0.getVersionIdentifier().getVersion();
    String ver2 = v1.getVersionIdentifier().getVersion();
    return ver1 != null && ver2 != null && ! ver1.equals(ver2);
  }

  default T asOf(Date d) {
    throw new UnsupportedOperationException();
    //return getVersions()[0];
  }

  default Optional<T> getVersion(String versionTag) {
    return getVersions().stream()
        .filter(v -> versionTag.equals(v.getVersionIdentifier().getVersion()))
        .findAny();
  }

  default Optional<T> getVersion(int index) {
    if (index < 0 || index >= getVersions().size()) {
      return Optional.empty();
    }
    return Optional.ofNullable(getVersions().get(index));
  }

  default boolean isEmpty() {
    return getVersions().isEmpty();
  }

  default T getLatest() {
    return latest().orElse(null);
  }

  default Optional<T> latest() {
    return isEmpty() ? Optional.empty() : Optional.of(getVersions().get(0));
  }

  default Optional<String> getLatestVersionTag() {
    return latest()
        .map(Versionable::getVersionIdentifier)
        .map(VersionedIdentifier::getVersion);
  }

  default VersionedIdentifier getVersionIdentifier() {
    return latest()
        .map(Versionable::getVersionIdentifier)
        .orElse(null);
  }

  default List<T> getVersions() {
    return Collections.emptyList();
  }

  static URIIdentifier toVersion(URIIdentifier seriesId, URI version) {
    return ((URIIdentifier) seriesId.clone())
        .withVersionId(version);
  }

}
