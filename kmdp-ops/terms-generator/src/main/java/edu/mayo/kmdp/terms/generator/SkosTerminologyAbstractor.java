/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp.terms.generator;

import static edu.mayo.kmdp.util.Util.isUUID;

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.ConceptScheme;
import edu.mayo.kmdp.terms.generator.config.SkosAbstractionConfig;
import edu.mayo.kmdp.terms.generator.config.SkosAbstractionConfig.CLOSURE_MODE;
import edu.mayo.kmdp.terms.generator.config.SkosAbstractionConfig.SkosAbstractionParameters;
import edu.mayo.kmdp.terms.generator.internal.ConceptGraph;
import edu.mayo.kmdp.terms.generator.internal.ConceptTerm;
import edu.mayo.kmdp.terms.generator.internal.MutableConceptScheme;
import edu.mayo.kmdp.terms.generator.util.HierarchySorter;
import edu.mayo.kmdp.util.NameUtils;
import edu.mayo.kmdp.util.Util;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;
import org.semanticweb.HermiT.Configuration;
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
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;


public class SkosTerminologyAbstractor {

  private static final IRI CONCEPT_SCHEME = iri(SKOS.ConceptScheme);
  private static final IRI CONCEPT = iri(SKOS.Concept);
  private static final IRI LABEL = iri(SKOS.prefLabel);
  private static final IRI IS_DEFINED_BY = iri(RDFS.isDefinedBy);
  private static final IRI COMMENT = iri(RDFS.comment);
  private static final IRI NOTATION = iri(SKOS.notation);
  private static final IRI IN_SCHEME = iri(SKOS.inScheme);
  private static final IRI HAS_TOP = iri(SKOS.hasTopConcept);
  private static final IRI TOP_OF = iri(SKOS.topConceptOf);
  private static final IRI BROADER = iri(SKOS.broader);
  private static final IRI BROADER_TRANSITIVE = iri(SKOS.broaderTransitive);
  private static final IRI VERSION = iri(OWL2.versionInfo);

  private static final IRI IS_CONCEPT_OF = iri("http://www.w3.org/ns/lemon/ontolex#isConceptOf");

  private static final IRI OID = iri("https://www.hl7.org/oid");

  private static IRI iri(Resource res) {
    return IRI.create(res.getURI());
  }

  private static IRI iri(String res) {
    return IRI.create(res);
  }

  private SkosAbstractionConfig cfg;

  public ConceptGraph traverse(OWLOntology o) {
    return traverse(o, new SkosAbstractionConfig());
  }

  public ConceptGraph traverse(OWLOntology o, SkosAbstractionConfig config) {
    cfg = config != null ? config : new SkosAbstractionConfig();

    if (cfg.getTyped(SkosAbstractionParameters.REASON)) {
      this.doReason(o);
    }
    final OWLOntology model = o;

    Map<URI, ConceptScheme<Term>> codeSystems;

    // build the code systems first
    codeSystems = model.individualsInSignature(Imports.INCLUDED)
        .filter(i -> isConceptScheme(i, model))
        .map(x -> toScheme(x, model))
        .collect(Collectors.toMap(ConceptScheme::getId,
            Function.identity()));

    // then the concepts
    model.individualsInSignature(Imports.INCLUDED)
        .filter(i -> isConcept(i, model))
        .filter(i -> !isTopConcept(i, model))
        .forEach(ind -> toCode(ind,
            getOrCreateInSchemes(ind, model, codeSystems),
            model));

    // finally the relationships
    model.individualsInSignature(Imports.INCLUDED)
        // concepts only
        .filter(i -> isConcept(i, model))
        // get property assertions
        .flatMap(i -> getAllObjectPropertyAssertions(i, model))
        // restrict to 'broader', on named concepts, avoid reflexivity
        .filter(opax -> opax.getProperty().isObjectPropertyExpression())
        .filter(opax -> (isProperty(BROADER, opax) || isProperty(BROADER_TRANSITIVE, opax)))
        .filter(opax -> (
            !opax.getSubject().equals(opax.getObject())
                && opax.getSubject().isIndividual() && opax.getObject().isNamed())
            && opax.getSubject().isIndividual() && opax.getObject().isNamed())
        // ignore (mock) top concepts
        .filter(opax -> !isTopConcept(opax.getObject(), model))
        // ignore parents that are outside of the recognized schemes (missing assertion, or mireoted parent)
        .filter(opax -> isInScheme(opax.getObject(), model))
        // register rel
        .forEach(opax -> addAncestor(
            resolve(opax.getSubject(), model, codeSystems)
                .orElseThrow(() -> new IllegalStateException(
                    "Unresolvable Child Concept " + opax.getSubject())),
            resolve(opax.getObject(), model, codeSystems)
                .orElseThrow(() -> new IllegalStateException(
                    "Unresolvable Parent Concept " + opax.getObject()))));

    // finally the Top Concept
    model.individualsInSignature(Imports.INCLUDED)
        .filter(i -> isConcept(i, model))
        .filter(i -> isTopConcept(i, model))
        .forEach(top -> toTopCode(top,
            getOrCreateInSchemes(top, model, codeSystems),
            model));

    Map<Term, Set<Term>> parents = join(codeSystems);

    ConceptGraph graph = new ConceptGraph(codeSystems, parents);

    return applyClosure(graph, cfg.getTyped(SkosAbstractionParameters.CLOSURE_MODE));
  }

