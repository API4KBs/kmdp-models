package org.omg.spec.api4kp._20200801.series;

import java.net.URI;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import org.omg.spec.api4kp._20200801.id.Identifiable;
import org.omg.spec.api4kp._20200801.id.IdentifierConstants;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.id.VersionIdentifier;

/**
 * Interface that marks a Mutable Resource, and provides access to a Series of Versionable SNAPSHOTs
 * of that Resource
 * <p>
 * The Series is a collection which, by default, is sorted temporally, i.e. the latest SNAPSHOT is
 * considered to be the greatest
 *
 * @param <T> a Versionable Type that is used to represent the SNAPSHOTs of the Mutable Resource
 */
public interface Series<T extends Versionable<T,E>, E extends Identifiable> {

  default boolean isEntityOf(Versionable<?,E> v0) {
    return getResourceId() != null
        && v0.getVersionIdentifier() != null
        && getResourceId().equals(
        v0.getVersionIdentifier().getResourceId());
  }

  default boolean isSameEntity(E other) {
    return getResourceId() != null
        && other.getIdentifier().getResourceId() != null
        && getResourceId().equals(other.getIdentifier().getResourceId());
  }


  /**
   * @return the Resource Identifier of the Mutable entity
   */
  URI getResourceId();

  /**
   * Returns the Versionable element of the Series that is the most recently established, but not
   * yet expired, with respect to a reference Date
   *
   * @param d the reference Date
   * @return the SNAPSHOT as of that Date
   */
  default Optional<T> asOf(Date d) {
    boolean expired = this.getSeriesExpiredOn()
        .map(exp -> d.compareTo(exp) >= 0)
        .orElse(false);

    return expired
        ? Optional.empty()
        : getVersions().stream()
            .filter(v -> d.compareTo(v.getVersionEstablishedOn()) >= 0)
            .findFirst();
  }

  /**
   * Returns the Versionable SNAPSHOT with the given version tag, if any
   *
   * @param versionTag the lookup version-specific tag
   * @return The (Optional) Versionable with the given version tag
   */
  default Optional<T> getVersion(String versionTag) {
    return getVersions().stream()
        .filter(v -> versionTag.equals(v.getVersionIdentifier().getVersionTag()))
        .findAny();
  }

  /**
   * @param versionTag the version-specific tag
   * @return true if the Series contains a member with the given version tag
   */
  default boolean hasVersion(String versionTag) {
    return getVersion(versionTag).isPresent();
  }

  /**
   * Returns a Versionable SNAPSHOT by index, i.e. the index-th element of the Series, sorted
   * temporally by establishment Date
   *
   * @param index the index into the Series
   * @return the Versionable at the given index within the time-sorted sequence
   */
  default Optional<T> getVersion(int index) {
    if (index < 0 || index >= getVersions().size()) {
      return Optional.empty();
    }
    return Optional.ofNullable(getVersions().get(index));
  }

  /**
   * Adds an explicit Version to this entity's Series
   *
   * @param newSnapshot a Versionable of the same entity
   * @param id the Version-specific id to add
   */
  default void addVersion(T newSnapshot, VersionIdentifier id) {
    if (id == null) {
      throw new UnsupportedOperationException("Adding Versionable SNAPSHOT"
          + " requires an explicit Version ID");
    }
    if (! getResourceId().equals(id.getResourceId())) {
      throw new UnsupportedOperationException(
          "Adding a Versionable SNAPSHOT of entity " + id.getResourceId()
          + " to the Series for entity " + getResourceId());
    }

    newSnapshot.dub(id);

    if (!isEmpty() && ! getLatest().ofSameAs(newSnapshot)) {
      throw new IllegalArgumentException("Adding " + id.getTag()
          + " would violate the Series identity principle - series id = " + getLatest()
          .getVersionIdentifier().getTag());
    }
    if (hasVersion(id.getVersionTag())) {
      throw new IllegalArgumentException("Version already present");
    }

    this.getVersions().add(0, newSnapshot);
    this.getVersions()
        .sort(Versionable.mostRecentFirstComparator());
  }

