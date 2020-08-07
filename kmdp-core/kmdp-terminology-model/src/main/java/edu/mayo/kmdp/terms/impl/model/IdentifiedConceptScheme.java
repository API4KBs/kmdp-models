package edu.mayo.kmdp.terms.impl.model;

import edu.mayo.kmdp.terms.ConceptScheme;
import java.net.URI;
import java.util.Date;
import java.util.UUID;
import org.omg.spec.api4kp._1_0.id.ResourceIdentifier;
import org.omg.spec.api4kp._1_0.id.SemanticIdentifier;
import org.omg.spec.api4kp._1_0.id.Term;

public abstract class IdentifiedConceptScheme<T extends Term> implements ConceptScheme<T> {

  private ResourceIdentifier schemeId;

  public IdentifiedConceptScheme(URI schemeURI, String versionTag, String schemeName, Date pubDate) {
    this.schemeId = SemanticIdentifier
        .newId(schemeURI)
        .withVersionTag(versionTag)
        .withName(schemeName)
        .withEstablishedOn(pubDate);
  }


  @Override
  public String getLabel() {
    return schemeId.getName();
  }

  @Override
  public String getTag() {
    return schemeId.getTag();
  }

  @Override
  public String getName() {
    return schemeId.getName();
  }

  @Override
  public Date getEstablishedOn() {
    return schemeId.getEstablishedOn();
  }

  @Override
  public URI getResourceId() {
    return schemeId.getResourceId();
  }


  @Override
  public String getVersionTag() {
    return schemeId.getVersionTag();
  }

  @Override
  public URI getVersionId() {
    return schemeId.getVersionId();
  }

  public ResourceIdentifier asNamespace() {
    return SemanticIdentifier.newNamespaceId(schemeId.getNamespaceUri());
  }

  @Override
  public UUID getUuid() {
    return schemeId.getUuid();
  }

  @Override
  public URI getNamespaceUri() {
    return schemeId.getResourceId();
  }
}
