package edu.mayo.kmdp.series;

import edu.mayo.kmdp.id.SemVerIdentifier;
import java.util.Comparator;

public interface SemVersionable<T extends Versionable<T>> extends Versionable<T> {

  @Override
  SemVerIdentifier getVersionIdentifier();

  static <T extends SemVersionable<T>> Comparator<T> highestVersionFirstComparator() {
    Comparator<T> c = Comparator.comparing(v -> v.getVersionIdentifier().getSemanticVersion());
    return c.reversed();
  }

}
