package edu.mayo.kmdp.terms;

import edu.mayo.kmdp.series.Series;
import edu.mayo.kmdp.series.Versionable;
import org.omg.spec.api4kp._1_0.id.Term;

public interface VersionableTerm<T extends Term & Versionable<T>,E extends Enum<E>>
    extends ConceptTerm<T>, Versionable<T> {

  E asEnum();

  Series<T> asSeries();


  default boolean isSame(Term other) {
    return isSameVersion(other);
  }

  default boolean sameAs(Term other) {
    return isSameVersion(other);
  }

  default boolean isSameVersion(Term other) {
    return this.getUuid().equals(other.getUuid())
        && this.getVersionTag().equals(other.getVersionTag());
  }

  default boolean isDifferentVersion(Term other) {
    return this.getUuid().equals(other.getUuid())
        && ! this.getVersionTag().equals(other.getVersionTag());
  }

  default boolean isSameEntity(Term other) {
    return this.getUuid().equals(other.getUuid());
  }

}