  private Stream<OWLObjectPropertyAssertionAxiom> getAllObjectPropertyAssertions(
      OWLNamedIndividual i, OWLOntology model) {
    return model.importsClosure()
        .flatMap(o -> o.objectPropertyAssertionAxioms(i))
        .collect(Collectors.toSet())
        .stream();
  }

  private boolean isInScheme(OWLIndividual ind, OWLOntology model) {
    return model.importsClosure()
        .flatMap(o -> o.axioms(AxiomType.OBJECT_PROPERTY_ASSERTION))
        .anyMatch(opax -> (opax.getSubject().equals(ind) && isProperty(IN_SCHEME, opax)));
  }

  private boolean isTopConcept(OWLIndividual ind, OWLOntology model) {
    return model.importsClosure()
        .flatMap(o -> o.axioms(AxiomType.OBJECT_PROPERTY_ASSERTION))
        .anyMatch(opax -> (opax.getSubject().equals(ind) && isProperty(TOP_OF, opax)
            || opax.getObject().equals(ind) && isProperty(HAS_TOP, opax)));
  }

  private static Map<Term, Set<Term>> join(Map<URI, ConceptScheme<Term>> conceptSchemes) {
    return conceptSchemes.values().stream()
        .map(s -> ((MutableConceptScheme) s).getAncestorsMap())
        .reduce(new HashMap<>(), SkosTerminologyAbstractor::mergeMaps);
  }


  private static Map<Term, Set<Term>> mergeMaps(Map<Term, Set<Term>> m1,
      Map<Term, Set<Term>> m2) {
    m1.putAll(m2);
    return m1;
  }


  private Optional<Term> resolve(final OWLIndividual ind, OWLOntology model,
      final Map<URI, ConceptScheme<Term>> codeSystems) {
    Optional<ConceptScheme<Term>> parentScheme = getOrCreateInSchemes(
        ind.asOWLNamedIndividual(),
        model,
        codeSystems)
        .stream()
        .findAny();

    URI uri = getURI(ind.asOWLNamedIndividual());

    return parentScheme.map(MutableConceptScheme.class::cast)
        .flatMap(mcs -> mcs.resolve(uri));
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

    Collection<ConceptScheme<Term>> schems = getPropertyValues(ind, model, IN_SCHEME)
        .map(AsOWLNamedIndividual::asOWLNamedIndividual)
        .map(sch -> codeSystems.getOrDefault(getURI(sch), toScheme(sch, model)))
        .collect(Collectors.toSet());

    boolean enforceClosure = cfg.getTyped(SkosAbstractionParameters.ENFORCE_CLOSURE);
    if (enforceClosure && schems.isEmpty()) {
      schems = getPropertyValues(ind, model, BROADER)
          .filter(parent -> isTopConcept(parent, model))
          .flatMap(top -> getSchemesForTopConcept(top, model))
          .map(AsOWLNamedIndividual::asOWLNamedIndividual)
          .map(sch -> codeSystems.getOrDefault(getURI(sch), toScheme(sch, model)))
          .collect(Collectors.toSet());
    }

    return schems;
  }

