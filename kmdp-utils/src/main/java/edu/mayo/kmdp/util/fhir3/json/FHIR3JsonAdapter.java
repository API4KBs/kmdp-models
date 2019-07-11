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
package edu.mayo.kmdp.util.fhir3.json;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.mayo.kmdp.util.JSonUtil;
import org.hl7.fhir.dstu3.model.*;

import java.io.IOException;
import java.util.List;

import static edu.mayo.kmdp.util.fhir3.json.FHIR3JsonUtil.toJsonString;
import static org.hl7.fhir.dstu3.model.Bundle.BundleType.COLLECTION;

public class FHIR3JsonAdapter {

  private static IParser jsonParser = FhirContext.forDstu3().newJsonParser();

  public static class FHIRSerializer extends JsonSerializer<Base> {

    @Override
    public void serialize(Base v, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (v == null) {
        gen.writeNull();
      } else if (v instanceof Type) {
        gen.writeObject(trySerializeType((Type) v));
      } else if (isFHIRList(v)) {
        Bundle b = new Bundle().setType(COLLECTION);
        ((List) v).forEach(
            (x) -> b.addEntry(new Bundle.BundleEntryComponent().setResource((Resource) x)));
        gen.writeRawValue(toJsonString(b));
      } else {
        gen.writeObject(v);
      }
    }

    private boolean isFHIRList(Base v) {
      return v instanceof List && ((List) v).stream().allMatch((x) -> x instanceof Resource);
    }
  }

  public static class FHIRResourceSerializer extends JsonSerializer<BaseResource> {

    @Override
    public void serialize(BaseResource v, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (v == null) {
        gen.writeNull();
      }
      gen.writeRawValue(toJsonString((Resource) v));
    }
  }


  public static class FHIRResourceDeserializer extends JsonDeserializer<BaseResource> {

    @Override
    public BaseResource deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
      return (BaseResource) jsonParser.parseResource(jp.readValueAsTree().toString());
    }
  }


  public static class FHIRDeserializer extends JsonDeserializer<Base> {

    @Override
    public Base deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
      JsonNode jn = jp.readValueAsTree();
      if (jn.isArray()) {
        Bundle b = new Bundle().setType(COLLECTION);
        jn.forEach((cjn) -> b
            .addEntry(new Bundle.BundleEntryComponent().setResource(tryParseAsResource(cjn))));
        return b;
      } else {
        return tryParse(jn);
      }
    }
  }


  /***********
   The following classes violate the FHIR standards, and should only be used when full compliance is not expected nor required
   ************/


  public static class FHIRDatatypeSerializer extends JsonSerializer<Type> {

    @Override
    public void serialize(Type t, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (t == null) {
        gen.writeNull();
      } else {
        gen.writeObject(trySerializeType(t));
      }
    }
  }

  public static class FHIRDatatypeDeserializer extends JsonDeserializer<Type> {

    @Override
    public Type deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
      JsonNode jn = jp.getCodec().readTree(jp);
      return tryParseType(jn);
    }
  }


  public static Resource tryParseAsResource(JsonNode jn) {
    try {
      return (Resource) jsonParser.parseResource(jn.toString());
    } catch (Exception e) {
      return parseTypeAsParam(jn);
    }
  }

  public static Base tryParse(JsonNode jn) {
    try {
      return (Base) jsonParser.parseResource(jn.toString());
    } catch (Exception e) {
      return tryParseType(jn);
    }
  }

  public static Type tryParseType(JsonNode jn) {
    Parameters parameters = parseTypeAsParam(jn);
    return parameters.getParameter().get(0).getValue();
  }

  private static Parameters parseTypeAsParam(JsonNode jn) {
    Parameters paramShell = new Parameters();
    paramShell.addParameter().setName("value").setValue(null);
    String template = toJsonString(paramShell);
    JsonNode parent = JSonUtil.readJson(template.getBytes()).get();
    ((ObjectNode) parent.get("parameter").get(0)).set(jn.fieldNames().next(), jn.elements().next());

    return jsonParser.parseResource(Parameters.class, parent.toString());
  }

  public static JsonNode trySerializeType(Type t) {
    // wrap in Parameters to serialize
    Parameters p = new Parameters();
    p.addParameter(new Parameters.ParametersParameterComponent().setValue(t));
    return JSonUtil.readJson(toJsonString(p).getBytes()).get().get("parameter").get(0);
  }


}
