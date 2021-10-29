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
package org.omg.spec.api4kp._20200801;

import static org.omg.spec.api4kp._20200801.Explainer.EMPTY_STACK_TRACE;
import static org.omg.spec.api4kp._20200801.Explainer.GENERIC_ERROR_TYPE;
import static org.omg.spec.api4kp._20200801.Explainer.GENERIC_INFO_TYPE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.mayo.ontology.taxonomies.ws.responsecodes.ResponseCode;
import edu.mayo.ontology.taxonomies.ws.responsecodes.ResponseCodeSeries;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

@SuppressWarnings("java:S110")
public class ServerSideException extends AbstractThrowableProblem {

  private final Map<String, List<String>> headers;

  protected ServerSideException(
      final URI type,
      final String title,
      final ResponseCode status,
      final String detail,
      final URI instance,
      final Throwable cause,
      final Map<String, List<String>> headers) {
    super(type,
        title,
        Status.valueOf(Integer.parseInt(status.getTag())),
        detail,
        instance,
        null,
        Collections.emptyMap());
    if (cause != null) {
      this.setStackTrace(filterStackTrace(cause.getStackTrace()));
    } else {
      this.setStackTrace(EMPTY_STACK_TRACE);
    }
    this.headers = headers != null ? new LinkedHashMap<>(headers) : Collections.emptyMap();
  }

  public ServerSideException(
      final URI type,
      final String title,
      final ResponseCode status,
      final String detail,
      final URI instance) {
    this(type, title, status, detail, instance, null, null);
  }

  public ServerSideException(Throwable t) {
    this(GENERIC_ERROR_TYPE,
        t.getClass().getSimpleName(),
        ResponseCodeSeries.InternalServerError,
        t.getMessage(),
        null, t, null);
  }

  public ServerSideException(ResponseCode code, Map<String, List<String>> headers, String error) {
    this(isError(code) ? GENERIC_ERROR_TYPE : GENERIC_INFO_TYPE,
        code.getLabel(), code, error, null, null, headers);
  }

  public ServerSideException(ResponseCode code, Map<String, List<String>> headers, byte[] error) {
    this(code, headers, new String(error));
  }

  public ServerSideException(ResponseCode code, byte[] error) {
    this(code, Collections.emptyMap(), error);
  }

  public ServerSideException(ResponseCode code, String errorMsg) {
    this(code, errorMsg.getBytes());
  }

  public ServerSideException(ResponseCode code, ResourceIdentifier instance, String error) {
    this(isError(code) ? GENERIC_ERROR_TYPE : GENERIC_INFO_TYPE,
    code.getLabel(),
    code,
    error,
    instance.getVersionId() != null ? instance.getVersionId() : instance.getResourceId(),
    null,
    Collections.emptyMap());
  }

  public ServerSideException(ResponseCode code) {
    this(code, code.getLabel());
  }

  @JsonIgnore
  public ResponseCode getCode() {
    if (this.getStatus() == null) {
      return null;
    }
    return ResponseCodeSeries.resolve(Integer.toString(this.getStatus().getStatusCode()))
        .orElseThrow(() ->
            new IllegalStateException("Unmapped status code " + this.getStatus().getStatusCode()));
  }

  public Map<String, List<String>> getHeaders() {
    return headers;
  }

  private static boolean isError(ResponseCode code) {
    return Integer.parseInt(code.getTag()) >= 300;
  }

  private StackTraceElement[] filterStackTrace(StackTraceElement[] stackTrace) {
    return Arrays.stream(stackTrace)
        .filter(ste -> !ste.getClassName().contains("$"))
        .filter(ste -> !ste.getClassName().startsWith("org.aspectj"))
        .filter(ste -> !ste.getMethodName().contains("_around"))
        .filter(ste -> !ste.getClassName().equals(Answer.class.getName()))
        .filter(ste -> !ste.getClassName().contains("Interceptor"))
        .limit(5)
        .toArray(StackTraceElement[]::new);
  }
}