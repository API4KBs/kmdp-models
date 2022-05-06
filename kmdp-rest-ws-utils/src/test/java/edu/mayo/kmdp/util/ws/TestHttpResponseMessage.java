package edu.mayo.kmdp.util.ws;

import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatusType;
import java.util.Map;

class TestHttpResponseMessage implements HttpResponseMessage {

  protected Object body;
  protected HttpStatusType httpStatusType;
  protected Map<String, String> headers;

  public TestHttpResponseMessage(HttpStatusType httpStatusType, Object body, Map<String, String> headers) {
    this.body = body;
    this.headers = headers;
    this.httpStatusType = httpStatusType;
  }

  @Override
  public HttpStatusType getStatus() {
    return httpStatusType;
  }

  @Override
  public String getHeader(String key) {
    return headers.get(key);
  }

  @Override
  public Object getBody() {
    return body;
  }

}
