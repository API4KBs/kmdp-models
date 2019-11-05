package edu.mayo.kmdp.terms;

import edu.mayo.kmdp.series.Series;
import java.util.Date;

@Deprecated
public interface TermSeries<T extends VersionableTerm<T,E>, E extends Enum<E>>
    extends ConceptTerm<T>, Series<T> {

}
