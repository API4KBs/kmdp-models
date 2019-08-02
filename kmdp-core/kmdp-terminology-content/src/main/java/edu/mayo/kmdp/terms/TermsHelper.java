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
package edu.mayo.kmdp.terms;

import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;

import java.net.URI;
import java.util.UUID;

import static edu.mayo.kmdp.id.helper.DatatypeHelper.ns;

public class TermsHelper {

  /**
   * Instantiates a SNOMED-CT term
   * @param label
   * @param code
   * @return
   */
  public static ConceptIdentifier sct(String label, String code) {
    return new ConceptIdentifier()
        .withLabel(label)
        .withTag(code)
        .withNamespace(ns("http://snomed.info/sct/900000000000207008",
            "SNOMED-CT",
            "20130731"))

        .withConceptId(URI.create("http://snomed.info/id/" + code))
        .withRef(URI.create("http://snomed.info/id/" + code));
  }

  public static ConceptIdentifier lnc(String label, String code) {
    return new ConceptIdentifier()
        .withLabel(label)
        .withTag(code)
        .withNamespace(new NamespaceIdentifier()
            .withId(URI.create("https://loinc.org/oids/1.3.6.1.4.1.12009.10.2.3")))
        .withConceptId(URI.create("http://loinc.org/" + code))
        .withRef(URI.create("http://loinc.org/" + code));
  }

  public static ConceptIdentifier rxn(String label, String code) {
    return new ConceptIdentifier()
        .withLabel(label)
        .withTag(code)
        .withNamespace(new NamespaceIdentifier()
            .withId(URI.create("http://www.nlm.nih.gov/research/umls/rxnorm")))
        .withConceptId(URI.create("http://www.nlm.nih.gov/research/umls/rxnorm/" + code))
        .withRef(URI.create("http://www.nlm.nih.gov/research/umls/rxnorm/" + code));
  }


  /**
   * Instantiates a Mayo Clinic local term
   * @param label
   * @param code
   * @return
   */
  public static ConceptIdentifier mayo(String label, String code) {
    return new ConceptIdentifier()
        .withLabel(label)
        .withTag(code)
        .withNamespace(
            new NamespaceIdentifier().withId(URI.create("http://ontology.mayo.edu/taxonomies/TODO")))
        .withConceptId(URI.create("http://ontology.mayo.edu/taxonomies/TODO#" + code))
        .withRef(URI.create("http://ontology.mayo.edu/taxonomies/TODO#" + code));
  }
}
