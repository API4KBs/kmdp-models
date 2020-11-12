package org.omg.spec.api4kp._20200801.terms;

import java.util.Date;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.series.Series;

/**
 * Interface that marks (versionable) Terms that are implemented by means of Enum constants
 *
 * @param <E> The supporting enumeration type
 * @param <T> The type of used to implement term snapshots
 * @param <S> The type of the denoted entity
 */
public interface EnumeratedConceptTerm<E extends Enum<E>, T extends VersionableTerm<T,S>,S extends Term>
    extends ConceptTerm {

  Series<T, S> asSeries();

  @Override
  default Date getEstablishedOn() {
    return asSeries().getEarliest().getVersionEstablishedOn();
  }

  ResourceIdentifier getDefiningScheme();

}
