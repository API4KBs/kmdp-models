/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.mayo.kmdp.terms.generator;

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.ConceptScheme;
import edu.mayo.kmdp.terms.generator.util.HierarchySorter;
import edu.mayo.kmdp.terms.generator.util.TransitiveClosure;
import edu.mayo.kmdp.terms.impl.model.AnonymousConceptScheme;
import edu.mayo.kmdp.terms.impl.model.InternalTerm;
import org.apache.jena.vocabulary.RDFS;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AsOWLNamedIndividual;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.HasIRI;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class SkosTerminologyAbstractor {

  static final String SKOS_NAMESPACE = "http://www.w3.org/2004/02/skos/core#";

  static final IRI CONCEPT_SCHEME = IRI.create(SKOS_NAMESPACE + "ConceptScheme");
  static final IRI CONCEPT = IRI.create(SKOS_NAMESPACE + "Concept");
  static final IRI LABEL = IRI.create(SKOS_NAMESPACE + "prefLabel");
  static final IRI COMMENT = IRI.create(RDFS.comment.getURI());
  static final IRI NOTATION = IRI.create(SKOS_NAMESPACE + "notation");
  static final IRI IN_SCHEME = IRI.create(SKOS_NAMESPACE + "inScheme");
  static final IRI HAS_TOP = IRI.create(SKOS_NAMESPACE + "hasTopConcept");
  static final IRI TOP_OF = IRI.create(SKOS_NAMESPACE + "topConceptOf");
  static final IRI BROADER = IRI.create(SKOS_NAMESPACE + "broader");
  static final IRI BROADER_TRANSITIVE = IRI.create(SKOS_NAMESPACE + "broaderTransitive");

  static final IRI DENOTES = IRI.create("http://www.w3.org/ns/lemon/ontolex#denotes");
  static final IRI IS_CONCEPT_OF = IRI.create("http://www.w3.org/ns/lemon/ontolex#isConceptOf");

  static final IRI OID = IRI.create("https://www.hl7.org/oid");

  static final IRI dceUUID = IRI.create("http://www.opengroup.org/dce/uuid");

  static final IRI dcID = IRI.create("http://purl.org/dc/terms/identifier");


  private OWLOntology model;

  public SkosTerminologyAbstractor(OWLOntology o, boolean reason) {
    this.model = o;
    if (reason) {
      this.doReason(o);
    }
  }

  public ConceptGraph traverse() {
    Map<URI, ConceptScheme<Term>> codeSystems;

    // build the code systems first
    codeSystems = model.individualsInSignature(Imports.INCLUDED)
        .filter((i) -> isConceptScheme(i, model))
        .map(x -> toScheme(x, model))
        .collect(Collectors.toMap(ConceptScheme::getId,
            Function.identity()));

    // then the concepts
    model.individualsInSignature(Imports.INCLUDED)
        .filter((i) -> isConcept(i, model))
        .forEach((ind) -> toCode(ind,
            getOrCreateInSchemes(ind, model, codeSystems),
            model));

    // finally the relationships
    model.individualsInSignature(Imports.INCLUDED)
        // concepts only
        .filter((i) -> isConcept(i, model))
        // get property assertions
        .flatMap(model::objectPropertyAssertionAxioms)
        // restrict to 'broader', on named concepts, avoid reflexivity
        .filter((opax) -> opax.getProperty().isObjectPropertyExpression())
        .filter((opax) -> (isProperty(BROADER, opax) || isProperty(BROADER_TRANSITIVE, opax)))
        .filter((opax) -> (
            !opax.getSubject().equals(opax.getObject())
                && opax.getSubject().isIndividual() && opax.getObject().isNamed())
            && opax.getSubject().isIndividual() && opax.getObject().isNamed()
        )
        // ignore (mock) top concepts
        .filter((opax) -> !isTopConcept(opax.getObject()))
        // ignore parents that are outside of the recognized schemes (missing assertion, or mireoted parent)
        .filter((opax) -> isInScheme(opax.getObject()))
        // register rel
        .forEach((opax) -> addAncestor(resolve(opax.getSubject(), codeSystems)
                .orElseThrow(IllegalStateException::new),
            resolve(opax.getObject(), codeSystems)
                .orElseThrow(IllegalStateException::new)));

    return new ConceptGraph(codeSystems, join(codeSystems));
  }

  private boolean isInScheme(OWLIndividual ind) {
    return model.importsClosure()
        .flatMap((o) -> o.axioms(AxiomType.OBJECT_PROPERTY_ASSERTION))
        .anyMatch((opax) -> (opax.getSubject().equals(ind) && isProperty(IN_SCHEME, opax)));
  }

  private boolean isTopConcept(OWLIndividual ind) {
    return model.importsClosure()
        .flatMap((o) -> o.axioms(AxiomType.OBJECT_PROPERTY_ASSERTION))
        .anyMatch((opax) -> (opax.getSubject().equals(ind) && isProperty(TOP_OF, opax)
            || opax.getObject().equals(ind) && isProperty(HAS_TOP, opax)));
  }

  private Map<Term, Set<Term>> join(Map<URI, ConceptScheme<Term>> conceptSchemes) {
    return conceptSchemes.values().stream()
        .map((s) -> ((MutableConceptScheme) s).getAncestorsMap())
        .reduce(new HashMap<>(), this::mergeMaps);
  }

  protected List<Term> linearize(Stream<Term> concepts,
      Map<Term, Set<Term>> graph) {
    return new HierarchySorter<Term>().linearize(concepts.collect(Collectors.toSet()), graph);
  }


  protected Map<Term, Set<Term>> mergeMaps(Map<Term, Set<Term>> m1,
      Map<Term, Set<Term>> m2) {
    m1.putAll(m2);
    return m1;
  }


  private Optional<Term> resolve(final OWLIndividual ind,
      final Map<URI, ConceptScheme<Term>> codeSystems) {
    return getOrCreateInSchemes(ind.asOWLNamedIndividual(),
        model,
        codeSystems).stream().findAny()
        .map(MutableConceptScheme.class::cast)
        .flatMap((mcs) -> mcs.resolve(getReferent(ind.asOWLNamedIndividual(),
            model)));
  }

  private void addAncestor(Term sub, Term sup) {
    InternalTerm subCD = (InternalTerm) sub;
    MutableConceptScheme subMCS = (MutableConceptScheme) subCD.getScheme();

    subMCS.addParent(sub, sup);
  }


  private boolean isProperty(IRI propIRI, OWLObjectPropertyAssertionAxiom opax) {
    return propIRI.equals(opax.getProperty().asOWLObjectProperty().getNamedProperty().getIRI());
  }

  private Collection<ConceptScheme<Term>> getOrCreateInSchemes(OWLNamedIndividual ind,
      OWLOntology model,
      Map<URI, ConceptScheme<Term>> codeSystems) {
    return getPropertyValues(ind, model, IN_SCHEME)
        .map(AsOWLNamedIndividual::asOWLNamedIndividual)
        .map((sch) -> codeSystems.getOrDefault(getURI(sch), toScheme(sch, model)))
        .collect(Collectors.toSet());
  }

  public ConceptScheme<Term> toScheme(OWLNamedIndividual ind, OWLOntology model) {
    URI uri = getURI(ind);
    URI version = applyVersion(ind, model).orElse(uri);
    String code = getCodedIdentifier(ind);

    // TODO FIXME: check rdfs:label vs skos:prefLabel, and consider that getFragment does not pick /name vs #name
    String label = getAnnotationValues(ind, model, LABEL).findFirst().orElse(uri.getFragment());

    return new MutableConceptScheme(uri, version, code, label);
  }

  private String getCodedIdentifier(OWLNamedIndividual ind) {
    Set<OWLLiteral> notations = getDataValues(ind, model, NOTATION).collect(Collectors.toSet());

    if (!notations.isEmpty()) {
      return notations.stream()
          .filter((lit) -> dceUUID.equals(lit.getDatatype().getIRI()))
          .findFirst()
          .map(OWLLiteral::getLiteral)
          .orElse(notations.iterator().next().getLiteral());
    } else {
      return getAnnotationValues(ind, model, OID).findFirst()
          .orElse(getURI(ind).getFragment());
    }
  }

  private Optional<URI> applyVersion(OWLNamedIndividual ind, OWLOntology model) {
    return model.getOntologyID().getVersionIRI()
        .map((v) -> IRI.create(v.toString(), "#" + ind.getIRI().getRemainder().get()).toURI());
  }

  public Term toCode(OWLNamedIndividual ind,
      Collection<ConceptScheme<Term>> schemes,
      OWLOntology model) {
    if (schemes.size() >= 2) {
      throw new UnsupportedOperationException(
          "TODO: Unable to handle concepts in more than 2 schemes");
    }
    MutableConceptScheme scheme =
        schemes.isEmpty() ? null : (MutableConceptScheme) schemes.iterator().next();

    URI uri = getReferent(ind, model);

    String code = getCodedIdentifier(ind);
    String label = getAnnotationValues(ind, model, LABEL).findFirst().orElse(uri.getFragment());
    String comment = getAnnotationValues(ind, model, COMMENT).findFirst().orElse(null);

    Term cd = new InternalTerm(ind.getIRI().toURI(), code, label, comment, uri, scheme);
    if (scheme != null) {
      scheme.addConcept(cd);
    }
    return cd;
  }

  private URI getReferent(OWLNamedIndividual ind, OWLOntology model) {
    return getConceptOf(ind, model).orElse(getURI(ind));
  }

  private Optional<URI> getConceptOf(OWLNamedIndividual ind, OWLOntology model) {
    return getPropertyValues(ind, model, IS_CONCEPT_OF)
        .filter(HasIRI.class::isInstance)
        .findAny()
        .map(HasIRI.class::cast)
        .map((s) -> s.getIRI().toURI());
  }

  private URI getURI(HasIRI x) {
    return URI.create(x.getIRI().toString());
  }

  private boolean isConcept(OWLNamedIndividual ind, OWLOntology model) {
    return is(ind, CONCEPT, model);
  }

  private boolean isConceptScheme(OWLNamedIndividual ind, OWLOntology model) {
    return is(ind, CONCEPT_SCHEME, model);
  }

  private boolean is(OWLNamedIndividual ind, IRI type, OWLOntology model) {
    return EntitySearcher.getTypes(ind, model.importsClosure())
        .anyMatch((kls) -> !kls.isAnonymous() && kls.asOWLClass().getIRI().equals(type));
  }

  private Stream<OWLIndividual> getPropertyValues(OWLNamedIndividual ind, OWLOntology model,
      IRI prop) {
    OWLObjectProperty p = model.getOWLOntologyManager().getOWLDataFactory()
        .getOWLObjectProperty(prop);
    return EntitySearcher.getObjectPropertyValues(ind,
        p,
        model.importsClosure());
  }


  private Stream<String> getAnnotationValues(OWLNamedIndividual ind, OWLOntology model, IRI prop) {
    OWLAnnotationProperty p = model.getOWLOntologyManager().getOWLDataFactory()
        .getOWLAnnotationProperty(prop);
    List<OWLAnnotation> allAnnos = EntitySearcher.getAnnotations(ind, model)
        .collect(Collectors.toList());
    return EntitySearcher.getAnnotationObjects(ind, model.importsClosure(), p)
        .map(OWLAnnotation::getValue)
        .map(OWLAnnotationValue::asLiteral)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(OWLLiteral::getLiteral);
  }

  private Stream<OWLLiteral> getDataValues(OWLNamedIndividual ind, OWLOntology model, IRI prop) {
    OWLDataProperty p = model.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(prop);
    return EntitySearcher.getDataPropertyValues(ind, p, model.importsClosure());
  }


  private void doReason(OWLOntology o) {
    OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
    OWLReasoner owler = reasonerFactory.createReasoner(o);

    InferredOntologyGenerator reasoner = new InferredOntologyGenerator(owler);

    OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();

    reasoner.fillOntology(owlOntologyManager.getOWLDataFactory(), o);
  }


  static class MutableConceptScheme extends AnonymousConceptScheme {

    private Set<Term> concepts = new HashSet<>();
    private Map<Term, Set<Term>> parents = new HashMap<>();

    public MutableConceptScheme(URI uri, URI version, String code, String label) {
      super(code, label, uri, version);
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
      return Collections.unmodifiableSet(parents.get(cd));
    }

    public Map<Term, Set<Term>> getAncestorsMap() {
      return new HashMap<>(parents);
    }

    public Optional<Term> resolve(URI uri) {
      return concepts.stream()
          .filter((cd) -> cd.getRef().equals(uri))
          .findAny();
    }


  }

  public class ConceptGraph {

    private Map<URI, ConceptScheme<Term>> conceptSchemes;
    private Map<Term, Set<Term>> conceptHierarchy;

    public ConceptGraph(Map<URI, ConceptScheme<Term>> conceptSchemes,
        Map<Term, Set<Term>> concepts) {
      this.conceptSchemes = new HashMap<>(conceptSchemes);
      this.conceptHierarchy = new HashMap<>(concepts);
    }

    public Map<Term, Set<Term>> getConceptHierarchy() {
      return conceptHierarchy;
    }

    public List<Term> getConceptList(URI conceptSchemeURI) {
      return linearize(conceptSchemes.get(conceptSchemeURI).getConcepts(),
          conceptHierarchy);
    }

    public Map<Term, List<Term>> getClosure() {
      return TransitiveClosure.closure(conceptHierarchy);
    }

    public Collection<ConceptScheme<Term>> getConceptSchemes() {
      return conceptSchemes.values();
    }

    public Optional<ConceptScheme<Term>> getConceptScheme(URI schemeURI) {
      return Optional.ofNullable(conceptSchemes.get(schemeURI));
    }
  }

}
