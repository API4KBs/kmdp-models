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
package edu.mayo.kmdp.util.fhir.fhir2_hl7;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import edu.mayo.kmdp.util.fhir.AbstractFHIRJsonUtil;
import java.util.List;
import org.hl7.fhir.instance.model.DomainResource;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class FHIR2HL7JsonUtil extends AbstractFHIRJsonUtil<DomainResource, Resource> {

  private static final IParser jsonParser = FhirContext.forDstu2Hl7Org().newJsonParser();

  public static final FHIR2HL7JsonUtil instance = new FHIR2HL7JsonUtil();

  @Override
  protected IParser getParser() {
    return jsonParser;
  }

  @Override
  public String toJsonString(IBaseResource res) {
    return toJsonString(res, DomainResource.class);
  }

  @Override
  protected List<Resource> getContained(DomainResource baseResource) {
    return baseResource.getContained();
  }
}
