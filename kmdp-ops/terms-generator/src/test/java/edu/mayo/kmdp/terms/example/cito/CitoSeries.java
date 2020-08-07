package edu.mayo.kmdp.terms.example.cito;


import static edu.mayo.kmdp.id.helper.DatatypeHelper.indexByUUID;
import static edu.mayo.kmdp.id.helper.DatatypeHelper.resolveTerm;

import edu.mayo.kmdp.series.Series;
import edu.mayo.kmdp.terms.ConceptTerm;
import edu.mayo.kmdp.terms.TermDescription;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.omg.spec.api4kp._1_0.id.ResourceIdentifier;
import org.omg.spec.api4kp._1_0.id.Term;
import org.omg.spec.api4kp._1_0.id.VersionIdentifier;

public enum CitoSeries implements ICito, Series<ICito> {

  Cites(Cito.Cites),
  Cites_As_Source_Document(Cito.Cites_As_Source_Document);

  public static final Map<UUID,ICito> index = indexByUUID(CitoSeries.values());


  private List<ICito> versions;

  CitoSeries(ICito... versions) {
    this.versions = Arrays.asList(versions);
  }

  public List<ICito> getVersions() {
    return versions;
  }

  @Override
  public TermDescription getDescription() {
    return latest().map(ConceptTerm::getDescription)
        .orElse(null);
  }

  @Override
  public ResourceIdentifier getNamespace() {
    return latest().map(ConceptTerm::getNamespace)
        .orElse(null);
  }

  @Override
  public VersionIdentifier getVersionIdentifier() {
    return getLatest().getVersionIdentifier();
  }



  public static Optional<ICito> resolve(final Term trm) {
    return resolveId(trm.getConceptId());
  }

  public static Optional<ICito> resolve(final String tag) {
    return resolveTag(tag);
  }

  public static Optional<ICito> resolveId(final String conceptId) {
    return resolveId(URI.create(conceptId));
  }

  public static Optional<ICito> resolveTag(final String tag) {
    return resolveTerm(tag, CitoSeries.values(), Term::getTag);
  }

  public static Optional<ICito> resolveUUID(final UUID conceptId) {
    return Optional.of(index.get(conceptId));
  }

  public static Optional<ICito> resolveId(final URI conceptId) {
    return resolveTerm(conceptId, CitoSeries.values(), Term::getConceptId);
  }

  public static Optional<ICito> resolveRef(final String refUri) {
    return resolveTerm(refUri, CitoSeries.values(), Term::getReferentId);
  }

  @Override
  public CitoSeries asEnum() {
    return this;
  }

  @Override
  public Series<ICito> asSeries() {
    return this;
  }

  @Override
  public String getVersionTag() {
    return getNamespace().getVersionTag();
  }

  @Override
  public Date getEstablishedOn() {
    return getVersions().get(0).getEstablishedOn();
  }

  @Override
  public Date getVersionEstablishedOn() {
    return getEstablishedOn();
  }

  @Override
  public URI getResourceId() {
    return getDescription().getResourceId();
  }

  @Override
  public UUID getUuid() {
    return getDescription().getUuid();
  }
}