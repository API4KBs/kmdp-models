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
package edu.mayo.kmdp.util.fhir.fhir2;


import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.primitive.CodeDt;

public class FHIR2DataTypeFormatter {

  public static String format(IDatatype dt) {
    if (dt instanceof QuantityDt) {
      return formatQuantity((QuantityDt) dt);
    } else if (dt instanceof CodingDt) {
      return formatCode((CodingDt) dt);
    } else if (dt instanceof CodeDt) {
      return formatCode((CodeDt) dt);
    } else {
      throw new UnsupportedOperationException("TODO ; formatter for " + dt.getClass());
    }

  }

  private static String formatCode(CodingDt dt) {
    return dt.getDisplay();
  }

  private static String formatCode(CodeDt dt) {
    return dt.getValue();
  }


  private static String formatQuantity(QuantityDt dt) {
    return dt.getValue() + " " + dt.getUnit();
  }

}
