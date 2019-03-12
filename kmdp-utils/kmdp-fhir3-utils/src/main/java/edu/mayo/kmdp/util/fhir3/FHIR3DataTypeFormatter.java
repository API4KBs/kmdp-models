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
package edu.mayo.kmdp.util.fhir3;

import org.hl7.fhir.dstu3.model.CodeType;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Type;

public class FHIR3DataTypeFormatter {

  public static String format(Type dt) {
    if (dt instanceof Quantity) {
      return formatQuantity((Quantity) dt);
    } else if (dt instanceof Coding) {
      return formatCode((Coding) dt);
    } else if (dt instanceof CodeType) {
      return formatCode((CodeType) dt);
    } else {
      throw new UnsupportedOperationException("TODO ; formatter for " + dt.getClass());
    }

  }

  private static String formatCode(Coding dt) {
    return dt.getDisplay();
  }

  private static String formatCode(CodeType dt) {
    return dt.getValue();
  }

  private static String formatQuantity(Quantity dt) {
    return dt.getValue() + " " + dt.getUnit();
  }

}
