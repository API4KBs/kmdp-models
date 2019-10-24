package edu.mayo.kmdp.terms;

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.series.Versionable;

public interface VersionableTerm<T extends Term & Versionable<T>> extends ConceptTerm<T>, Versionable<T> {

}
