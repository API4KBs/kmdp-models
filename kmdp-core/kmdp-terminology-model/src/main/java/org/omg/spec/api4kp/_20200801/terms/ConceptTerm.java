package org.omg.spec.api4kp._20200801.terms;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.omg.spec.api4kp._20200801.id.ConceptIdentifier;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;

/**
 * Interface that supports Terms that are part of a
 * common, possibly hierarchical concept scheme
 */
public interface ConceptTerm extends Term, Taxonomic {

  @Override
  default UUID getUuid() {
    return getDescription().getUuid();
  }

  @Override
  default URI getResourceId() {
    return getDescription().getResourceId();
  }

  @Override
  default URI getVersionId() {
    return getDescription().getVersionId();
  }

  @Override
  default String getVersionTag() {
    return getDescription().getVersionTag();
  }

  @Override
  default String getName() {
    return getLabel();
  }

  @Override
  default String getLabel() {
    return getDescription().getLabel();
  }

  @Override
  default String getTag() {
    return getDescription().getTag();
  }

  default List<String> getTags() {
    return getDescription().getTags();
  }

  @Override
  default URI getReferentId() {
    return getDescription().getReferentId();
  }

  default void setConceptId(URI id) {
    throw new UnsupportedOperationException("IDs are immutable");
  }

  default URI getNamespaceUri() {
    return getDescription().getNamespaceUri();
  }

  default ResourceIdentifier getNamespace() {
    return SemanticIdentifier.newNamespaceId(getNamespaceUri());
  }

  @Override
  default Term[] getClosure() {
    return getDescription().getClosure();
  }

  @Override
  default Term[] getAncestors() {
    return getDescription().getAncestors();
  }

  @Override
  default ConceptIdentifier asConceptIdentifier() {
    return (ConceptIdentifier) Term
        .newTerm(
            this.getConceptId(),
            this.getTag(),
            this.getUuid(),
            this.getNamespaceUri(),
            this.getReferentId(),
            this.getVersionTag(),
            this.getLabel(),
            this.getEstablishedOn());
  }

  TermDescription getDescription();


}
