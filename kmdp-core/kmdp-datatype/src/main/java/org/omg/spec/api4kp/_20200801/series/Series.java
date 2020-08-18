package org.omg.spec.api4kp._20200801.series;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.id.VersionIdentifier;
import org.omg.spec.api4kp._20200801.series.Versionable;

public interface Series<T extends Versionable<T>> {

   static boolean isSame(Versionable<?> v0, Versionable<?> v1) {
     return isSameVersion(v0, v1);
   }

   static boolean isSameEntity(Versionable<?> v0, Versionable<?> v1) {
    if (v0 == null || v0.getVersionIdentifier().getTag() == null || v1 == null ) {
      return false;
    }
    return v0.getVersionIdentifier().getTag().equals(v1.getVersionIdentifier().getTag());
  }

  static boolean isSameVersion(Versionable<?> v0, Versionable<?> v1) {
    if (!isSameEntity(v0, v1)) {
      return false;
    }
    String ver1 = v0.getVersionIdentifier().getVersionTag();
    String ver2 = v1.getVersionIdentifier().getVersionTag();
    return ver1 != null && ver1.equals(ver2);
  }

  static boolean isDifferentVersion(Versionable<?> v0, Versionable<?> v1) {
    if (!isSameEntity(v0, v1)) {
      return false;
    }
    String ver1 = v0.getVersionIdentifier().getVersionTag();
    String ver2 = v1.getVersionIdentifier().getVersionTag();
    return ver1 != null && ver2 != null && !ver1.equals(ver2);
  }

  default Optional<T> asOf(Date d) {
    boolean expired = this.getSeriesExpiredOn()
        .map(exp -> d.compareTo(exp) >= 0)
        .orElse(false);

    return expired
        ? Optional.empty()
        : getVersions().stream()
            .filter(v -> d.compareTo(v.getVersionEstablishedOn()) >= 0)
            .findFirst();
  }

  default Optional<T> getVersion(String versionTag) {
    return getVersions().stream()
        .filter(v -> versionTag.equals(v.getVersionIdentifier().getVersionTag()))
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

  default void addVersion(T newSnapshot, VersionIdentifier id) {
    newSnapshot.dub(id);

    if (!isEmpty() && !isSameEntity(getLatest(), newSnapshot)) {
      throw new IllegalArgumentException("Adding " + id.getTag()
          + " would violate the Series identity principle - series id = " + getLatest()
          .getVersionIdentifier().getTag());
    }
    if (hasVersion(id.getVersionTag())) {
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

  default T getEarliest() {
    return earliest().orElse(null);
  }

  default Optional<T> earliest() {
    List<T> versions = getVersions();
    return isEmpty() ? Optional.empty() : Optional.of(versions.get(versions.size() - 1));
  }

  default Date getSeriesEstablishedOn() {
    return getEarliest().getVersionEstablishedOn();
  }

  default Optional<Date> getSeriesExpiredOn() {
    return Optional.empty();
  }

  default boolean isSeriesExpired() {
    return getLatest().isVersionExpired();
  }

  default List<T> getVersions() {
    return Collections.emptyList();
  }

  default List<T> sortedByVersion() {
    List<T> list = new LinkedList<>(getVersions());
    list.sort(Versionable.highestVersionFirstComparator());
    return list;
  }

  static ResourceIdentifier toVersion(ResourceIdentifier seriesId, String versionTag) {
    return ((ResourceIdentifier) seriesId.clone())
        .withVersionTag(versionTag);
  }

  default VersionIdentifier newIdentifier(String tag) {
    return newIdentifier(tag, "0.0.0");
  }

  default VersionIdentifier newIdentifier(String tag, String versionTag) {
    return newIdentifier(tag, versionTag, new Date());

  }

  default ResourceIdentifier newIdentifier(String tag, String versionTag, Date d) {
    return SemanticIdentifier.newId(tag, versionTag)
        .withEstablishedOn(d);
  }

}
