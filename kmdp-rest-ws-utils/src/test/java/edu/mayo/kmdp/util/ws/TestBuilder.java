package edu.mayo.kmdp.util.ws;

import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpResponseMessage.Builder;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.HttpStatusType;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringEscapeUtils;

class TestBuilder implements Builder {

  protected Object body;
  protected HttpStatus httpStatus;
  protected HttpStatusType httpStatusType;
  protected Map<String, String> headers = new HashMap<>();

  public TestBuilder() {
  }

  public TestBuilder(HttpStatus httpStatus) {
    this.httpStatus = httpStatus;
  }

  @Override
  public Builder status(HttpStatusType httpStatusType) {
    this.httpStatusType = httpStatusType;
    return this;
  }

  @Override
  public Builder header(String key, String value) {

    String valueEscaped = StringEscapeUtils.escapeHtml4(value);
    headers.put(key, valueEscaped);

    return this;
  }

  @Override
  public Builder body(Object o) {
    this.body = body;
    return this;
  }

  @Override
  public HttpResponseMessage build() {

    TestHttpResponseMessage httpResponseMessage = new TestHttpResponseMessage(httpStatusType, body, headers);

    return httpResponseMessage;

  }

}
