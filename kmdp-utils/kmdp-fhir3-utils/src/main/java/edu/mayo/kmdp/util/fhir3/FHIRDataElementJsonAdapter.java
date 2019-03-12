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
package edu.mayo.kmdp.util.fhir3;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import org.hl7.fhir.dstu3.model.DataElement;

import java.io.IOException;

public class FHIRDataElementJsonAdapter {

  private static IParser jsonParser = FhirContext.forDstu3().newJsonParser();

  public static class Serializer extends JsonSerializer<DataElement> {

    @Override
    public void serialize(DataElement v, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (v == null) {
        return;
      }
      gen.writeRawValue(jsonParser.setPrettyPrint(true).encodeResourceToString(v));
    }
  }

  public static class Deserializer extends JsonDeserializer<DataElement> {

    @Override
    public DataElement deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
      JsonNode node = jp.getCodec().readTree(jp);
      System.out.println(node.toString());
      return jsonParser.parseResource(DataElement.class, node.toString());
    }
  }
}
