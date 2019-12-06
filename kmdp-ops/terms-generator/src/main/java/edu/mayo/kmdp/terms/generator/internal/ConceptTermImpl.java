package edu.mayo.kmdp.terms.generator.internal;

import static edu.mayo.kmdp.util.NameUtils.namespaceURIStringToPackage;
import static edu.mayo.kmdp.util.NameUtils.removeTrailingPart;
import static edu.mayo.kmdp.util.Util.ensureUTF8;

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.ConceptScheme;
import edu.mayo.kmdp.terms.impl.model.InternalTerm;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ConceptTermImpl extends InternalTerm {

  private UUID internalConceptUUID;
  private List<String> notations;

  public ConceptTermImpl(URI conceptURI, String code, String label, String comment, URI refUri,
      ConceptScheme<Term> scheme, UUID conceptUUID, List<String> notations) {

    super(conceptURI, code, label, ensureUTF8(comment), refUri, scheme);
    this.internalConceptUUID = conceptUUID;
    this.notations = new ArrayList<>(notations);
  }

  public ConceptTermImpl(ConceptTermImpl other) {
    this(other.getConceptId(), other.getTag(), other.getLabel(), other.getComment(),
        other.getRef(), other.getScheme(), other.getConceptUUID(), other.getNotations());
  }

  public String getTermConceptName() {
    return edu.mayo.kmdp.util.NameUtils.getTermConceptName(tag, label);
  }

  public String getTermConceptPackage() {
    return namespaceURIStringToPackage(removeTrailingPart(getScheme().getVersionId().toString()));
  }

  public String getTermConceptScheme() {
    return getScheme().getPublicName();
  }

  @Override
  public Term[] getAncestors() {
    Set<Term> ancs = ((MutableConceptScheme) scheme).getAncestors(this);
    return ancs.toArray(new Term[0]);
  }

  @Override
  public Term[] getClosure() {
    List<Term> closure = ((MutableConceptScheme) scheme).getClosure(this);
    return closure.toArray(new Term[0]);
  }

  public ConceptTermImpl cloneInto(ConceptScheme<Term> cs) {
    return new ConceptTermImpl(getConceptId(), getTag(), getLabel(), getComment(), getRef(),
        cs, getConceptUUID(), new ArrayList<>(getNotations()));
  }

  @Override
  public UUID getConceptUUID() {
    return internalConceptUUID;
  }

  public List<String> getNotations() {
    return notations;
  }

  @Override
  public String toString() {
    return label + '{' + tag + '}';
  }

  @Override
  public boolean equals(Object object) {
    return object instanceof ConceptTermImpl
        && getConceptId().equals(((ConceptTermImpl) object).getConceptId())
        && getNamespace().equals(((ConceptTermImpl) object).getNamespace());
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (conceptId != null ? conceptId.hashCode() : 0);
    result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
    return result;
  }

}