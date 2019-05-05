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
import edu.mayo.kmdp.util.NameUtils;
import java.net.URI;
import java.util.ArrayList;
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
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
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


public class SkosTerminologyAbstractor {

  static final IRI CONCEPT_SCHEME = iri(SKOS.ConceptScheme);
  static final IRI CONCEPT = iri(SKOS.Concept);
  static final IRI LABEL = iri(SKOS.prefLabel);
  static final IRI IS_DEFINED_BY = iri(RDFS.isDefinedBy);
  static final IRI COMMENT = iri(RDFS.comment);
  static final IRI NOTATION = iri(SKOS.notation);
  static final IRI IN_SCHEME = iri(SKOS.inScheme);
  static final IRI HAS_TOP = iri(SKOS.hasTopConcept);
  static final IRI TOP_OF = iri(SKOS.topConceptOf);
  static final IRI BROADER = iri(SKOS.broader);
  static final IRI BROADER_TRANSITIVE = iri(SKOS.broaderTransitive);

  static final IRI DENOTES = iri("http://www.w3.org/ns/lemon/ontolex#denotes");
  static final IRI IS_CONCEPT_OF = iri("http://www.w3.org/ns/lemon/ontolex#isConceptOf");

  static final IRI OID = iri("https://www.hl7.org/oid");

  static final IRI dceUUID = iri("http://www.opengroup.org/dce/uuid");

  static final IRI dctID = iri(DCTerms.identifier);

  private static IRI iri(Resource res) {
    return IRI.create(res.getURI());
  }

  private static IRI iri(String res) {
    return IRI.create(res);
  }

  public ConceptGraph traverse(OWLOntology o, boolean reason) {
    if (reason) {
      o = this.doReason(o);
    }
    final OWLOntology model = o;

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
        .filter((i) -> !isTopConcept(i, model))
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
        .filter((opax) -> !isTopConcept(opax.getObject(), model))
        // ignore parents that are outside of the recognized schemes (missing assertion, or mireoted parent)
        .filter((opax) -> isInScheme(opax.getObject(), model))
        // register rel
        .forEach((opax) -> addAncestor(resolve(opax.getSubject(), model, codeSystems)
                .orElseThrow(IllegalStateException::new),
            resolve(opax.getObject(), model, codeSystems)
                .orElseThrow(IllegalStateException::new)));

    // finally the Top Concept
    model.individualsInSignature(Imports.INCLUDED)
        .filter((i) -> isConcept(i, model))
        .filter((i) -> isTopConcept(i, model))
        .forEach((top) -> toTopCode(top,
            getOrCreateInSchemes(top, model, codeSystems),
            model));

    return new ConceptGraph(codeSystems, join(codeSystems));
  }

  private boolean isInScheme(OWLIndividual ind, OWLOntology model) {
    return model.importsClosure()
        .flatMap((o) -> o.axioms(AxiomType.OBJECT_PROPERTY_ASSERTION))
        .anyMatch((opax) -> (opax.getSubject().equals(ind) && isProperty(IN_SCHEME, opax)));
  }