  private Stream<OWLIndividual> getSchemesForTopConcept(OWLIndividual top, OWLOntology model) {
    Set<OWLIndividual> schemes1 = getPropertyValues(top.asOWLNamedIndividual(), model, TOP_OF)
        .collect(Collectors.toSet());
    Set<OWLIndividual> schemes2 = model.individualsInSignature(Imports.INCLUDED)
        .filter(ind -> isConceptScheme(ind, model))
        .filter(sch -> getPropertyValues(sch, model, HAS_TOP).anyMatch(t -> t.equals(top)))
        .collect(Collectors.toSet());
    schemes1.addAll(schemes2);
    return schemes1.stream();
  }

  public ConceptScheme<Term> toScheme(OWLNamedIndividual ind, OWLOntology model) {
    URI uri = getURI(ind);
    URI version = applyVersion(ind, model).orElse(uri);
    String code = getCodedIdentifiers(ind, model).get(0);

    String label = getAnnotationValues(ind, model, LABEL).findFirst().orElse(uri.getFragment());
    return new MutableConceptScheme(uri, version, code, label);
  }

  private List<String> getCodedIdentifiers(OWLNamedIndividual ind, OWLOntology model) {
    Set<OWLLiteral> notations = getDataValues(ind, model, NOTATION).collect(Collectors.toSet());
    Optional<String> version = getAnnotationValues(ind, model, VERSION).findFirst()
        .map(Object::toString);

    if (!notations.isEmpty()) {
      String code = notations.stream()
          .filter(lit -> cfg.getTyped(SkosAbstractionParameters.TAG_TYPE)
              .equals(lit.getDatatype().getIRI().toURI()))
          .findFirst()
          .map(OWLLiteral::getLiteral)
          .orElse(notations.iterator().next().getLiteral());
      String primaryId = version.map(ver -> code + "-" + ver).orElse(code);

      Set<String> aliases = notations.stream().map(OWLLiteral::getLiteral)
          .collect(Collectors.toSet());
      aliases.remove(code);

      LinkedList<String> tags = new LinkedList<>(aliases);
      tags.addFirst(primaryId);
      return tags;
    } else {
      return Collections.singletonList(getAnnotationValues(ind, model, OID).findFirst()
          .orElse(getURI(ind).getFragment()));
    }
  }

