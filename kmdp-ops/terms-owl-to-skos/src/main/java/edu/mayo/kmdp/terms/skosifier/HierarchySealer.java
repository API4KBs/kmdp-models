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
package edu.mayo.kmdp.terms.skosifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;

/**
 * Ensures that a SKOS scheme is closed, i.e. no concept is broader/narrower than another concept
 * that is not part of the scheme
 */
public class HierarchySealer {

  public Optional<Model> close(Model model) {

    Set<Statement> removand = new HashSet<>();
    Set<Statement> addends = new HashSet<>();

    Resource topConcept = null;

    StmtIterator iter = model.listStatements();
    while (iter.hasNext()) {
      Statement st = iter.nextStatement();

      Property p = st.getPredicate();
      if (SKOS.broader.equals(p) || SKOS.broaderTransitive.equals(p)
          || SKOS.narrower.equals(p) || SKOS.narrowerTransitive.equals(p)
          || SKOS.related.equals(p)) {

        Resource related = (Resource) st.getObject();
        if (! model.contains(related,SKOS.inScheme)) {
          scheduleForRemoval(st, removand);
          if (topConcept == null) {
            topConcept = getTopConcept(model);
          }
          addends.add(ResourceFactory.createStatement(st.getSubject(), getClosureRelationship(p), topConcept));
        }
      }
    }

    model.remove(new ArrayList<>(removand));
    model.add(new ArrayList<>(addends));

    return Optional.of(model);
  }

  private Resource getTopConcept(Model model) {
    NodeIterator n = model.listObjectsOfProperty(SKOS.hasTopConcept);
    if (!n.hasNext()) {
      throw new IllegalStateException("Unable to detect Scheme's top concept");
    }
    Resource top = (Resource) n.next();
    if (n.hasNext()) {
      throw new IllegalStateException("Scheme has more than one top concept");
    }
    return top;
  }

  private Property getClosureRelationship(Property p) {
    if (SKOS.broader.equals(p) || SKOS.broaderTransitive.equals(p)) {
      return SKOS.broader;
    } else if (SKOS.narrower.equals(p) || SKOS.narrowerTransitive.equals(p)) {
      return SKOS.narrower;
    } else {
      return SKOS.related;
    }
  }

  private void scheduleForRemoval(Statement st, Set<Statement> removand) {
    removand.add(ResourceFactory.createStatement((Resource) st.getObject(), RDF.type, SKOS.Concept));
    removand.add(st);
  }
}
