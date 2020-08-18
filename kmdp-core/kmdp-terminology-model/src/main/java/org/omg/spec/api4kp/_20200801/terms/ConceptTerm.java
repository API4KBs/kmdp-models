package org.omg.spec.api4kp._20200801.terms;

import java.net.URI;
import java.util.List;
import org.omg.spec.api4kp._20200801.id.ConceptIdentifier;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.terms.Taxonomic;
import org.omg.spec.api4kp._20200801.terms.TermDescription;

public interface ConceptTerm<T extends Term> extends Term, Taxonomic<T> {


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

  ResourceIdentifier getNamespace();
  
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