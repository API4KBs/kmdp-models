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
package edu.mayo.kmdp.util.fhir.fhir3;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import edu.mayo.kmdp.util.fhir.AbstractFHIRJsonUtil;
import org.hl7.fhir.dstu3.model.Resource;

public class FHIR3JsonUtil extends AbstractFHIRJsonUtil<Resource> {

  private static final FhirContext context = FhirContext.forDstu3();

  public static final FHIR3JsonUtil instance = new FHIR3JsonUtil();

  @Override
  protected IParser getParser() {
    return context.newJsonParser();
  }

}
