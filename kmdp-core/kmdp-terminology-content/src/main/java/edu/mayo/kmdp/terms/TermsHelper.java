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

import static org.omg.spec.api4kp._20200801.id.IdentifierConstants.VERSION_LATEST;

import edu.mayo.kmdp.util.DateTimeUtil;
import java.net.URI;
import java.util.UUID;
import org.omg.spec.api4kp._20200801.id.ConceptIdentifier;

public class TermsHelper {

  protected TermsHelper() {
    // functions only
  }

  /**
   * Instantiates a SNOMED-CT term
   * @param label
   * @param code
   * @return a Term from SNOMED
   */
  public static ConceptIdentifier sct(String label, String code) {
    return new ConceptIdentifier()
        .withName(label)
        .withTag(code)
        .withUuid(UUID.nameUUIDFromBytes(code.getBytes()))
        .withNamespaceUri(URI.create("http://snomed.info/sct/900000000000207008"))
        .withResourceId(URI.create("http://snomed.info/id/" + code))
        .withReferentId(URI.create("http://snomed.info/id/" + code))
        .withVersionTag("900000000000207008")
        .withVersionId(URI.create("http://snomed.info/id/900000000000207008/" + code));
  }

  public static ConceptIdentifier lnc(String label, String code) {
    return new ConceptIdentifier()
        .withName(label)
        .withTag(code)
        .withUuid(UUID.nameUUIDFromBytes(code.getBytes()))
        .withNamespaceUri(URI.create("http://loinc.org"))
        .withResourceId(URI.create("http://loinc.org/" + code))
        .withReferentId(URI.create("http://loinc.org/" + code))
        .withEstablishedOn(DateTimeUtil.parseDate("2020-01-01"))
        .withVersionTag(VERSION_LATEST)
        .withVersionId(URI.create("http://loinc.org/LATEST/" + code));
  }

  public static ConceptIdentifier cpt(String label, String code) {
    return new ConceptIdentifier()
        .withName(label)
        .withTag(code)
        .withUuid(UUID.nameUUIDFromBytes(code.getBytes()))
        .withNamespaceUri(URI.create("http://www.ama-assn.org/go/cpt"))
        .withResourceId(URI.create("http://www.ama-assn.org/go/cpt/" + code))
        .withReferentId(URI.create("http://www.ama-assn.org/go/cpt/" + code))
        .withEstablishedOn(DateTimeUtil.parseDate("2020-01-01"))
        .withVersionTag(VERSION_LATEST)
        .withVersionId(URI.create("http://www.ama-assn.org/go/cpt/LATEST/" + code));
  }

  public static ConceptIdentifier rxn(String label, String code) {
    return new ConceptIdentifier()
        .withName(label)
        .withTag(code)
        .withUuid(UUID.nameUUIDFromBytes(code.getBytes()))
        .withNamespaceUri(URI.create("http://www.nlm.nih.gov/research/umls/rxnorm"))
        .withResourceId(URI.create("http://www.nlm.nih.gov/research/umls/rxnorm/" + code))
        .withReferentId(URI.create("http://www.nlm.nih.gov/research/umls/rxnorm/" + code))
        .withEstablishedOn(DateTimeUtil.parseDate("2020-01-01"))
        .withVersionTag(VERSION_LATEST)
        .withVersionId(URI.create("http://www.nlm.nih.gov/research/umls/rxnorm/LATEST/" + code));
  }

  public static ConceptIdentifier icd10(String label, String code) {
    return new ConceptIdentifier()
        .withName(label)
        .withTag(code)
        .withUuid(UUID.nameUUIDFromBytes(code.getBytes()))
        .withNamespaceUri(URI.create("http://hl7.org/fhir/sid/icd-10"))
        .withResourceId(URI.create("http://hl7.org/fhir/sid/icd-10/" + code))
        .withReferentId(URI.create("http://hl7.org/fhir/sid/icd-10/" + code))
        .withEstablishedOn(DateTimeUtil.parseDate("2020-01-01"))
        .withVersionTag(VERSION_LATEST)
        .withVersionId(URI.create("http://hl7.org/fhir/sid/icd-10/LATEST/" + code));
  }

  public static ConceptIdentifier icd10pcs(String label, String code) {
    return new ConceptIdentifier()
        .withName(label)
        .withTag(code)
        .withUuid(UUID.nameUUIDFromBytes(code.getBytes()))
        .withNamespaceUri(URI.create("http://hl7.org/fhir/sid/icd-10-pcs"))
        .withResourceId(URI.create("http://hl7.org/fhir/sid/icd-10-pcs/" + code))
        .withReferentId(URI.create("http://hl7.org/fhir/sid/icd-10-pcs/" + code))
        .withEstablishedOn(DateTimeUtil.parseDate("2020-01-01"))
        .withVersionTag(VERSION_LATEST)
        .withVersionId(URI.create("http://hl7.org/fhir/sid/icd-10-pcs/LATEST/" + code));
  }

  public static ConceptIdentifier icd10cm(String label, String code) {
    return new ConceptIdentifier()
        .withName(label)
        .withTag(code)
        .withUuid(UUID.nameUUIDFromBytes(code.getBytes()))
        .withNamespaceUri(URI.create("http://hl7.org/fhir/sid/icd-10-cm"))
        .withResourceId(URI.create("http://hl7.org/fhir/sid/icd-10-cm/" + code))
        .withReferentId(URI.create("http://hl7.org/fhir/sid/icd-10-cm/" + code))
        .withEstablishedOn(DateTimeUtil.parseDate("2020-01-01"))
        .withVersionTag(VERSION_LATEST)
        .withVersionId(URI.create("http://hl7.org/fhir/sid/icd-10-cm/LATEST/" + code));
  }

  public static ConceptIdentifier hcpcs(String label, String code) {
    return new ConceptIdentifier()
        .withName(label)
        .withTag(code)
        .withUuid(UUID.nameUUIDFromBytes(code.getBytes()))
        .withNamespaceUri(URI.create("urn:oid:2.16.840.1.113883.6.14"))
        .withResourceId(URI.create("urn:oid:2.16.840.1.113883.6.14:" + code))
        .withReferentId(URI.create("urn:oid:2.16.840.1.113883.6.14:" + code))
        .withEstablishedOn(DateTimeUtil.parseDate("2020-01-01"))
        .withVersionTag(VERSION_LATEST)
        .withVersionId(URI.create("urn:oid:2.16.840.1.113883.6.14:LATEST:" + code));
  }




  /**
   * Instantiates a Mayo Clinic local term
   * @param label
   * @param code
   * @return a Test Term from a fictitious concept scheme
   */
  public static ConceptIdentifier mayo(String label, String code) {
    return new ConceptIdentifier()
        .withName(label)
        .withTag(code)
        .withUuid(UUID.nameUUIDFromBytes(code.getBytes()))
        .withNamespaceUri(URI.create("http://ontology.mayo.edu/taxonomies/TODO"))
        .withResourceId(URI.create("http://ontology.mayo.edu/taxonomies/TODO#" + code))
        .withVersionId(URI.create("http://ontology.mayo.edu/taxonomies/LATEST/TODO#" + code))
        .withReferentId(URI.create("http://ontology.mayo.edu/taxonomies/TODO#" + code));
  }
}