  /**
   * @return true if this Series has no explicit Versionable SNAPSHOTs
   */
  default boolean isEmpty() {
    return getVersions().isEmpty();
  }

  /**
   * @return the latest versionable SNAPSHOT, or null if the series is empty
   * @see Series#isEmpty()
   */
  default T getLatest() {
    return latest().orElse(null);
  }

  /**
   * @return an Optional latest versionable SNAPSHOT, or empty() if the series is empty
   * @see Series#isEmpty()
   */
  default Optional<T> latest() {
    return isEmpty() ? Optional.empty() : Optional.of(getVersions().get(0));
  }

  /**
   * @return the first versionable SNAPSHOT, or null if the series is empty
   * @see Series#isEmpty()
   */
  default T getEarliest() {
    return earliest().orElse(null);
  }

  /**
   * @return the firt versionable SNAPSHOT, or empty() if the series is empty
   * @see Series#isEmpty()
   */
  default Optional<T> earliest() {
    List<T> versions = getVersions();
    return isEmpty() ? Optional.empty() : Optional.of(versions.get(versions.size() - 1));
  }

  /**
   * The Date of creation of the underlying mutable entity, estimated using the establishment date
   * of the first element in the Series, or empty() if the Series is empty
   *
   * @return the (Optional) Date the Series was established
   */
  default Optional<Date> getSeriesEstablishedOn() {
    return earliest()
        .map(Versionable::getVersionEstablishedOn);
  }

  /**
   * The Date of destruction of the underlying mutable Entity. By default, this is NOT assumed to
   * coincide with the expiration date of the latest SNAPSHOT
   *
   * @return the (Optional) Date the Series was expired
   */
  default Optional<Date> getSeriesExpiredOn() {
    return Optional.empty();
  }

  default boolean isSeriesExpired() {
    return getLatest().isVersionExpired();
  }

  /**
   * The Versionables in this series, as a List sorted by establishedOn
   *
   * @return A List of the Versionables, sorted by establishedOn
   */
  List<T> getVersions();

  /**
   * The Versionables in this series, as a List sorted using the order imposed by the version tags
   *
   *  Whenever possible, getVersions() and sortedByVersion() should coincide
   *
   * @return a List of the Versionables, sorted by version tag
   */
  default List<T> sortedByVersion() {
    List<T> list = new LinkedList<>(getVersions());
    list.sort(Versionable.highestVersionFirstComparator());
    return list;
  }


  /**
   * Creates a new Versionable SNAPSHOT applying a 'mutator' function to the latest() element in the
   * Series.
   *
   * @param mutator       A function that peforms a differential transformation of a SNAPSHOT of the
   *                      underlying mutable entity, creating a new SNAPSHOT of that same entity
   * @param newVersionTag a tag used to create a new Version Identifier for the newly created
   *                      SNAPSHOT
   * @param d             the point in time at which the new version is established
   * @return The new Versionable SNAPSHOT
   */
  default T evolve(UnaryOperator<T> mutator, String newVersionTag, Date d) {
    T latest = getLatest();
    T next = mutator.apply(latest.snapshot());
    addVersion(next,
        newIdentifier(latest.getVersionIdentifier().getTag(), newVersionTag, d));
    return next;
  }


  static ResourceIdentifier toVersion(ResourceIdentifier seriesId, String versionTag) {
    return SemanticIdentifier.toVersionId(seriesId, versionTag);
  }

  default VersionIdentifier newIdentifier(String tag) {
    return newIdentifier(tag, IdentifierConstants.VERSION_ZERO);
  }

  default VersionIdentifier newIdentifier(String tag, String versionTag) {
    return newIdentifier(tag, versionTag, new Date());

  }

  default ResourceIdentifier newIdentifier(String tag, String versionTag, Date d) {
    return SemanticIdentifier.newId(tag, versionTag)
        .withEstablishedOn(d);
  }

}
