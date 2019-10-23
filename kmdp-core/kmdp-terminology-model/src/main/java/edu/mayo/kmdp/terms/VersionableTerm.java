package edu.mayo.kmdp.terms;

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.id.Versionable;

public interface VersionableTerm<T extends Term> extends ConceptTerm<T>, Versionable {

}