  private Optional<URI> applyVersion(OWLNamedIndividual ind, OWLOntology model) {
    String ontoUri = model.getOntologyID().getOntologyIRI().map(IRI::toString).orElse("");
    String versionUri = model.getOntologyID().getVersionIRI().map(IRI::toString).orElse("");
    String versionFragment = NameUtils.strip(ontoUri, versionUri);

    String indURI = ind.getIRI().toString();
    String localId = NameUtils.getTrailingPart(ind.getIRI().toString());

    return model.getOntologyID().getVersionIRI()
        .map(v -> IRI
            .create(indURI.substring(0, indURI.lastIndexOf(localId) - 1)
                + (versionFragment.startsWith("/") ? versionFragment : "/" + versionFragment)
                + "#" + localId)
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

  private Term toCode(OWLNamedIndividual ind,
      Collection<ConceptScheme<Term>> schemes,
      OWLOntology model, boolean asTop) {
    if (schemes.size() >= 2) {
      throw new UnsupportedOperationException(
          "TODO: Unable to handle concepts in more than 2 schemes");
    }
    MutableConceptScheme scheme =
        schemes.isEmpty() ? null : (MutableConceptScheme) schemes.iterator().next();

    List<String> codes = getCodedIdentifiers(ind, model);
    URI referentUri = getReferent(ind, model);
    String comment = getAnnotationValues(ind, model, COMMENT).findFirst().orElse(null);
    String label = getAnnotationValues(ind, model, LABEL).findFirst()
        .orElse(referentUri.getFragment());

    URI conceptId = ind.getIRI().toURI();
    String tag = codes.get(0);
    UUID uuid = makeUUID(conceptId);
    Term cd = new ConceptTerm(
        conceptId,
        tag,
        label,
        comment,
        referentUri,
        scheme,
        uuid,
        codes);
    if (scheme != null) {
      if (asTop) {
        scheme.setTop(cd);
      } else {
        scheme.addConcept(cd);
      }
    }
    return cd;
  }

  private UUID makeUUID(URI conceptId) {
    String id = NameUtils.getTrailingPart(conceptId.toString());
    if (id == null) {
      return UUID.nameUUIDFromBytes(conceptId.toString().getBytes());
    }
    return isUUID(id)
        ? Util.ensureUUID(id).orElseThrow(IllegalStateException::new)
        : UUID.nameUUIDFromBytes(id.getBytes());
  }

  private URI getReferent(OWLNamedIndividual ind, OWLOntology model) {
    return getConceptOf(ind, model)
        .orElse(getDefinedBy(ind, model)
            .orElse(getURI(ind)));
  }

  private Optional<URI> getConceptOf(OWLNamedIndividual ind, OWLOntology model) {
    return getPropertyValues(ind, model, IS_CONCEPT_OF)
        .filter(HasIRI.class::isInstance)
        .findAny()
        .map(HasIRI.class::cast)
        .map(s -> s.getIRI().toURI());
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
        .anyMatch(kls -> !kls.isAnonymous() && kls.asOWLClass().getIRI().equals(type));
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
        .flatMap(Util::trimStream)
        .map(OWLLiteral::getLiteral);
  }

  private Stream<OWLLiteral> getDataValues(OWLNamedIndividual ind, OWLOntology model, IRI prop) {
    OWLDataProperty p = model.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(prop);
    return EntitySearcher.getDataPropertyValues(ind, p, model.importsClosure());
  }


  private void doReason(OWLOntology o) {
    OWLReasoner owler = new Reasoner(new Configuration(), o);

    InferredOntologyGenerator reasoner = new InferredOntologyGenerator(owler);

    OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();

    reasoner.fillOntology(owlOntologyManager.getOWLDataFactory(), o);
  }





  private static ConceptGraph applyClosure(ConceptGraph graph, CLOSURE_MODE closureMode) {
    if (closureMode == CLOSURE_MODE.IMPORTS) {
      // Already implied
      return graph;
    }

    // detect all cross-scheme dependencies
    Map<ConceptScheme<Term>, Set<ConceptScheme<Term>>> dependencies = new HashMap<>();
    graph.getConceptSchemes().stream()
        .map(MutableConceptScheme.class::cast)
        .forEach(cs -> cs.getConcepts()
            .flatMap(cs::streamAncestors)
            .map(ConceptTerm.class::cast)
            .filter(a -> !a.getScheme().equals(cs))
            .forEach(a -> {
              if (!dependencies.containsKey(cs)) {
                dependencies.put(cs, new HashSet<>());
              }
              dependencies.get(cs).add(a.getScheme());
            })
        );

    // sort in case of transitive dependencies
    HierarchySorter<ConceptScheme<Term>> sorter = new HierarchySorter<>();
    List<ConceptScheme<Term>> sortedSchemes = sorter
        .linearize(graph.getConceptSchemes(), dependencies);

    List<MutableConceptScheme> clonedSchemes = sortedSchemes.stream()
        .map(MutableConceptScheme.class::cast)
        .map(MutableConceptScheme::new)
        .collect(Collectors.toList());

    // Include Concepts from other schemes
    clonedSchemes.forEach(src ->
        dependencies.keySet().stream()
            .filter(cs -> cs.equals(src))
            .map(dependencies::get)
            .forEach(tgtDeps -> {
              for (ConceptScheme<Term> tgtDep : tgtDeps) {
                // add the individuals
                tgtDep.getConcepts()
                    .map(ConceptTerm.class::cast)
                    .forEach(c -> src.addConcept(c.cloneInto(src)));
                // add the parent/child relationships
                ((MutableConceptScheme) tgtDep).getAncestorsMap()
                    .forEach((con, parents) -> {
                      Term mapped = src.getConcept(con.getConceptId());
                      parents.forEach(par ->
                          src.addParent(mapped, src.getConcept(par.getConceptId())));
                    });
              }
            }));

    // Now rewrite the parents to point to the internal concept
    clonedSchemes.forEach(src ->
        src.getAncestorsMap().forEach((trm, parents) -> {
          Set<Term> includedParents = parents.stream()
              .filter(prn -> src.getConcepts().anyMatch(c -> c.getConceptId().equals(prn.getConceptId())))
              .filter(prn -> !((ConceptTerm) prn).getScheme().equals(src))
              .collect(Collectors.toSet());
          parents.removeAll(includedParents);
          includedParents.forEach(p -> parents.add(src.getConcept(p.getConceptId())));
        }));

    Map<URI, ConceptScheme<Term>> codeSystems = clonedSchemes.stream()
        .collect(Collectors.toMap(NamespaceIdentifier::getId, Function.identity()));
    Map<Term, Set<Term>> parents = join(codeSystems);

    return new ConceptGraph(codeSystems, parents);
  }

}
