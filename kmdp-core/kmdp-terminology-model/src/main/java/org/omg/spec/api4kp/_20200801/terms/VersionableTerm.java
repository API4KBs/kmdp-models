package org.omg.spec.api4kp._20200801.terms;

import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.series.Versionable;

/**
 * This interface extends the notion of (Concept) Term with
 * explicit version management.
 * Versionable terms are mutable with immutable snapshots.
 * Mutation can involve contigent properties of the term itself (e.g. labels),
 * or be due to changes in the referent (assuming the referent is mutable)
 *
 * Mostly used with Terms where the mutable characteristic
 * is the membership in one or more different Concept Schemes
 * at different points in time
 *
 * @param <T> The actual type used to implements the mutable term snapshots
 * @param <E> The type of the denoted entity
 */
public interface VersionableTerm<T extends VersionableTerm<T,E>, E extends Term>
    extends ConceptTerm, Versionable<T, E> {


  /**
   * Return true if two Term are versions of the same mutable Term
   *
   * Under the assumption that the denoted entity is an essential
   * characteristic of a Term (i.e. changing referent implies
   * the creation of a new Term, rather than a new version),
   * IF two term snapshots are version of the same term, then
   * the two snapshots must be co-referent
   */
  @Override
  default boolean ofSameAs(T other) {
    return other != null
        && this.getUuid().equals(other.getUuid());
  }

  /**
   * Return true if two Term versions are actually
   * the same version of the same Term
   *
   * @param other the Versionable Term to be compared to
   * @return
   */
  @Override
  default boolean isSameVersion(T other) {
    return ofSameAs(other)
        && this.getVersionTag().equals(other.getVersionTag());
  }

  /**
   * Return true if two Term versions are different
   * versions of the same mutable Term
   */
  @Override
  default boolean isDifferentVersion(T other) {
    return ofSameAs(other)
        && ! this.getVersionTag().equals(other.getVersionTag());
  }

}
