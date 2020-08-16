package edu.mayo.kmdp.terms.impl.model;

import edu.mayo.kmdp.terms.ConceptTerm;
import org.omg.spec.api4kp._20200801.id.ConceptIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;

public class ConceptDescriptor extends ConceptIdentifier {

  private Term[] ancestors;
  private Term[] closure;

  public ConceptDescriptor() {
    // Empty constructor
  }

  /**
   * Get the ancestors for the conceptDescriptor
   * @return an array of any ancestor terms
   */
  public Term[] getAncestors() {
    return ancestors;
  }

  /**
   * Set the ancestors for the conceptDescriptor
   * @param ancestors the ancestor terms
   */
  public void setAncestors(Term[] ancestors) {
    this.ancestors = ancestors;
  }

  /**
   * Get all the relations for the conceptDescriptor
   * @return an array of any related terms
   */
  public Term[] getClosure() {
    return closure;
  }

  /**
   * Set the related terms for the conceptDescriptor
   * @param closure the related terms
   */
  public void setClosure(Term[] closure) {
    this.closure = closure;
  }

  /**
   * Converts a term into a ConceptDescriptor
   * @param v the term to be converted
   * @return the ConceptDescriptor
   */
  public static ConceptDescriptor toConceptDescriptor(ConceptTerm<? extends Term> v) {
    if (v == null) {
      return null;
    }
    if (v instanceof ConceptDescriptor) {
      return (ConceptDescriptor) v;
    }
    ConceptDescriptor conceptDescriptor = new ConceptDescriptor();
    conceptDescriptor.withReferentId(v.getReferentId());
    conceptDescriptor.withUuid(v.getUuid());
    conceptDescriptor.withName(v.getLabel());
    conceptDescriptor.withTag(v.getTag());
    conceptDescriptor.withResourceId(v.getConceptId());
    conceptDescriptor.withNamespaceUri(v.getNamespaceUri());
    conceptDescriptor.setAncestors(v.getAncestors());
    conceptDescriptor.setClosure(v.getClosure());

    return conceptDescriptor;
  }

}
