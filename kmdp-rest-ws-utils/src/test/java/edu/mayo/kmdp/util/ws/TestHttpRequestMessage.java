package edu.mayo.kmdp.util.ws;

import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage.Builder;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.HttpStatusType;
import java.net.URI;
import java.util.Map;

class TestHttpRequestMessage implements HttpRequestMessage {

  @Override
  public URI getUri() {
    return null;
  }

  @Override
  public HttpMethod getHttpMethod() {
    return HttpMethod.GET;
  }

  @Override
  public Map<String, String> getHeaders() {
    return null;
  }

  @Override
  public Map<String, String> getQueryParameters() {
    return null;
  }

  @Override
  public Object getBody() {
    return null;
  }

  @Override
  public Builder createResponseBuilder(HttpStatus httpStatus) {
    return new TestBuilder(httpStatus);
  }

  @Override
  public Builder createResponseBuilder(HttpStatusType httpStatusType) {
    return new TestBuilder().status(httpStatusType);
  }

}