  private boolean isTopConcept(OWLIndividual ind, OWLOntology model) {
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


  private Optional<Term> resolve(final OWLIndividual ind, OWLOntology model,
      final Map<URI, ConceptScheme<Term>> codeSystems) {
    return getOrCreateInSchemes(ind.asOWLNamedIndividual(),
        model,
        codeSystems).stream().findAny()
        .map(MutableConceptScheme.class::cast)
        .flatMap((mcs) -> mcs.resolve(getReferent(ind.asOWLNamedIndividual(),
            model)));
  }

  private void addAncestor(Term sub, Term sup) {
    ConceptTerm subCD = (ConceptTerm) sub;
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
    String code = getCodedIdentifier(ind, model);

    // TODO FIXME: check rdfs:label vs skos:prefLabel, and consider that getFragment does not pick /name vs #name
    String label = getAnnotationValues(ind, model, LABEL).findFirst().orElse(uri.getFragment());

    return new MutableConceptScheme(uri, version, code, label);
  }

  private String getCodedIdentifier(OWLNamedIndividual ind, OWLOntology model) {
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
        .map((v) -> IRI
            .create(v.toString() + "#" + NameUtils.getTrailingPart(ind.getIRI().toString()))
            .toURI());
  }


  public Term toCode(OWLNamedIndividual ind,
      Collection<ConceptScheme<Term>> schemes,
      OWLOntology model) {
    return toCode(ind, schemes, model, false);
  }

  public Term toTopCode(OWLNamedIndividual ind,
      Collection<ConceptScheme<Term>> schemes,
      OWLOntology model) {
    return toCode(ind, schemes, model, true);
  }

  protected Term toCode(OWLNamedIndividual ind,
      Collection<ConceptScheme<Term>> schemes,
      OWLOntology model, boolean asTop) {
    if (schemes.size() >= 2) {
      throw new UnsupportedOperationException(
          "TODO: Unable to handle concepts in more than 2 schemes");
    }
    MutableConceptScheme scheme =
        schemes.isEmpty() ? null : (MutableConceptScheme) schemes.iterator().next();

    URI uri = getReferent(ind, model);

    String code = getCodedIdentifier(ind, model);
    String label = getAnnotationValues(ind, model, LABEL).findFirst().orElse(uri.getFragment());
    String comment = getAnnotationValues(ind, model, COMMENT).findFirst().orElse(null);

    Term cd = new ConceptTerm(ind.getIRI().toURI(), code, label, comment, uri, scheme);
    if (scheme != null) {
      if (asTop) {
        scheme.setTop(cd);
      } else {
        scheme.addConcept(cd);
      }
    }
    return cd;
  }

  private boolean isAbstract(Term cd) {
    return cd.getRef().equals(cd.getConceptId());
  }

  private URI getReferent(OWLNamedIndividual ind, OWLOntology model) {
    return getConceptOf(ind, model).orElse(getDefinedBy(ind, model).orElse(getURI(ind)));
  }

  private Optional<URI> getConceptOf(OWLNamedIndividual ind, OWLOntology model) {
    return getPropertyValues(ind, model, IS_CONCEPT_OF)
        .filter(HasIRI.class::isInstance)
        .findAny()
        .map(HasIRI.class::cast)
        .map((s) -> s.getIRI().toURI());
  }

  private Optional<URI> getDefinedBy(OWLNamedIndividual ind, OWLOntology model) {
    return getAnnotationValues(ind, model, IS_DEFINED_BY)
        .findAny()
        .map(URI::create);
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


  private OWLOntology doReason(OWLOntology o) {
    OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
    OWLReasoner owler = reasonerFactory.createReasoner(o);

    InferredOntologyGenerator reasoner = new InferredOntologyGenerator(owler);

    OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();

    reasoner.fillOntology(owlOntologyManager.getOWLDataFactory(), o);
    return o;
  }


  static class MutableConceptScheme extends AnonymousConceptScheme {

    private Set<Term> concepts = new HashSet<>();
    private Map<Term, Set<Term>> parents = new HashMap<>();
    private Term top;
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

    public Map<Term, Set<Term>> getAncestorsMap() {
      return new HashMap<>(parents);
    }

    public Optional<Term> resolve(URI uri) {
      return concepts.stream()
          .filter((cd) -> cd.getRef().equals(uri))
          .findAny();
    }

    public void setClosure(Map<Term, List<Term>> closure) {
      this.closure = closure;
    }

    public List<Term> getClosure(Term cd) {
      return closure.containsKey(cd) ? Collections.unmodifiableList(closure.get(cd))
          : Collections.emptyList();
    }
  }

  public class ConceptGraph {

    private Map<URI, ConceptScheme<Term>> conceptSchemes;
    private Map<Term, Set<Term>> conceptHierarchy;
    private Map<Term, List<Term>> closure;

    public ConceptGraph(Map<URI, ConceptScheme<Term>> conceptSchemes,
        Map<Term, Set<Term>> concepts) {
      this.conceptSchemes = new HashMap<>(conceptSchemes);
      this.conceptHierarchy = new HashMap<>(concepts);
      this.closure = TransitiveClosure.closure(conceptHierarchy);
      conceptSchemes.values().stream()
          .filter(MutableConceptScheme.class::isInstance)
          .map(MutableConceptScheme.class::cast)
          .forEach((mcs) -> mcs.setClosure(closure));
    }

    public Map<Term, Set<Term>> getConceptHierarchy() {
      return conceptHierarchy;
    }

    public List<Term> getConceptList(URI conceptSchemeURI) {
      return linearize(conceptSchemes.get(conceptSchemeURI).getConcepts(),
          conceptHierarchy);
    }

    public Collection<ConceptScheme<Term>> getConceptSchemes() {
      return conceptSchemes.values();
    }

    public Optional<ConceptScheme<Term>> getConceptScheme(URI schemeURI) {
      return Optional.ofNullable(conceptSchemes.get(schemeURI));
    }
  }

  public static class ConceptTerm extends InternalTerm {

    public ConceptTerm(URI conceptURI, String code, String label, String comment, URI refUri,
        ConceptScheme<Term> scheme) {
      super(conceptURI, code, label, comment, refUri, scheme);
    }

    public String getTermConceptName() {
      return edu.mayo.kmdp.util.NameUtils.getTermConceptName(tag, label);
    }

    public List<Term> getAncestors() {
      return new ArrayList<>(((MutableConceptScheme) scheme).getAncestors(this));
    }

    public List<Term> getClosure() {
      return new ArrayList<>(((MutableConceptScheme) scheme).getClosure(this));
    }

  }

}
