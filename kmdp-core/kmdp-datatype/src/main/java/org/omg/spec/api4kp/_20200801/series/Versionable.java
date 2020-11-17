package org.omg.spec.api4kp._20200801.series;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;
import org.omg.spec.api4kp._20200801.id.Identifiable;
import org.omg.spec.api4kp._20200801.id.VersionIdentifier;

/**
 * Interface that marks Immutable Resources that are SNAPSHOTs
 * of some mutable Entity, and are part of a Series
 *
 * This interface does not require the Versionable
 * to reference the Series, but does not prevent implementations
 * from doing so
 *
 * Scope note: the term 'Versionable' indicates that SNAPSHOTs
 * are candidate Versions
 * @param <T>
 */
public interface Versionable<T extends Versionable<T,E>, E extends Identifiable>
    extends Identifiable {

  /**
   * A Version Identifier associated to this Versionable.
   * Can be stable, or SNAPSHOT-like (e.g. "CURRENT", "LATEST"),
   * even if the use of SemVer patterns is encouraged if not expected
   *
   * @return A Version Identifier associated to this Versionable
   */
  VersionIdentifier getVersionIdentifier();


  /**
   * @return the Object that represents the Immutable
   * SNAPSHOT of the mutable Entity
   */
  @SuppressWarnings("unchecked")
  default T snapshot() {
    return (T) this;
  }

  /**
   * @return true if a successor Version of this entity exists
   */
  default boolean isVersionExpired() {
    return false;
  }

  /**
   * @return the Date of expiration of this Versionable,
   * corrsponding to the establishment of a successor version,
   * or to the destruction of the mutable Entity
   */
  default Optional<Date> getVersionExpiredOn() {
    return Optional.empty();
  }

  /**
   * @return the Date of establishment of this Versionable,
   */
  Date getVersionEstablishedOn();

  /**
   * Assigns a (stable) Version Identifier to this Versionable
   * @param identifier
   */
  default void dub(VersionIdentifier identifier) {
    if (getVersionIdentifier() != null) {
      throw new IllegalStateException("Unable to assign new identifier "
          + identifier + " to an already identified entity " + getVersionIdentifier());
    }
  }

  /**
   * Compares two Versionables based on their date of establishment,
   * effectively determining the 'latest'
   * @param <T>
   * @return
   */
  static <T extends Versionable<T,E>, E extends Identifiable> Comparator<T> mostRecentFirstComparator() {
    Comparator<T> c = Comparator.comparing(v -> v.getVersionIdentifier().getEstablishedOn());
    return c.reversed();
  }


  /**
   * Compares two Versionables based on their version, according some ordering
   * implied by their version identifier, effectively determining the 'greatest'
   * @param <T>
   * @return
   */
  static <T extends Versionable<T,E>, E extends Identifiable> Comparator<T> highestVersionFirstComparator() {
    Comparator<T> c = Comparator.comparing(v -> v.getVersionIdentifier().getVersionTag());
    return c.reversed();
  }


  /**
   * Compares a Versionable to a collection of Versionables,
   * and returns true if at least of the members of the collection is the same
   * version of the same entity as this
   * @param others
   * @return
   */
  default boolean isAnyOfVersions(Collection<? extends T> others) {
    return others != null && others.stream().anyMatch(this::isSameVersion);
  }

  /**
   * Compares a Versionable to a collection of Versionables,
   * and returns true if none of the members of the collection
   * is the same version as this
   * (either because they are Versionable of a different Entity,
   * or different versions of the same entity)
   * @param others
   * @return
   */
  default boolean isNoneOfVersions(Collection<? extends T> others) {
    return ! isAnyOfVersions(others);
  }


  /**
   * Compares two Versionables, returning true if and only if they are SNAPSHOTs of
   * the same mutable entity.
   *
   * The default implementation relies on the 'resource' (version agnostic)
   * component of the VersionIdentifier associated to this Versionable.
   *
   * Implementing classes can override this method to provide more precise
   * and/or efficient definitions
   *
   * @param other the Versionable to be compared to
   * @return true if this and other are Versionables of the same mutable entity
   */
  default boolean ofSameAs(T other) {
    return other != null
        && other.getVersionIdentifier() != null
        && this.getVersionIdentifier() != null
        && this.getVersionIdentifier().getResourceId() != null
        && this.getVersionIdentifier().getResourceId().equals(
        other.getVersionIdentifier().getResourceId());
  }

  /**
   * Compares two Versionables, returning true if and only if they are
   * the same SNAPSHOT of the same mutable entity.
   *
   * @param other the Versionable to be compared to
   * @return true if this and other are the same SNAPSHOT of the same mutable entity
   */
  default boolean isSameVersion(T other) {
    return ofSameAs(other)
        && this.getVersionIdentifier().getVersionId() != null
        && other.getVersionIdentifier().getVersionId() != null
        && this.getVersionIdentifier().getVersionId().equals(
            other.getVersionIdentifier().getVersionId());
  }

  /**
   * Compares two Versionables, returning true if and only if they are
   * two different SNAPSHOTs/Versions of the same mutable entity.
   *
   * @param other the Versionable to be compared to
   * @return true if this and other are different SNAPSHOTs of the same mutable entity
   */
  default boolean isDifferentVersion(T other) {
    return ofSameAs(other)
        && this.getVersionIdentifier().getVersionId() != null
        && other.getVersionIdentifier().getVersionId() != null
        && ! this.getVersionIdentifier().getVersionId().equals(
        other.getVersionIdentifier().getVersionId());
  }

  /**
   * Determines if this versionable is a SNAPSHOT of a given entity
   * @param entity the reference mutable entity
   * @return true if this is a SNAPSHOT/version of entity
   */
  default boolean isVersionOf(E entity) {
    if (entity == null || entity.getIdentifier() == null) {
      return false;
    }
    return this.getVersionIdentifier() != null
        && this.getVersionIdentifier().getResourceId() != null
        && this.getVersionIdentifier().getResourceId()
        .equals(entity.getIdentifier().getResourceId());
  }

}
