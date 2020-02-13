/**
 * Copyright © 2020 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
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
package edu.mayo.kmdp.util.ws;

import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.TXT;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.HTML;

import edu.mayo.kmdp.id.helper.DatatypeHelper;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;

/**
 * Message Converter used to adapt HTML streams into KnowledgeCarriers
 *
 * Used to handle redirects, helps ensure compatibility between KMDP APIs and non-KMDP endpoints
 */
public class HTMLKnowledgeCarrierWrapper implements HttpMessageConverter<KnowledgeCarrier> {

  @Override
  public boolean canRead(Class<?> clazz, MediaType mediaType) {
    return KnowledgeCarrier.class.equals(clazz) && MediaType.TEXT_HTML.equals(mediaType);
  }

  @Override
  public boolean canWrite(Class<?> clazz, MediaType mediaType) {
    return KnowledgeCarrier.class.equals(clazz) && MediaType.TEXT_HTML.equals(mediaType);
  }

  @Override
  public List<MediaType> getSupportedMediaTypes() {
    return Collections.singletonList(MediaType.TEXT_HTML);
  }

  @Override
  public void write(KnowledgeCarrier knowledgeCarrier, MediaType contentType,
      HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    if (HTML.sameAs(knowledgeCarrier.getRepresentation().getLanguage())) {
     String html = knowledgeCarrier.asString()
         .orElseThrow(() -> new HttpMessageNotWritableException(
             "Unable to stream HTML content: expected a 'serialized expression', but was "
                 + knowledgeCarrier.getLevel().getLabel()));
     outputMessage.getBody().write(html.getBytes());
    } else {
      throw new HttpMessageNotWritableException("Expected HTML Carrier, but found "
          + knowledgeCarrier.getRepresentation().getLanguage().getLabel());
    }
  }

  @Override
  public KnowledgeCarrier read(Class clazz, HttpInputMessage inputMessage)
      throws IOException {
    return AbstractCarrier.of(inputMessage.getBody())
        .withRepresentation(AbstractCarrier.rep(HTML,TXT))
        .withAssetId(DatatypeHelper.uri(UUID.randomUUID().toString()))
        .withArtifactId(DatatypeHelper.uri(UUID.randomUUID().toString()));
  }
}
