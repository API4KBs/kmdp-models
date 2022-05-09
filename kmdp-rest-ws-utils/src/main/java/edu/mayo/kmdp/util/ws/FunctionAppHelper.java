package edu.mayo.kmdp.util.ws;

import static edu.mayo.kmdp.util.CharsetEncodingUtil.recodeToBase64;
import static org.omg.spec.api4kp._20200801.Explainer.EXPL_HEADER;
import static org.omg.spec.api4kp._20200801.Explainer.GENERIC_ERROR_TYPE;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import edu.mayo.kmdp.util.CharsetEncodingUtil;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.ontology.taxonomies.ws.responsecodes.ResponseCodeSeries;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.ServerSideException;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

/**
 * Helper methods to bridge the API4KP / Azure Fn'App layers
 */
public class FunctionAppHelper {

  private FunctionAppHelper() {
    // functions only
  }

  /**
   * Extracts a Path parameter from a request
   *
   * @param argName      the name of the parameter
   * @param route        the path pattern
   * @param argType      the parameter datatype
   * @param required     true if mandatory
   * @param defaultValue used if absent
   * @param path         the actual request path
   * @param requestId    unique identifier
   * @param <T>          the parameter datatype
   * @return T, or null
   * @throws ServerSideException (BadRequest) if unable to parse the parameter value as a T
   */
  public static <T> T fromPath(
      String argName,
      String route,
      Class<T> argType,
      boolean required,
      String defaultValue,
      String path,
      String requestId) {

    String value = null;
    String key = "{" + argName + "}";

    String[] routeBlocks = route.split("/");
    String[] pathBlocks = path.split("/");

    for (int j = 0; j < routeBlocks.length; j++) {
      if (key.equals(routeBlocks[j])) {
        value = pathBlocks[j];
        break;
      }
    }

    return tryParse(argName, value, argType, defaultValue, required, requestId);
  }

  /**
   * Extracts a Body parameter from a request
   *
   * @param argName      the name of the parameter
   * @param route        the path pattern
   * @param argType      the parameter datatype
   * @param required     true if mandatory
   * @param defaultValue used if absent
   * @param body         the body of the request
   * @param requestId    unique identifier
   * @param <T>          the parameter datatype
   * @return T, or null
   * @throws ServerSideException (BadRequest) if unable to parse the parameter value as a T
   */
  public static <T> T fromBody(
      String argName,
      String route,
      Class<T> argType,
      boolean required,
      String defaultValue,
      String body,
      String requestId) {

    return tryParse(argName, body, argType, defaultValue, required, requestId);
  }

  /**
   * Extracts a Header parameter from a request
   *
   * @param argName      the name of the parameter
   * @param route        the path pattern
   * @param argType      the parameter datatype
   * @param required     true if mandatory
   * @param defaultValue used if absent
   * @param headers      the headers of the request
   * @param requestId    unique identifier
   * @param <T>          the parameter datatype
   * @return T, or null
   * @throws ServerSideException (BadRequest) if unable to parse the parameter value as a T
   */
  public static <T> T fromHeader(
      String argName,
      String route,
      Class<T> argType,
      boolean required,
      String defaultValue,
      Map<String, String> headers,
      String requestId) {

    return tryParse(argName, headers.get(argName), argType, defaultValue, required, requestId);
  }

  /**
   * Extracts a Query parameter from a request
   *
   * @param argName      the name of the parameter
   * @param route        the path pattern
   * @param argType      the parameter datatype
   * @param required     true if mandatory
   * @param defaultValue used if absent
   * @param query        the query parameters of the request
   * @param requestId    unique identifier
   * @param <T>          the parameter datatype
   * @return T, or null
   * @throws ServerSideException (BadRequest) if unable to parse the parameter value as a T
   */
  public static <T> T fromQuery(
      String argName,
      String route,
      Class<T> argType,
      boolean required,
      String defaultValue,
      Map<String, String> query,
      String requestId) {

    return tryParse(argName, query.get(argName), argType, defaultValue, required, requestId);
  }

