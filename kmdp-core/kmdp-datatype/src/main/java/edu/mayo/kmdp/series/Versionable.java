package edu.mayo.kmdp.series;

import java.util.Comparator;
import java.util.Date;
import java.util.Optional;
import org.omg.spec.api4kp._20200801.id.VersionIdentifier;

public interface Versionable<T extends Versionable<T>> {

  default void dub(VersionIdentifier identifier) {
    if (getVersionIdentifier() != null) {
      throw new IllegalStateException("Unable to assign new identifier "
          + identifier + " to an already identified entity " + getVersionIdentifier());
    }
  }

  VersionIdentifier getVersionIdentifier();

  @SuppressWarnings("unchecked")
  default T snapshot() {
    return (T) this;
  }

  default boolean isVersionExpired() {
    return false;
  }

  default Optional<Date> getVersionExpiredOn () {
    return Optional.empty();
  }

  Date getVersionEstablishedOn();

  static <T extends Versionable<T>> Comparator<T> mostRecentFirstComparator() {
    Comparator<T> c = Comparator.comparing(v -> v.getVersionIdentifier().getEstablishedOn());
    return c.reversed();
  }

  static <T extends Versionable<T>> Comparator<T> highestVersionFirstComparator() {
    Comparator<T> c = Comparator.comparing(v -> v.getVersionIdentifier().getVersionTag());
    return c.reversed();
  }
}
