package org.omg.spec.api4kp._20200801.terms;

import java.util.Collection;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.series.Series;
import org.omg.spec.api4kp._20200801.series.Versionable;

public interface VersionableTerm<T extends Term & Versionable<T>,E extends Enum<E>>
    extends ConceptTerm<T>, Versionable<T> {

  E asEnum();

  Series<T> asSeries();


  default boolean isSame(Term other) {
    return isSameVersion(other);
  }

  default boolean isAnyOf(Collection<? extends Term> others) {
    return others != null && others.stream().anyMatch(this::isSame);
  }

  default boolean isNoneOf(Collection<? extends Term> others) {
    return ! isAnyOf(others);
  }

  default boolean sameAs(Term other) {
    return isSameVersion(other);
  }

  default boolean isSameVersion(Term other) {
    return other != null
        && this.getUuid().equals(other.getUuid())
        && this.getVersionTag().equals(other.getVersionTag());
  }

  default boolean isDifferentVersion(Term other) {
    return other != null
        && this.getUuid().equals(other.getUuid())
        && ! this.getVersionTag().equals(other.getVersionTag());
  }

  default boolean isSameEntity(Term other) {
    return other != null && this.getUuid().equals(other.getUuid());
  }

}