  /**
   * Converts an {@link Answer} to a {@link HttpResponseMessage}
   * <p>
   * Maps the HTTP status code and return headers (if any).
   * <p>
   * If {@link Answer#isSuccess()}, serializes the body in JSON and sets the Content-Type header.
   * <p>
   * //TODO this may break the type-safe contract.. problem may be better as a header.. Otherwise,
   * uses the Answer's explanation {@link org.zalando.problem.Problem} as a return type
   */
  public static HttpResponseMessage toResponse(
      HttpRequestMessage<Optional<String>> request,
      Answer<?> answer) {
    var builder = request
        .createResponseBuilder(
            HttpStatus.valueOf(Integer.parseInt(answer.getOutcomeType().getTag())));

    builder.header("Content-Type", "application/json");
    if (answer.isSuccess()) {
      builder.body(answer.flatOpt(JSonUtil::writeJsonAsString).get());
    } else {
      builder.body(answer.printExplanationAsJson());
    }

    var expl = recodeToBase64(answer.printExplanationAsJson());
    builder.header(EXPL_HEADER, expl);

    answer.listMeta().forEach(h ->
        answer.getMetas(h)
            .forEach(v ->
                builder.header(CharsetEncodingUtil.sanitizeToASCIItext(h), sanitizeHeaderValue(v))));

    return builder.build();
  }

  protected static String sanitizeHeaderValue(String headerValue) {

    PolicyFactory policy = Sanitizers.FORMATTING;
    String sanitizedHeaderValue = policy.sanitize(headerValue);

    return sanitizedHeaderValue;

  }

  /**
   * Validates and parses an input parameter, assuming that value is a String serialization of an
   * object of type T
   *
   * @param argName      the name of the parameter
   * @param value        the value of the parameter
   * @param argType      the parameter datatype
   * @param required     true if mandatory
   * @param defaultValue used if absent
   * @param requestId    unique identifier
   * @param <T>          the parameter datatype
   * @return T, or null
   * @throws ServerSideException (BadRequest) if unable to parse the parameter value as a T
   */
  private static <T> T tryParse(
      String argName,
      String value,
      Class<T> argType,
      String defaultValue,
      boolean required,
      String requestId) {
    try {
      if (value == null) {
        return null;
      }

      var validatedValue = validateRaw(argName, value, defaultValue, required);

      // TODO add more specialized constructors here if needed...
      if (UUID.class.equals(argType)) {
        return argType.cast(UUID.fromString(validatedValue));
      }
      try {
        return argType.getConstructor(String.class).newInstance(validatedValue);
      } catch (NoSuchMethodException nsme) {
        return JSonUtil.parseJson(validatedValue, argType)
            .orElseThrow(() -> new IllegalArgumentException("Unable to parse " + validatedValue));
      }
    } catch (Exception e) {
      throw new ServerSideException(
          GENERIC_ERROR_TYPE,
          e.getClass().getSimpleName(),
          ResponseCodeSeries.BadRequest,
          e.getMessage(),
          URI.create("urn:" + requestId));
    }
  }

  /**
   * Checks whether an argument is present (not null, blanks are allowed though), returning the
   * defaultValue otherwise. If the argument is required but no defaultValue is provided, throws an
   * Exception (expected to be caught and recast as a BadRequest error)
   *
   * @param argName      the name of the parameter
   * @param value        the value of the parameter
   * @param required     true if mandatory
   * @param defaultValue used if absent
   * @return the value if present, defaultValue otherwise
   */
  private static String validateRaw(
      String argName,
      String value,
      String defaultValue,
      boolean required) {
    if (required && value == null) {
      if (defaultValue != null) {
        var message = String.format("Required parameter '%s' not present", argName);
        throw new IllegalArgumentException(message);
      } else {
        return defaultValue;
      }
    } else {
      return value;
    }
  }

  /**
   * Logs an Exception using the context Logger
   *
   * @param context the context providing the Logger
   * @param e       the {@link Exception} to be logged
   */
  public static void logException(ExecutionContext context, Exception e) {
    context.getLogger().severe(() ->
        "Error in " + context.getFunctionName()
            + " - request " + context.getInvocationId()
            + " with details : " + e.getMessage());
  }


}
