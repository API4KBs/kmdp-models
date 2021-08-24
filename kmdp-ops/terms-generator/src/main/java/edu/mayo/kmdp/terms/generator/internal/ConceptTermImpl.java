package edu.mayo.kmdp.terms.generator.internal;

import static edu.mayo.kmdp.util.NameUtils.namespaceURIStringToPackage;
import static edu.mayo.kmdp.util.NameUtils.removeTrailingPart;
import static edu.mayo.kmdp.util.Util.ensureUTF8;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.jena.vocabulary.SKOS;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.terms.ConceptScheme;

public class ConceptTermImpl extends InternalTerm {

  private UUID internalConceptUUID;
  private List<String> notations;

  private Map<String,String> labels;
  private String primaryLabelType;

  public ConceptTermImpl(URI conceptURI, String code, Map<String,String> labels, String comment, URI refUri,
      ConceptScheme<Term> scheme, UUID conceptUUID, List<String> notations, Date establishedOn,
      String primaryLabelType) {

    super(conceptURI, code, labels.get(detectActualPrimaryLabelType(primaryLabelType, labels)),
        ensureUTF8(comment), refUri, scheme, establishedOn);
    this.internalConceptUUID = conceptUUID;
    this.notations = new ArrayList<>(notations);
    this.labels = labels;
    this.primaryLabelType = detectActualPrimaryLabelType(primaryLabelType, labels);
  }

  public ConceptTermImpl(ConceptTermImpl other) {
    this(other.getConceptId(), other.getTag(), other.getLabels(), other.getComment(),
        other.getReferentId(), other.getScheme(), other.getUuid(), other.getNotations(), other.getEstablishedOn(),
        other.getPrimaryLabelType());
  }

  private static String detectActualPrimaryLabelType(String primaryLabelType, Map<String, String> labels) {
    if (primaryLabelType != null && labels.containsKey(primaryLabelType)) {
      return primaryLabelType;
    } else if (! labels.containsKey(SKOS.prefLabel.getLocalName())) {
      throw new IllegalStateException("Unable to detect primary label type " + labels +
          " when expecting " + primaryLabelType);
    } else {
      return SKOS.prefLabel.getLocalName();
    }
  }

  private String getPrimaryLabelType() {
    return primaryLabelType;
  }

  private Map<String, String> getLabels() {
    return labels;
  }

  public String getTermConceptName() {
    String name = labels.getOrDefault(SKOS.hiddenLabel.getLocalName(), getPrefLabel());
    return edu.mayo.kmdp.util.NameUtils.getTermConceptName(tag, name);
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
    return new ConceptTermImpl(getConceptId(), getTag(), getLabels(), getComment(), getReferentId(),
        cs, getUuid(), new ArrayList<>(getNotations()), getEstablishedOn(), getPrimaryLabelType());
  }

  @Override
  public UUID getUuid() {
    return internalConceptUUID;
  }

  public List<String> getNotations() {
    return notations;
  }

  @Override
  public String toString() {
    return getLabel() + '{' + tag + '}';
  }

  @Override
  public boolean equals(Object object) {
    return object instanceof ConceptTermImpl
        && getConceptId().equals(((ConceptTermImpl) object).getConceptId())
        && getNamespaceUri().equals(((ConceptTermImpl) object).getNamespaceUri());
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (getConceptId() != null ? getConceptId().hashCode() : 0);
    result = 31 * result + (getNamespaceUri() != null ? getNamespaceUri().hashCode() : 0);
    return result;
  }

}