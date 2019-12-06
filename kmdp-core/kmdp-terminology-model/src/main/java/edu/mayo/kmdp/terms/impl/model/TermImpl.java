package edu.mayo.kmdp.terms.impl.model;

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.TermDescription;
import edu.mayo.kmdp.util.Util;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;

public class TermImpl extends ConceptIdentifier implements TermDescription {

  private List<String> tags;

  private Term[] ancestors;
  private Term[] ancestorsClosure;

  TermImpl() {

  }

  public TermImpl(final String conceptId, final String conceptUUID, final String code,
      final List<String> additionalCodes, final String displayName, final String referent,
      final Term[] ancestors, final Term[] closure) {
    this.ref = Util.isEmpty(referent) ? null : URI.create(referent);
    this.tag = code;
    this.tags = java.util.Collections.unmodifiableList(additionalCodes);
    this.label = displayName;
    this.ancestors = ancestors;
    this.ancestorsClosure = closure;
    this.conceptId = URI.create(conceptId);
    this.conceptUUID = UUID.fromString(conceptUUID);
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public String getTag() {
    return tag;
  }

  public List<String> getTags() {
    return tags;
  }

  public UUID getConceptUUID() {
    return conceptUUID;
  }

  @Override
  public URI getRef() {
    return ref;
  }

  @Override
  public URI getConceptId() {
    return conceptId;
  }

  @Override
  public Term[] getClosure() {
    return ancestorsClosure;
  }

  public Term[] getAncestors() {
    return ancestors;
  }

}
