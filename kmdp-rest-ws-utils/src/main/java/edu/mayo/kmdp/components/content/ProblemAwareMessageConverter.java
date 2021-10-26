package edu.mayo.kmdp.components.content;


import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;

@Component
@Order
public class ProblemAwareMessageConverter
    implements HttpMessageConverter<Object>, ProblemAware {

  @Override
  public boolean canRead(Class<?> clazz, MediaType mediaType) {
    return false;
  }

  @Override
  public boolean canWrite(Class<?> clazz, MediaType mediaType) {
    return getSupportedMediaTypes().contains(mediaType);
  }

  @Override
  public List<MediaType> getSupportedMediaTypes() {
    return applicableMediatTypes();
  }

  @Override
  public KnowledgeCarrier read(Class<?> clazz,
      HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    return null;
  }

  @Override
  public void write(Object obj, MediaType contentType,
      HttpOutputMessage outputMessage) throws IOException {
    if (obj instanceof KnowledgeCarrier) {
      Optional<String> msg = ((KnowledgeCarrier) obj).asString();
      if (msg.isPresent()) {
        outputMessage.getBody().write(msg.get().getBytes());
      }
    } else {
      outputMessage.getBody().write(obj.toString().getBytes());
    }
  }


}

