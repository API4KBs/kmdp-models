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
package edu.mayo.kmdp.util.fhir.fhir2;

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
import edu.mayo.kmdp.util.fhir.AbstractFHIRJsonAdapter;
import java.io.IOException;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBase;

public class FHIR2JsonAdapter extends
    AbstractFHIRJsonAdapter<BaseResource, IResource, Parameters, Bundle, IDatatype> {

  private static IParser jsonParser = FhirContext.forDstu2().newJsonParser();
  protected static FHIR2JsonAdapter instance = new FHIR2JsonAdapter();

  private FHIR2JsonAdapter() {
    super(
        FHIR2JsonUtil.instance,
        Parameters.class,
        Parameters::new,
        FHIR2JsonAdapter::getParam,
        FHIR2JsonAdapter::setParam);
  }

  private static void setParam(Parameters p, String paramName, IDatatype value) {
    p.addParameter().setName(paramName).setValue(value);
  }

  private static IDatatype getParam(Parameters p) {
    return p.getParameter().get(0).getValue();
  }

  @Override
  protected IParser getParser() {
    return jsonParser;
  }

  public static class FHIRSerializer extends JsonSerializer<BaseElement> {

    @Override
    public void serialize(BaseElement v, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (v == null) {
        gen.writeNull();
      } else if (v instanceof IResource) {
        gen.writeRawValue(FHIR2JsonUtil.instance.toJsonString((IResource) v));
      } else if (v instanceof IDatatype) {
        gen.writeObject(instance.trySerializeType((IDatatype) v));
      }  else if (isFHIRList(v)) {
        Bundle b = new Bundle().setType(BundleTypeEnum.COLLECTION);
        ((List<?>) v).forEach(x -> b.addEntry(new Bundle.Entry().setResource((IResource) x)));
        gen.writeRawValue(FHIR2JsonUtil.instance.toJsonString(b));
      } else {
        gen.writeObject(v);
      }
    }

    private boolean isFHIRList(IBase v) {
      return v instanceof List && ((List<?>) v).stream().allMatch(x -> x instanceof IResource);
    }
  }


  public static class FHIRDeserializer extends JsonDeserializer<BaseElement> {

    @Override
    public BaseElement deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
      JsonNode jn = jp.readValueAsTree();
      if (jn.isArray()) {
        Bundle b = new Bundle().setType(BundleTypeEnum.COLLECTION);
        jn.forEach(
            cjn -> b.addEntry(new Bundle.Entry().setResource(instance.tryParseAsResource(cjn))));
        return b;
      } else {
        return (BaseElement) instance.tryParse(jn);
      }
    }
  }

}
