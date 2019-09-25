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

import com.fasterxml.jackson.core.Version;
import edu.mayo.kmdp.util.fhir.AbstractFHIRJacksonModule;
import org.hl7.fhir.dstu3.model.Base;

public class FHIR3JacksonModule extends AbstractFHIRJacksonModule<Base> {

  private static final String NAME = "FHIR-STU3-Jackson-Module";

  public FHIR3JacksonModule() {
    super(NAME,
        new Version(3, 0, 0, "LATEST",
            FHIR3JacksonModule.class.getPackage().toString(),
            FHIR3JacksonModule.class.getName()),
        Base.class,
        new FHIR3JsonAdapter.FHIRSerializer(),
        new FHIR3JsonAdapter.FHIRDeserializer());
  }

}