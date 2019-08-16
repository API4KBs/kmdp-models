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
package edu.mayo.kmdp.util.fhir.fhir3;

import static org.hl7.fhir.dstu3.model.Bundle.BundleType.COLLECTION;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import edu.mayo.kmdp.util.fhir.AbstractFHIRJsonAdapter;
import java.io.IOException;
import java.util.List;
import org.hl7.fhir.dstu3.model.Base;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Parameters;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.Type;
import org.hl7.fhir.instance.model.api.IBase;

public class FHIR3JsonAdapter extends
    AbstractFHIRJsonAdapter<DomainResource, Resource, Parameters, Type> {

  private static IParser jsonParser = FhirContext.forDstu3().newJsonParser();
  protected static FHIR3JsonAdapter instance = new FHIR3JsonAdapter();

  private FHIR3JsonAdapter() {
    super(
        FHIR3JsonUtil.instance,
        Parameters.class,
        Parameters::new,
        FHIR3JsonAdapter::getParam,
        FHIR3JsonAdapter::setParam);
  }

  private static void setParam(Parameters p, String paramName, Type value) {
    p.addParameter().setName(paramName).setValue(value);
  }

  private static Type getParam(Parameters p) {
    return p.getParameter().get(0).getValue();
  }

  @Override
  protected IParser getParser() {
    return jsonParser;
  }


  public static class FHIRSerializer extends JsonSerializer<Base> {

    @Override
    public void serialize(Base v, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (v == null) {
        gen.writeNull();
      } else if (v instanceof Resource) {
        gen.writeRawValue(FHIR3JsonUtil.instance.toJsonString((Resource) v));
      }  else if (v instanceof Type) {
        gen.writeObject(instance.trySerializeType((Type) v));
      } else if (isFHIRList(v)) {
         Bundle b = new Bundle().setType(COLLECTION);
        ((List<?>) v).forEach(
            x -> b.addEntry(new Bundle.BundleEntryComponent().setResource((Resource) x)));
        gen.writeRawValue(FHIR3JsonUtil.instance.toJsonString(b));
      } else {
        gen.writeObject(v);
      }
    }

    private boolean isFHIRList(IBase v) {
      return v instanceof List && ((List<?>) v).stream().allMatch(x -> x instanceof Resource);
    }
  }

  public static class FHIRDeserializer extends JsonDeserializer<Base> {

    @Override
    public Base deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
      JsonNode jn = jp.readValueAsTree();
      if (jn.isArray()) {
        Bundle b = new Bundle().setType(COLLECTION);
        jn.forEach(cjn -> b
            .addEntry(
                new Bundle.BundleEntryComponent().setResource(instance.tryParseAsResource(cjn))));
        return b;
      } else {
        return (Base) instance.tryParse(jn);
      }
    }
  }


}
