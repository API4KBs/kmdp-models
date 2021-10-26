package edu.mayo.kmdp.components.content;

import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.TXT;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.HTML;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Encoded_Knowledge_Expression;

import java.nio.charset.Charset;
import java.util.Optional;
import org.omg.spec.api4kp._20200801.AbstractCarrier.Encodings;
import org.omg.spec.api4kp._20200801.Explainer;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
@Order
public class CustomResponseBodyAdvice implements ResponseBodyAdvice<Object> {

  public static final SyntacticRepresentation NL_MIME =
      rep(HTML, TXT, Charset.defaultCharset(), Encodings.DEFAULT);

  @Override
  public boolean supports(MethodParameter returnType,
      Class<? extends HttpMessageConverter<?>> converterType) {
   return ProblemAware.class.isAssignableFrom(converterType) &&
       ResponseEntity.class.isAssignableFrom(returnType.getParameterType());
  }

  @Override
  public Object beforeBodyWrite(Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {
    if (body == null && ProblemAware.APPLICABLE_TYPES.contains(selectedContentType)) {
      return Optional.ofNullable(response.getHeaders().get(Explainer.EXPL_HEADER))
          .map(list -> list.get(0))
          .map(s -> new KnowledgeCarrier()
              .withExpression(s.getBytes())
              .withRepresentation(NL_MIME)
              .withLevel(Encoded_Knowledge_Expression))
          .orElse(null);
    } else {
      return body;
    }
  }

}