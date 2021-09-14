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
package org.omg.spec.api4kp._20200801;

import static edu.mayo.kmdp.util.Util.isEmpty;
import static org.omg.spec.api4kp._20200801.Explainer.extractExplanation;

import edu.mayo.ontology.taxonomies.ws.responsecodes.ResponseCode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.omg.spec.api4kp._20200801.services.Error;

public class ServerSideException extends RuntimeException {
  private final ResponseCode code;
  private final Map<String, List<String>>
      headers;
  private final Error error;

  public ServerSideException(ResponseCode code, Map<String, List<String>> headers, byte[] error) {
    this.code = code;
    this.headers = headers;
    this.error = new Error().withMessage(composeErrorMessage(error, headers));
  }

  public ServerSideException(ResponseCode code,  byte[] error) {
    this(code, Collections.emptyMap(), error);
  }

  public ServerSideException(ResponseCode code,  String errorMsg) {
    this(code, errorMsg.getBytes());
  }

  public ServerSideException(ResponseCode code) {
    this(code, code.getLabel());
  }

  public ResponseCode getCode() {
    return code;
  }

  public Map<String, List<String>> getHeaders() {
    return headers;
  }

  public Error getError() {
    return error;
  }

  private String composeErrorMessage(byte[] error, Map<String, List<String>> headers) {
    String mainError = new String(error);
    String explanation = extractExplanation(headers)
        .flatMap(AbstractCarrier::asString)
        .orElse("");
    if (isEmpty(mainError) && isEmpty(explanation)) {
      return "Generic Error";
    } else {
      return String.join("\n",mainError, explanation).trim();
    }
  }

  @Override
  public String getMessage() {
    return error == null ? super.getMessage() : error.getMessage();
  }
}