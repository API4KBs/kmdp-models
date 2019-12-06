package edu.mayo.kmdp.terms;

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.series.Series;
import edu.mayo.kmdp.series.Versionable;

public interface VersionableTerm<T extends Term & Versionable<T>,E extends Enum<E>>
    extends ConceptTerm<T>, Versionable<T> {

  E asEnum();

  Series<T> asSeries();

}
