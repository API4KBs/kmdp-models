package edu.mayo.kmdp.terms.generator.internal;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import org.omg.spec.api4kp._20200801.id.Term;

public class ConceptTermSeries {

  private URI id;
  private String label;
  private List<Term> versions = new LinkedList<>();

  public ConceptTermSeries(Term trm) {
    this.id = trm.getConceptId();
    this.label = ((ConceptTermImpl) trm).getTermConceptName();
    addMember(trm);
  }

  public List<Term> getVersions() {
    return versions;
  }

  public ConceptTermSeries addMember(Term trm) {
    if (trm.getConceptId().equals(id)) {
      versions.add(trm);
    } else {
      throw new IllegalStateException("Trying to add a concept to a different series");
    }
    return this;
  }

  public String getLabel() {
    return label;
  }
}
