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

import edu.mayo.ontology.taxonomies.api4kp.responsecodes.ResponseCode;
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
    this.error = new Error().withMessage(error != null ? new String(error) : "Undefined error");
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
}