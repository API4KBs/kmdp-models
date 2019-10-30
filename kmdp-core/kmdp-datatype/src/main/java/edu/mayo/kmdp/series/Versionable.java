package edu.mayo.kmdp.series;

import edu.mayo.kmdp.id.VersionedIdentifier;
import java.util.Comparator;

public interface Versionable<T extends Versionable<T>> {

  default void dub(VersionedIdentifier identifier) {
    if (getVersionIdentifier() != null) {
      throw new IllegalStateException("Unable to assign new identifier "
          + identifier + " to an already identified entity " + getVersionIdentifier());
    }
  }

  VersionedIdentifier getVersionIdentifier();

  default T snapshot() {
    return (T) this;
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
