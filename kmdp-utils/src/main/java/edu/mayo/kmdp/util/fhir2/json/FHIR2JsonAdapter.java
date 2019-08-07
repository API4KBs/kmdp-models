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

import static edu.mayo.kmdp.util.fhir2.json.FHIR2JsonUtil.toJsonString;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.BaseElement;
import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.BaseResource;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Parameters;
import ca.uhn.fhir.model.dstu2.valueset.BundleTypeEnum;
import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.mayo.kmdp.util.JSonUtil;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.hl7.fhir.instance.model.api.IBase;

public class FHIR2JsonAdapter {

  private static IParser jsonParser = FhirContext.forDstu2().newJsonParser();

  public static class FHIRSerializer extends JsonSerializer<IBase> {

    @Override
    public void serialize(IBase v, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (v == null) {
        gen.writeNull();
      } else if (v instanceof IDatatype) {
        gen.writeObject(trySerializeType((IDatatype) v));
      } else if (v instanceof IResource) {
        gen.writeRawValue(toJsonString((IResource) v));
      } else if (isFHIRList(v)) {
        Bundle b = new Bundle().setType(BundleTypeEnum.COLLECTION);
        ((List) v).forEach((x) -> b.addEntry(new Bundle.Entry().setResource((IResource) x)));
        gen.writeRawValue(toJsonString(b));
      } else {
        gen.writeObject(v);
      }
    }

    private boolean isFHIRList(IBase v) {
      return v instanceof List && ((List) v).stream().allMatch((x) -> x instanceof IResource);
    }
  }

  public static class FHIRResourceSerializer extends JsonSerializer<IResource> {

    @Override
    public void serialize(IResource v, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (v == null) {
        gen.writeNull();
      }
      gen.writeRawValue(toJsonString(v));
    }
  }


  public static class FHIRResourceDeserializer extends JsonDeserializer<BaseResource> {

    @Override
    public BaseResource deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
      return (BaseResource) jsonParser.parseResource(jp.readValueAsTree().toString());
    }
  }


  public static class FHIRDeserializer extends JsonDeserializer<BaseElement> {

    @Override
    public BaseElement deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
      JsonNode jn = jp.readValueAsTree();
      if (jn.isArray()) {
        Bundle b = new Bundle().setType(BundleTypeEnum.COLLECTION);
        jn.forEach((cjn) -> b.addEntry(new Bundle.Entry().setResource(tryParseAsResource(cjn))));
        return b;
      } else {
        return tryParse(jn);
      }
    }


  }


  /***********
   The following classes violate the FHIR standards, and should only be used when full compliance is not expected nor required
   ************/


  public static class FHIRDatatypeSerializer extends JsonSerializer<IDatatype> {

    @Override
    public void serialize(IDatatype t, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (t == null) {
        gen.writeNull();
      } else {
        gen.writeObject(trySerializeType(t));
      }
    }
  }

  public static class FHIRDatatypeDeserializer extends JsonDeserializer<IDatatype> {

    @Override
    public IDatatype deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
      JsonNode jn = jp.getCodec().readTree(jp);
      return tryParseType(jn);
    }
  }

  public static IResource tryParseAsResource(JsonNode jn) {
    try {
      return (IResource) jsonParser.parseResource(jn.toString());
    } catch (Exception e) {
      return parseTypeAsParam(jn);
    }
  }

  public static BaseElement tryParse(JsonNode jn) {
    try {
      return (BaseElement) jsonParser.parseResource(jn.toString());
    } catch (Exception e) {
      return (BaseElement) tryParseType(jn);
    }
  }

  public static IDatatype tryParseType(JsonNode jn) {
    Parameters parameters = parseTypeAsParam(jn);
    return parameters != null ? parameters.getParameter().get(0).getValue() : null;
  }

  private static Parameters parseTypeAsParam(JsonNode jn) {
    Parameters paramShell = new Parameters();
    paramShell.addParameter().setName("value").setValue(null);
    String template = toJsonString(paramShell);
    Optional<JsonNode> parent = JSonUtil.readJson(template.getBytes());
    if (parent.isPresent()) {
      ((ObjectNode) parent.get().get("parameter").get(0))
          .set(jn.fieldNames().next(), jn.elements().next());

      return jsonParser.parseResource(Parameters.class, parent.get().toString());
    } else {
      return new Parameters();
    }
  }

  public static JsonNode trySerializeType(IDatatype t) {
    // wrap in Parameters to serialize
    Parameters p = new Parameters();
    p.addParameter().setValue(t);
    Optional<JsonNode> node = JSonUtil.readJson(toJsonString(p).getBytes());
    return node
        .map(jsonNode -> jsonNode.get("parameter").get(0))
        .orElse(JsonNodeFactory.instance.nullNode());
  }


}
