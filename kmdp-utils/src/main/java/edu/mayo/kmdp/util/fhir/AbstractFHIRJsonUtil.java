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
package edu.mayo.kmdp.util.fhir;

import ca.uhn.fhir.parser.IParser;
import java.io.ByteArrayInputStream;
import org.hl7.fhir.instance.model.api.IBaseResource;

public abstract class AbstractFHIRJsonUtil<I extends IBaseResource> {

  protected AbstractFHIRJsonUtil() {
  }

  public String toJsonString(I res) {
    return getParser().encodeResourceToString(res);
  }

  protected abstract IParser getParser();

  public <T extends I> T parse(byte[] data, Class<T> type) {
    return getParser().parseResource(type,new ByteArrayInputStream(data));
  }

  public <T extends I> T parse(String data, Class<T> type) {
    return getParser().parseResource(type,new ByteArrayInputStream(data.getBytes()));
  }

}
