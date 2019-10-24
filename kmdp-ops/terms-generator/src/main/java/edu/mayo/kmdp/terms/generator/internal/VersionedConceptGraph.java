package edu.mayo.kmdp.terms.generator.internal;


import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.ConceptScheme;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class VersionedConceptGraph extends ConceptGraph {

  public VersionedConceptGraph(ConceptGraph other) {
    merge(other);
  }

  public VersionedConceptGraph merge(ConceptGraph other) {
    other.conceptSchemes
        .forEach((uri, scheme) -> this.conceptSchemes.put(scheme.getVersionId(), scheme));
    this.conceptHierarchy.putAll(other.conceptHierarchy);
    this.closure.putAll(other.closure);
    return this;
  }

  @Override
  public Collection<ConceptScheme<Term>> getDistinctConceptSchemes() {
    return conceptSchemes.values().stream()
        .filter(distinctByKey(ConceptScheme::getId))
        .collect(Collectors.toSet());
  }

  public Collection<ConceptTermSeries> getConceptSeries(URI schemeURI) {
    Map<URI, ConceptTermSeries> series = new HashMap<>();

    List<ConceptScheme<Term>> schemeVersions = conceptSchemes.values().stream()
        .filter(s -> s.getId().equals(schemeURI))
        .collect(Collectors.toList());

    schemeVersions.forEach(cs ->
        cs.getConcepts().forEach(con -> {
          ConceptTerm trm = (ConceptTerm) con;
          if (!series.containsKey(trm.getConceptId())) {
            series.put(trm.getConceptId(), new ConceptTermSeries(trm));
          } else {
            series.get(trm.getConceptId()).addMember(trm);
          }
        }));
    return series.values();
  }

  private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
    Set<Object> seen = ConcurrentHashMap.newKeySet();
    return t -> seen.add(keyExtractor.apply(t));
  }
}
