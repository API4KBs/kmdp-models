package edu.mayo.kmdp.series;

import edu.mayo.kmdp.id.VersionedIdentifier;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;

public interface Versionable<T extends Versionable<T>> {

  default void dub(VersionedIdentifier identifier) {
    if (getVersionIdentifier() != null) {
      throw new IllegalStateException("Unable to assign new identifier "
          + identifier + " to an already identified entity " + getVersionIdentifier());
    }
  }

  VersionedIdentifier getVersionIdentifier();

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

  default Date getVersionEstablishedOn() {
    Date d0 = getVersionIdentifier().getEstablishedOn();
    return d0 != null ? d0 : new Date(0);
  }

  static <T extends Versionable<T>> Comparator<T> mostRecentFirstComparator() {
    Comparator<T> c = Comparator.comparing(v -> v.getVersionIdentifier().getEstablishedOn());
    return c.reversed();
  }

  static <T extends Versionable<T>> Comparator<T> highestVersionFirstComparator() {
    Comparator<T> c = Comparator.comparing(v -> v.getVersionIdentifier().getVersion());
    return c.reversed();
  }
}
