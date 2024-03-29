package org.omg.spec.api4kp._20200801.terms.model;

import java.util.HashMap;
import java.util.Map;
import org.apache.jena.vocabulary.SKOS;
import org.omg.spec.api4kp._20200801.id.ConceptIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.terms.ConceptTerm;

public class ConceptDescriptor extends ConceptIdentifier {

  private Term[] ancestors;
  private Term[] closure;

  private final Map<String,String> labels = new HashMap<>();

  public ConceptDescriptor() {
    // Empty constructor
  }

  /**
   * Get the ancestors for the conceptDescriptor
   *
   * @return an array of any ancestor terms
   */
  public Term[] getAncestors() {
    return ancestors;
  }

  /**
   * Set the ancestors for the conceptDescriptor
   *
   * @param ancestors the ancestor terms
   */
  public void setAncestors(Term[] ancestors) {
    this.ancestors = ancestors;
  }

  /**
   * Get all the relations for the conceptDescriptor
   *
   * @return an array of any related terms
   */
  public Term[] getClosure() {
    return closure;
  }

  /**
   * Set the related terms for the conceptDescriptor
   *
   * @param closure the related terms
   */
  public void setClosure(Term[] closure) {
    this.closure = closure;
  }

  public Map<String, String> getLabels() {
    return labels;
  }

  public ConceptDescriptor withLabels(Map<String,String> labelsByType) {
    labels.putAll(labelsByType);
    return this;
  }

  public void addLabel(String label, String type) {
    labels.put(type, label);
  }

  @Override
  public String getPrefLabel() {
    return labels.containsKey(SKOS.prefLabel.getLocalName())
        ? labels.get(SKOS.prefLabel.getLocalName())
        : super.getPrefLabel();
  }

  /**
   * Converts a term into a ConceptDescriptor
   *
   * @param v the term to be converted
   * @return the ConceptDescriptor
   */
  public static ConceptDescriptor toConceptDescriptor(Term v) {
    if (v == null) {
      return null;
    }
    if (v instanceof ConceptDescriptor) {
      return (ConceptDescriptor) v;
    }
    ConceptDescriptor cd = (ConceptDescriptor) new ConceptDescriptor()
        .withReferentId(v.getReferentId())
        .withUuid(v.getUuid())
        .withName(v.getLabel())
        .withTag(v.getTag())
        .withResourceId(v.getConceptId())
        .withVersionId(v.getVersionId())
        .withEstablishedOn(v.getEstablishedOn())
        .withVersionTag(v.getVersionTag())
        .withNamespaceUri(v.getNamespaceUri());

    if (v instanceof ConceptTerm) {
      ConceptTerm ct = (ConceptTerm) v;
      cd.setAncestors(ct.getAncestors());
      cd.setClosure(ct.getClosure());
    }

    return cd;
  }


  /**
   * Creates a ConceptIdentifier that is NOT a ConceptDescriptor from this ConceptDescriptor
   *
   * @return
   * @see Term#asConceptIdentifier()
   */
  public ConceptIdentifier toConceptIdentifier() {
    return new ConceptIdentifier()
        .withReferentId(getReferentId())
        .withUuid(getUuid())
        .withName(getLabel())
        .withTag(getTag())
        .withResourceId(getConceptId())
        .withNamespaceUri(getNamespaceUri())
        .withVersionId(getVersionId())
        .withEstablishedOn(getEstablishedOn())
        .withName(getName())
        .withVersionTag(getVersionTag());
  }

  @Override
  public boolean equals(Object object) {
    return super.equals(object);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
