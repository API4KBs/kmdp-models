package edu.mayo.kmdp.terms;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.omg.spec.api4kp._1_0.id.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.id.ResourceIdentifier;
import org.omg.spec.api4kp._1_0.id.Term;

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
    return (ConceptIdentifier) org.omg.spec.api4kp._1_0.id.Term
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
