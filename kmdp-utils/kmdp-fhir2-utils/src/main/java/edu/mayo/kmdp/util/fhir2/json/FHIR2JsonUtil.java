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
package edu.mayo.kmdp.util.fhir2.json;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.mayo.kmdp.util.JSonUtil;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

public class FHIR2JsonUtil {

  private static IParser jsonParser = FhirContext.forDstu2().newJsonParser();

  public static String toJsonString(IResource res) {
    return toJsonStringContained(res, true);
  }

  private static String toJsonStringBasic(IResource res) {
    return jsonParser.setPrettyPrint(true).encodeResourceToString(res);
  }

  private static String toJsonStringContained(IResource r, boolean pretty) {
    try {
      return pretty
          ? new ObjectMapper().writerWithDefaultPrettyPrinter()
          .writeValueAsString(toJsonContained(r))
          : toJsonContained(r).toString();
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return toJsonContained(r).toString();
  }

  private static JsonNode toJsonContained(IResource r) {
    // FIXME Workaround for issue possibly related to https://github.com/jamesagnew/hapi-fhir/issues/505
    JsonNode qNode = toJsonBasic(r);
    List<JsonNode> jns = r.getContained().getContainedResources().stream()
        .map(FHIR2JsonUtil::toJsonBasic)
        .collect(Collectors.toList());
    if (!jns.isEmpty()) {
      ArrayNode an = ((ObjectNode) qNode).putArray("contained");
      jns.forEach(an::add);
    }
    return qNode;
  }

  private static JsonNode toJsonBasic(IResource res) {
    StringWriter sw = new StringWriter();
    sw.write(toJsonStringBasic(res));
    return JSonUtil.readJson(new ByteArrayInputStream(sw.toString().getBytes())).get();
  }

  public static <T extends IResource> T parse(byte[] data, Class<T> klass) {
    return jsonParser.parseResource(klass, new String(data));
  }
}
