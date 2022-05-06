package edu.mayo.kmdp.util.ws;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpResponseMessage.Builder;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.HttpStatusType;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.Answer;

public class FunctionAppHelperTest {

  public static final String HEADER_CONTENT_KEY = "content";
  public static final String HEADER_CONTENT_VALUE = "The & should be escaped to {ampersand character}amp;";

  @Test
  public void testToResponse() {

    HttpRequestMessage<Optional<String>> request = new TestHttpRequestMessage();

    Map<String, List<String>> meta = new HashMap<>();
    meta.put(HEADER_CONTENT_KEY, List.of(HEADER_CONTENT_VALUE));
    Answer<String> answer = Answer.of("200", "{\"status\": \"testing\"}", meta);

    HttpResponseMessage httpResponseMessage = FunctionAppHelper.toResponse(request, answer);

    String contentHeaderValue = httpResponseMessage.getHeader(HEADER_CONTENT_KEY);
    assertEquals("The &amp; should be escaped to {ampersand character}amp;", contentHeaderValue);

    String explanationHeaderValue = httpResponseMessage.getHeader("X-Explanation");
    assertEquals("", explanationHeaderValue);

    String contentTypeHeaderValue = httpResponseMessage.getHeader("Content-Type");
    assertEquals("application/json", contentTypeHeaderValue);

  }

}
