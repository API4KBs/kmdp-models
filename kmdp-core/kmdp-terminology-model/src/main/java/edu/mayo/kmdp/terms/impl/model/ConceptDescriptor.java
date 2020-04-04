package edu.mayo.kmdp.terms.impl.model;

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.ConceptTerm;
import org.omg.spec.api4kp._1_0.id.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;

public class ConceptDescriptor extends ConceptIdentifier {


  private Term[] ancestors;
  private Term[] ancestorsClosure;



  public static ConceptDescriptor toConceptDescriptor(ConceptTerm v) {
    if (v == null) {
      return null;
    }
    if (v instanceof ConceptDescriptor) {
      return (ConceptDescriptor) v;
    }
    return (ConceptDescriptor) new ConceptDescriptor()
        .withReferentId(v.getRef())
        .withUuid(v.getConceptUUID())
        .withName(v.getLabel())
        .withTag(v.getTag())
        .withResourceId(v.getConceptId())
        .withNamespaceUri(((NamespaceIdentifier) v.getNamespace()).getId());
  }

}
