package edu.mayo.kmdp.terms;

import edu.mayo.kmdp.series.Series;

public interface TermSeries<T extends VersionableTerm<T,E>, E extends Enum<E>> extends Series<T> {

}
