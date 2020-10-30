/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
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

  public static final String SNOMED_BASE = "http://snomed.info/";
  public static final String SNOMED_TERMS = SNOMED_BASE + "id/";

  public static final String LOINC = "http://loinc.org";

  public static final String CPT = "http://www.ama-assn.org/go/cpt";

  public static final String RxNORM = "http://www.nlm.nih.gov/research/umls/rxnorm";

  public static final String ICD_10_BASE = "http://hl7.org/fhir/sid/icd-10";
  public static final String ICD_10_CM = "http://hl7.org/fhir/sid/icd-10-cm";
  public static final String ICD_10_PCS = "http://hl7.org/fhir/sid/icd-10-pcs";

  public static final String HCPCS = "urn:oid:2.16.840.1.113883.6.14";

  public static final String MOCK = "http://ontology.mayo.edu/taxonomies/TODO";

  /**
   * Instantiates a SNOMED-CT term (http://www.snomed.org/)
   *
   * @param label
   * @param code
   * @return a Term from SNOMED
   */
  public static ConceptIdentifier sct(String label, String code) {
    return new ConceptIdentifier()
        .withName(label)
        .withTag(code)
        .withUuid(UUID.nameUUIDFromBytes(code.getBytes()))
        .withNamespaceUri(URI.create(SNOMED_BASE + "sct/900000000000207008"))
        .withResourceId(URI.create(SNOMED_TERMS + code))
        .withReferentId(URI.create(SNOMED_TERMS + code))
        .withVersionTag("900000000000207008")
        .withVersionId(URI.create(SNOMED_BASE + "id/900000000000207008/" + code));
  }

  /**
   * Instantiates a LOINC term (https://loinc.org/)
   *
   * @param label
   * @param code
   * @return a Term from LOINC
   */
  public static ConceptIdentifier lnc(String label, String code) {
    return new ConceptIdentifier()
        .withName(label)
        .withTag(code)
        .withUuid(UUID.nameUUIDFromBytes(code.getBytes()))
        .withNamespaceUri(URI.create(LOINC))
        .withResourceId(URI.create(LOINC + "/" + code))
        .withReferentId(URI.create(LOINC + "/" + code))
        .withEstablishedOn(DateTimeUtil.parseDate("2020-01-01"))
        .withVersionTag(VERSION_LATEST)
        .withVersionId(URI.create(LOINC + "/LATEST/" + code));
  }

  /**
   * Instantiates a CPT term (https://www.aapc.com/resources/medical-coding/cpt.aspx)
   *
   * @param label
   * @param code
   * @return a Term from CPT
   */
  public static ConceptIdentifier cpt(String label, String code) {
    return new ConceptIdentifier()
        .withName(label)
        .withTag(code)
        .withUuid(UUID.nameUUIDFromBytes(code.getBytes()))
        .withNamespaceUri(URI.create(CPT))
        .withResourceId(URI.create(CPT + "/" + code))
        .withReferentId(URI.create(CPT + "/" + code))
        .withEstablishedOn(DateTimeUtil.parseDate("2020-01-01"))
        .withVersionTag(VERSION_LATEST)
        .withVersionId(URI.create(CPT + "/LATEST/" + code));
  }

  /**
   * Instantiates a RxNORM term (https://www.nlm.nih.gov/research/umls/rxnorm/index.html)
   *
   * @param label
   * @param code
   * @return a Term from RxNorm
   */
  public static ConceptIdentifier rxn(String label, String code) {
    return new ConceptIdentifier()
        .withName(label)
        .withTag(code)
        .withUuid(UUID.nameUUIDFromBytes(code.getBytes()))
        .withNamespaceUri(URI.create(""))
        .withResourceId(URI.create(RxNORM + "/" + code))
        .withReferentId(URI.create(RxNORM + "/" + code))
        .withEstablishedOn(DateTimeUtil.parseDate("2020-01-01"))
        .withVersionTag(VERSION_LATEST)
        .withVersionId(URI.create(RxNORM + "/LATEST/" + code));
  }

  /**
   * Instantiates a ICD_10 term (https://www.who.int/classifications/icd/icdonlineversions/en/)
   *
   * @param label
   * @param code
   * @return a Term from ICD-10
   */
  public static ConceptIdentifier icd10(String label, String code) {
    return new ConceptIdentifier()
        .withName(label)
        .withTag(code)
        .withUuid(UUID.nameUUIDFromBytes(code.getBytes()))
        .withNamespaceUri(URI.create(ICD_10_BASE))
        .withResourceId(URI.create(ICD_10_BASE + "/" + code))
        .withReferentId(URI.create(ICD_10_BASE + "/" + code))
        .withEstablishedOn(DateTimeUtil.parseDate("2020-01-01"))
        .withVersionTag(VERSION_LATEST)
        .withVersionId(URI.create(ICD_10_BASE + "/LATEST/" + code));
  }


  /**
   * Instantiates a ICD_PCS term (https://www.cms.gov/Medicare/Coding/ICD10/2020-ICD-10-PCS)
   *
   * @param label
   * @param code
   * @return a Term from ICD-10-PCS
   */
  public static ConceptIdentifier icd10pcs(String label, String code) {
    return new ConceptIdentifier()
        .withName(label)
        .withTag(code)
        .withUuid(UUID.nameUUIDFromBytes(code.getBytes()))
        .withNamespaceUri(URI.create(ICD_10_PCS))
        .withResourceId(URI.create(ICD_10_PCS + "/" + code))
        .withReferentId(URI.create(ICD_10_PCS + "/" + code))
        .withEstablishedOn(DateTimeUtil.parseDate("2020-01-01"))
        .withVersionTag(VERSION_LATEST)
        .withVersionId(URI.create(ICD_10_PCS + "/LATEST/" + code));
  }


  /**
   * Instantiates a ICD_CM term (https://www.cdc.gov/nchs/icd/icd10cm.htm)
   *
   * @param label
   * @param code
   * @return a Term from ICD-10-CM
   */
  public static ConceptIdentifier icd10cm(String label, String code) {
    return new ConceptIdentifier()
        .withName(label)
        .withTag(code)
        .withUuid(UUID.nameUUIDFromBytes(code.getBytes()))
        .withNamespaceUri(URI.create(ICD_10_CM))
        .withResourceId(URI.create(ICD_10_CM + "/" + code))
        .withReferentId(URI.create(ICD_10_CM + "/" + code))
        .withEstablishedOn(DateTimeUtil.parseDate("2020-01-01"))
        .withVersionTag(VERSION_LATEST)
        .withVersionId(URI.create(ICD_10_CM + "/LATEST/" + code));
  }


  /**
   * Instantiates a HCPCS term (https://www.cms.hhs.gov/MedHCPCSGenInfo/)
   *
   * @param label
   * @param code
   * @return a Term from HCPCS
   */
  public static ConceptIdentifier hcpcs(String label, String code) {
    return new ConceptIdentifier()
        .withName(label)
        .withTag(code)
        .withUuid(UUID.nameUUIDFromBytes(code.getBytes()))
        .withNamespaceUri(URI.create(HCPCS))
        .withResourceId(URI.create(HCPCS + ":" + code))
        .withReferentId(URI.create(HCPCS + ":" + code))
        .withEstablishedOn(DateTimeUtil.parseDate("2020-01-01"))
        .withVersionTag(VERSION_LATEST)
        .withVersionId(URI.create(HCPCS + ":LATEST:" + code));
  }


  /**
   * Instantiates a Mock term
   * Used for testing
   *
   * @param label
   * @param code
   * @return a Test Term from a fictitious concept scheme
   */
  public static ConceptIdentifier mayo(String label, String code) {
    return new ConceptIdentifier()
        .withName(label)
        .withTag(code)
        .withUuid(UUID.nameUUIDFromBytes(code.getBytes()))
        .withNamespaceUri(URI.create(MOCK))
        .withResourceId(URI.create(MOCK + "#" + code))
        .withVersionId(URI.create(MOCK + "#" + code))
        .withReferentId(URI.create(MOCK + "#" + code));
  }
}
