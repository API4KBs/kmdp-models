package org.omg.spec.api4kp._20200801.terms.model;

import edu.mayo.kmdp.util.NameUtils;
import org.omg.spec.api4kp._20200801.terms.TermDescription;
import edu.mayo.kmdp.util.Util;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.omg.spec.api4kp._20200801.id.ConceptIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;

public class TermImpl extends ConceptIdentifier implements TermDescription {

  private List<String> tags;

  private Term[] ancestors;
  private Term[] ancestorsClosure;

  public TermImpl() {

  }

  public TermImpl(final String conceptId, final String conceptUUID, final String versionTag, final String code,
      final List<String> additionalCodes, final String displayName, final String referent,
      final Term[] ancestors, final Term[] closure, Date publicationDate) {
    this.referentId = Util.isEmpty(referent) ? null : URI.create(referent);
    this.tag = code;
    this.versionTag = versionTag;
    this.tags = java.util.Collections.unmodifiableList(additionalCodes);
    this.name = displayName;
    this.ancestors = ancestors;
    this.ancestorsClosure = closure;
    this.resourceId = URI.create(conceptId);
    this.versionId = Util.isEmpty(versionTag)
        ? null
        : SemanticIdentifier.newVersionId(resourceId,versionTag).getVersionId();
    this.namespaceUri = SemanticIdentifier.newNamespaceId(
        URI.create(NameUtils.removeTrailingPart(conceptId))).getResourceId();
    this.uuid = UUID.fromString(conceptUUID);
    this.establishedOn = publicationDate;
  }

  public List<String> getTags() {
    return tags;
  }

  @Override
  public Term[] getClosure() {
    return ancestorsClosure;
  }

  public Term[] getAncestors() {
    return ancestors;
  }

  @Override
  public String getLabel() {
    return getName();
  }
}
