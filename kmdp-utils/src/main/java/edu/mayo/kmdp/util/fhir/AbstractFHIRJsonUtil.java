/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
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
package edu.mayo.kmdp.util.fhir;

import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.mayo.kmdp.util.JSonUtil;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;
import org.hl7.fhir.instance.model.api.IBaseResource;

public abstract class AbstractFHIRJsonUtil<R extends IBaseResource, I extends IBaseResource> {

  protected String toJsonString(IBaseResource res, Class<R> domainResourceClass) {
    return domainResourceClass.isInstance(res)
        ? abstractToJsonStringContained((R) res)
        : abstractToJsonStringBasic(res);
  }

  protected String abstractToJsonStringBasic(IBaseResource res) {
    return getParser().encodeResourceToString(res);
  }

  protected String abstractToJsonStringContained(R r) {
    try {
      return new ObjectMapper().writerWithDefaultPrettyPrinter()
          .writeValueAsString(abstractToJsonContained(r));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return abstractToJsonContained(r).toString();
  }

  protected JsonNode abstractToJsonContained(R r) {
    //Workaround for issue possibly related to https://github.com/jamesagnew/hapi-fhir/issues/505
    JsonNode qNode = abstractToJsonBasic(r);
    List<JsonNode> jns = getContained(r).stream()
        .map(this::abstractToJsonBasic)
        .collect(Collectors.toList());
    if (!jns.isEmpty()) {
      ArrayNode an = ((ObjectNode) qNode).putArray("contained");
      jns.forEach(an::add);
    }
    return qNode;
  }

  protected JsonNode abstractToJsonBasic(IBaseResource res) {
    StringWriter sw = new StringWriter();
    sw.write(abstractToJsonStringBasic(res));
    return JSonUtil.readJson(new ByteArrayInputStream(sw.toString().getBytes()))
        .orElse(JsonNodeFactory.instance.nullNode());
  }

  protected abstract IParser getParser();

  public abstract String toJsonString(IBaseResource res);

  public <T extends R> T parse(byte[] data, Class<T> type) {
    return getParser().parseResource(type,new ByteArrayInputStream(data));
  }

  protected abstract List<I> getContained(R r);

}
