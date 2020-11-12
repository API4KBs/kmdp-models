package org.omg.spec.api4kp._20200801.series;

import java.util.Comparator;
import org.omg.spec.api4kp._20200801.id.Identifiable;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;

public interface SemVersionable<T extends Versionable<T,E>,E extends Identifiable> extends Versionable<T,E> {

  @Override
  ResourceIdentifier getVersionIdentifier();

  static <T extends SemVersionable<T,E>, E extends Identifiable> Comparator<T> highestVersionFirstComparator() {
    Comparator<T> c = Comparator.comparing(v -> v.getVersionIdentifier().getSemanticVersionTag());
    return c.reversed();
  }

}
