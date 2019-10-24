package edu.mayo.kmdp.terms.generator.internal;


import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.impl.model.AnonymousConceptScheme;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class MutableConceptScheme extends AnonymousConceptScheme {

  private Set<Term> concepts = new HashSet<>();
  private Map<Term, Set<Term>> parents = new HashMap<>();
  private Term top;
  // set when creating a graph
  private Map<Term, List<Term>> closure;

  public MutableConceptScheme(URI uri, URI version, String code, String label) {
    super(code, label, uri, version);
  }

  public void setTop(Term top) {
    this.top = top;
  }

  @Override
  public Optional<Term> getTopConcept() {
    return Optional.ofNullable(top);
  }

  public void addConcept(Term cd) {
    this.concepts.add(cd);
  }

  public void addParent(Term child, Term parent) {
    if (!parents.containsKey(child)) {
      parents.put(child, new HashSet<>());
    }
    parents.get(child).add(parent);
  }

  @Override
  public Stream<Term> getConcepts() {
    return concepts.stream();
  }

  public Set<Term> getAncestors(Term cd) {
    return parents.containsKey(cd) ? Collections.unmodifiableSet(parents.get(cd))
        : Collections.emptySet();
  }

  public Stream<Term> streamAncestors(Term cd) {
    return getAncestors(cd).stream();
  }

  public Map<Term, Set<Term>> getAncestorsMap() {
    return new HashMap<>(parents);
  }

  public Optional<Term> resolve(URI uri) {
    return concepts.stream()
        .filter(cd -> cd.getConceptId().equals(uri))
        .findAny();
  }

  void setClosure(Map<Term, List<Term>> closure) {
    this.closure = closure;
  }

  List<Term> getClosure(Term cd) {
    return closure.containsKey(cd) ? Collections.unmodifiableList(closure.get(cd))
        : Collections.emptyList();
  }

  public MutableConceptScheme(MutableConceptScheme other) {
    this(
        other.getId(),
        other.getVersionId(),
        other.getTag(),
        other.getLabel());

    setTop(
        other.getTopConcept()
            .map(ConceptTerm.class::cast)
            .map(c -> c.cloneInto(this))
            .orElse(null));

    other.getConcepts()
        .map(ConceptTerm.class::cast)
        .map(ct -> ct.cloneInto(this))
        .map(ConceptTerm.class::cast)
        .forEach(this::addConcept);

    other.getAncestorsMap().forEach(
        (trm, anc) -> anc.forEach(a -> {
          Term child = this.getConcepts()
              .filter(c -> c.equals(trm))
              .findFirst()
              .orElseThrow(IllegalStateException::new);
          this.addParent(child, a);
        }));

    closure = new HashMap<>();
  }

  @Override
  public String toString() {
    return "MutableConceptScheme{" +
        "label='" + label + '\'' +
        ", tag='" + tag + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof MutableConceptScheme &&
        getId().equals(((MutableConceptScheme) other).getId());
  }

  @Override
  public int hashCode() {
    return getId().hashCode();
  }

  Optional<Term> tryGetConcept(URI conceptId) {
    return getConcepts()
        .filter(c -> c.getConceptId().equals(conceptId))
        .findFirst();
  }

  public Term getConcept(URI conceptId) {
    return getConcepts()
        .filter(c -> c.getConceptId().equals(conceptId))
        .findFirst()
        .orElseThrow(IllegalStateException::new);
  }

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    throw new UnsupportedOperationException(
        "MutableConceptSchemes should only be used at compile time");
  }

  private void readObject(java.io.ObjectInputStream in)
      throws IOException, ClassNotFoundException {
    throw new UnsupportedOperationException(
        "MutableConceptSchemes should only be used at compile time");
  }
}
