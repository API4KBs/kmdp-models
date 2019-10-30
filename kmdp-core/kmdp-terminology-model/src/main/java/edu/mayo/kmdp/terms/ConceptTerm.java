package edu.mayo.kmdp.terms;

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.id.VersionedIdentifier;
import edu.mayo.kmdp.id.helper.DatatypeHelper;
import edu.mayo.kmdp.series.Versionable;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface ConceptTerm<T extends Term> extends Term, Taxonomic<T> {

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
  default URI getRef() {
    return getDescription().getRef();
  }

  @Override
  default URI getConceptId() {
    return getDescription().getConceptId();
  }
  
  @Override
  default Term[] getClosure() {
    return getDescription().getClosure();
  }

  @Override
  default Term[] getAncestors() {
    return getDescription().getAncestors();
  }

  default Date getEstablishedOn() {
    return ((VersionedIdentifier) getNamespace()).getEstablishedOn();
  }
  default String getVersion() {
    return ((VersionedIdentifier) getNamespace()).getVersion();
  }

  @Override
  default org.omg.spec.api4kp._1_0.identifiers.QualifiedIdentifier asQualified() {
    return DatatypeHelper.toQualifiedIdentifier( this.getConceptId() );
  }

  @Override
  default org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier asConcept() {
    return DatatypeHelper.toConceptIdentifier( this );
  }

  TermDescription getDescription();


}
