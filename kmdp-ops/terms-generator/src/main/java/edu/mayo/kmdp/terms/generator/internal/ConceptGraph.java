package edu.mayo.kmdp.terms.generator.internal;

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.id.VersionedIdentifier;
import edu.mayo.kmdp.terms.ConceptScheme;
import edu.mayo.kmdp.terms.generator.util.HierarchySorter;
import edu.mayo.kmdp.terms.generator.util.TransitiveClosure;
import edu.mayo.kmdp.util.DateTimeUtil;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConceptGraph {

  protected Map<URI, ConceptScheme<Term>> conceptSchemes;
  protected Map<Term, Set<Term>> conceptHierarchy;
  protected Map<Term, List<Term>> closure;

  protected ConceptGraph() {
    this.conceptSchemes = new HashMap<>();
    this.conceptHierarchy = new HashMap<>();
    this.closure = new HashMap<>();
  }

  public ConceptGraph(Map<URI, ConceptScheme<Term>> conceptSchemes,
      Map<Term, Set<Term>> conceptsWithParents) {
    this.conceptSchemes = new HashMap<>(conceptSchemes);
    this.conceptHierarchy = new HashMap<>(conceptsWithParents);
    this.closure = TransitiveClosure.closure(conceptHierarchy);
    conceptSchemes.values().stream()
        .filter(MutableConceptScheme.class::isInstance)
        .map(MutableConceptScheme.class::cast)
        .forEach(mcs -> mcs.setClosure(assign(closure, mcs)));
  }

  private Map<Term, List<Term>> assign(Map<Term, List<Term>> closure, MutableConceptScheme mcs) {
    Map<Term, List<Term>> assignedClosure = new HashMap<>();
    for (Entry<Term, List<Term>> entry : closure.entrySet()) {
      Optional<Term> resolvedChild = mcs.tryGetConcept(entry.getKey().getConceptId());
      resolvedChild.ifPresent(
          term -> assignedClosure.put(
              term,
              entry.getValue().stream()
                  // prefer local
                  .map(p -> mcs.tryGetConcept(p.getConceptId()).orElse(p))
                  .collect(Collectors.toList())));
    }
    return assignedClosure;
  }

  public Map<Term, Set<Term>> getConceptHierarchy() {
    return conceptHierarchy;
  }

  public List<Term> getConceptList(ConceptScheme<Term> conceptScheme) {
    List<Term> trms = getConceptList(conceptScheme.getVersionId());
    if (trms.isEmpty()) {
      trms = getConceptList(conceptScheme.getId());
    }
    return trms;
  }

  public List<Term> getConceptList(URI conceptSchemeURI) {
    if (! conceptSchemes.containsKey(conceptSchemeURI)) {
      return Collections.emptyList();
    }
    return linearize(conceptSchemes.get(conceptSchemeURI).getConcepts(),
        conceptHierarchy);
  }

  private static List<Term> linearize(Stream<Term> concepts,
      Map<Term, Set<Term>> graph) {
    return new HierarchySorter<Term>().linearize(concepts.collect(Collectors.toSet()), graph);
  }

  public Collection<ConceptScheme<Term>> getConceptSchemes() {
    return conceptSchemes.values();
  }

  public Optional<ConceptScheme<Term>> getConceptScheme(URI schemeURI) {
    return Optional.ofNullable(conceptSchemes.get(schemeURI));
  }

  public Collection<ConceptScheme<Term>> getDistinctConceptSchemes() {
    return conceptSchemes.values();
  }

  public Collection<ConceptTermSeries> getConceptSeries(URI schemeURI) {
    return getConceptList(schemeURI).stream()
        .map(ConceptTermSeries::new)
        .collect(Collectors.toList());
  }

  public Collection<String> getSchemeSeries(URI schemeURI) {
    return getConceptSchemes().stream()
        .filter(s -> s.getId().equals(schemeURI))
        .sorted(Comparator.comparing(VersionedIdentifier::getEstablishedOn).reversed())
        .map(ConceptScheme::getVersion)
        .collect(Collectors.toList());
  }

  public List<String> getSchemeReleases(URI schemeURI) {
    return getConceptSchemes().stream()
        .filter(s -> s.getId().equals(schemeURI))
        .sorted(Comparator.comparing(VersionedIdentifier::getEstablishedOn).reversed())
        .map(ConceptScheme::getEstablishedOn)
        .map(DateTimeUtil::format)
        .collect(Collectors.toList());
  }

}
