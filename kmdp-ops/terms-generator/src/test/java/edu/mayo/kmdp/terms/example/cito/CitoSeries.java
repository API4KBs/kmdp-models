package edu.mayo.kmdp.terms.example.cito;


import static edu.mayo.kmdp.id.helper.DatatypeHelper.indexByUUID;
import static edu.mayo.kmdp.id.helper.DatatypeHelper.resolveTerm;

import edu.mayo.kmdp.terms.example.cito.ICito.ICitoVersion;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.series.Series;
import org.omg.spec.api4kp._20200801.terms.ConceptTerm;
import org.omg.spec.api4kp._20200801.terms.EnumeratedConceptTerm;
import org.omg.spec.api4kp._20200801.terms.TermDescription;

public enum CitoSeries implements
    ICito,
    Series<ICitoVersion,ICito>,
    EnumeratedConceptTerm<CitoSeries,ICitoVersion,ICito> {

  Cites(Cito.Cites),
  Cites_As_Source_Document(Cito.Cites_As_Source_Document);

  public static final Map<UUID,CitoSeries> index = indexByUUID(CitoSeries.values());


  private List<ICitoVersion> versions;

  CitoSeries(ICitoVersion... versions) {
    this.versions = Arrays.asList(versions);
  }

  public List<ICitoVersion> getVersions() {
    return versions;
  }

  @Override
  public TermDescription getDescription() {
    return latest().map(ConceptTerm::getDescription)
        .orElse(null);
  }


  @Override
  public Date getEstablishedOn() {
    return getVersions().get(getVersions().size() - 1).getEstablishedOn();
  }

  @Override
  public ResourceIdentifier getDefiningScheme() {
    return schemeSeriesIdentifier;
  }

  @Override
  public URI getResourceId() {
    return getDescription().getResourceId();
  }

  @Override
  public CitoSeries asEnum() {
    return this;
  }

  @Override
  public CitoSeries asSeries() {
    return this;
  }


  public static CitoSeries resolve(final Term term) {
    return resolveUUID(term.getUuid())
        .orElseThrow();
  }

  public static CitoSeries resolve(final ICito term) {
    return resolveUUID(term.getUuid())
        .orElseThrow();
  }


  public static Optional<CitoSeries> resolveId(final String conceptId) {
    return resolveId(URI.create(conceptId));
  }

  public static Optional<CitoSeries> resolveTag(final String tag) {
    return resolveTerm(tag, CitoSeries.values(), Term::getTag);
  }

  public static Optional<CitoSeries> resolveUUID(final UUID conceptId) {
    return Optional.of(index.get(conceptId));
  }

  public static Optional<CitoSeries> resolveId(final URI conceptId) {
    return resolveTerm(conceptId, CitoSeries.values(), Term::getConceptId);
  }

  public static Optional<CitoSeries> resolveRef(final String refUri) {
    return resolveTerm(refUri, CitoSeries.values(), Term::getReferentId);
  }

//  @Override
//  public boolean sameAs(ICito other) {
//    return isSameVersion(other);
//  }
//
//  @Override
//  public boolean isSameEntity(ICito other) {
//    return other.getUuid().equals(this.getUuid());
//  }
//
//  @Override
//  public boolean isSameVersion(ICito other) {
//    return false;
//  }
//
//  @Override
//  public boolean isDifferentVersion(ICito other) {
//    return false;
//  }
}