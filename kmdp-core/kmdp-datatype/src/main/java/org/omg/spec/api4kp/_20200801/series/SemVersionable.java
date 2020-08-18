package org.omg.spec.api4kp._20200801.series;

import java.util.Comparator;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;

public interface SemVersionable<T extends Versionable<T>> extends Versionable<T> {

  @Override
  ResourceIdentifier getVersionIdentifier();

  static <T extends SemVersionable<T>> Comparator<T> highestVersionFirstComparator() {
    Comparator<T> c = Comparator.comparing(v -> v.getVersionIdentifier().getSemanticVersionTag());
    return c.reversed();
  }

}
