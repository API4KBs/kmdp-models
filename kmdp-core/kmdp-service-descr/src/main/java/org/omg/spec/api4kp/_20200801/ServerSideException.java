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

/**
 * Self-explanatory processing Exception, i.e. Exception that implements {@link
 * org.zalando.problem.Problem} and, when thrown, results in an Answer.failed().withExplanationDetail(this).
 * <p>
 * This class should be used, or extended, to denote operation failures - as opposed to identifying
 * system states that are possible yet undesirable because they violate one or more quality
 * constraints. For example, a KnowledgeBase may not exist, or be in an invalid state, but no
 * failure should occur until a client tries to resolve or process that KnowledgeBase. Undesirable
 * states should be tracked using implementations of {@link org.zalando.problem.Problem} that are
 * not subclasses of @{@link Exception}.
 *
 * @see Explainer#newProblem() and its overloads
 */
@SuppressWarnings("java:S110")
public class ServerSideException extends AbstractThrowableProblem {

  private final Map<String, List<String>> headers;

  /**
   * General purpose constructor
   * <p>
   * The parameters can be divided in different categories:
   * <ul>
   *   <li>Human-oriented: information meant to be consumed for presentation and/or troubleshooting</li>
   *   <li>Web-oriented: metadata used in a web-based client/interaction</li>
   *   <li>Technical: underlying exceptions and stack traces</li>
   *   <li>Semantic: identification of the entity and/or state within the system that caused the exception</li>
   * </ul>
   * <p>
   * There are several correlations between the parameters. Detail should refine title, and both
   * should reference the instance, if any.
   * If the type denotes a state, it should be the state of the instance when possible.
   * The status and the type (state) should be consistent, where the latter implies the former.
   * For example, a "NOT_FOUND" could result from the non-existence of a requested entity, while a
   * "CONFLICT" may result from the attempt to assert data into a KnowledgeBase which would lead to
   * an inconsistent state.
   *
   * @param type     a URI that denotes the nature of the server state that caused the failure
   * @param title    a human oriented summary of the problem
   * @param status   an HTTP response status code that denotes the type of failure
   * @param detail   a human oriented more detailed description of the problem
   * @param instance an individual entity that can be traced to be at the root of the problem
   * @param cause    an underlying Exception
   * @param headers  to be included in the response
   */
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
        Map.of(Severity.KEY, Severity.ERR));
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
    // it's technically possible, but questionable, to throw an SSE with code 2xx
    return Integer.parseInt(code.getTag()) >= 300;
  }

  /**
   * The use of AOP for logging and other 'meta' purposes has the side effect of increasing the
   * complexity of the stack traces. This method removes some of the AOP-related entries, and limits
   * the length of the trace to the first 5 entries (number is arbitrary and could be changed).
   * <p>
   * The rationale is that exceptions should be logged internally by a server, and only the most
   * relevant information should be sent to the client, especially when the client is remote.
   *
   * @param stackTrace
   * @return
   */
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