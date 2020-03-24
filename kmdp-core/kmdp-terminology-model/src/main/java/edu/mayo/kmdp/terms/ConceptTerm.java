package edu.mayo.kmdp.terms;

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.id.helper.DatatypeHelper;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.omg.spec.api4kp._1_0.id.ConceptIdentifier;

public interface ConceptTerm<T extends Term> extends Term, org.omg.spec.api4kp._1_0.id.Term, Taxonomic<T> {

  @Override
  default URI getResourceId() {
    return getDescription().getConceptId();
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

  @Override
  default List<String> getTags() {
    return getDescription().getTags();
  }

  @Override
  default UUID getConceptUUID() {
    return getDescription().getConceptUUID();
  }

  @Override
  default UUID getUuid() {
    return getConceptUUID();
  }

  @Override
  default URI getRef() {
    return getDescription().getRef();
  }

  @Override
  default URI getReferentId() {
    return getRef();
  }

  @Override
  default URI getConceptId() {
    return getDescription().getConceptId();
  }

  default void setConceptId(URI id) {
    throw new UnsupportedOperationException("IDs are immutable");
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
  default org.omg.spec.api4kp._1_0.identifiers.QualifiedIdentifier asQualified() {
    return DatatypeHelper.toQualifiedIdentifier( this.getConceptId() );
  }

  @Override
  default org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier asConcept() {
    return DatatypeHelper.toConceptIdentifier( this );
  }

  @Override
  default ConceptIdentifier asConceptIdentifier() {
    return (ConceptIdentifier) org.omg.spec.api4kp._1_0.id.Term
        .newId(this.getTag(), this.getUuid(), this.getNamespaceUri(), this.getReferentId(),
            this.getVersionTag(), this.getLabel(), this.getEstablishedOn());
  }

  TermDescription getDescription();


}
