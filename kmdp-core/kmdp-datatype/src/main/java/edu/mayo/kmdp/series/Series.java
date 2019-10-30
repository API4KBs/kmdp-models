package edu.mayo.kmdp.series;

import edu.mayo.kmdp.id.VersionedIdentifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.VersionIdentifier;

public interface Series<T extends Versionable<T>> {

  String SNAPSHOT = "SNAPSHOT";
  String SNAPSHOT_DATE_PATTERN = "yyyyMMdd-HHmmSS";

  static boolean isSame(Versionable v0, Versionable v1) {
    if (v0 == null || v0.getVersionIdentifier() == null
        || v0.getVersionIdentifier().getTag() == null
        || v1 == null || v1.getVersionIdentifier() == null) {
      return false;
    }
    return v0.getVersionIdentifier().getTag().equals(v1.getVersionIdentifier().getTag());
  }

  static boolean isDifferentVersion(Versionable v0, Versionable v1) {
    if (!isSame(v0, v1)) {
      return false;
    }
    String ver1 = v0.getVersionIdentifier().getVersion();
    String ver2 = v1.getVersionIdentifier().getVersion();
    return ver1 != null && ver2 != null && !ver1.equals(ver2);
  }

  default Optional<T> asOf(Date d) {
    return getVersions().stream()
        .filter(v -> d.compareTo(v.getVersionIdentifier().getEstablishedOn()) >= 0)
        .findFirst();
  }

  default Optional<T> getVersion(String versionTag) {
    return getVersions().stream()
        .filter(v -> versionTag.equals(v.getVersionIdentifier().getVersion()))
        .findAny();
  }

  default boolean hasVersion(String versionTag) {
    return getVersion(versionTag).isPresent();
  }


  default Optional<T> getVersion(int index) {
    if (index < 0 || index >= getVersions().size()) {
      return Optional.empty();
    }
    return Optional.ofNullable(getVersions().get(index));
  }

  default void addVersion(T newSnapshot, VersionedIdentifier id) {
    newSnapshot.dub(id);

    if (!isEmpty() && !isSame(getLatest(), newSnapshot)) {
      throw new IllegalArgumentException("Adding " + id.getTag()
          + " would violate the Series identity principle - series id = " + getLatest()
          .getVersionIdentifier().getTag());
    }
    if (hasVersion(id.getVersion())) {
      throw new IllegalArgumentException("Version already present");
    }

    this.getVersions().add(0,newSnapshot);
    this.getVersions()
        .sort(Versionable.mostRecentFirstComparator());
  }

  default T evolve(UnaryOperator<T> mutator, String newVersionTag, Date d) {
    T latest = getLatest();
    T next = mutator.apply(latest.snapshot());
    addVersion(next,
        newIdentifier(latest.getVersionIdentifier().getTag(), newVersionTag, d));
    return next;
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

  default List<T> getVersions() {
    return Collections.emptyList();
  }

  default List<T> sortedByVersion() {
    List<T> list = new LinkedList<>(getVersions());
    list.sort(Versionable.highestVersionFirstComparator());
    return list;
  }

  static URIIdentifier toVersion(URIIdentifier seriesId, URI version) {
    return ((URIIdentifier) seriesId.clone())
        .withVersionId(version);
  }

  default VersionedIdentifier newIdentifier(String tag) {
    return newIdentifier(tag, "0.0.0");
  }

  default VersionedIdentifier newIdentifier(String tag, String versionTag) {
    return newIdentifier(tag, versionTag, new Date());

  }

  default VersionedIdentifier newIdentifier(String tag, String versionTag, Date d) {
    return new VersionIdentifier()
        .withTag(tag)
        .withVersion(versionTag)
        .withEstablishedOn(d);
  }

}
