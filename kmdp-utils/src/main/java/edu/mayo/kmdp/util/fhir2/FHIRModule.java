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
package edu.mayo.kmdp.util.fhir2;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.BaseResource;
import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.type.SimpleType;

import java.io.IOException;

public class FHIRModule extends SimpleModule {

  private static final String NAME = "FHIRModule";

  public FHIRModule() {
    super(NAME);

    SimpleSerializers simpleSerializers = new SimpleSerializers();
    simpleSerializers.addSerializer(BaseResource.class, new JsonSerializer<BaseResource>() {
      @Override
      public void serialize(BaseResource parameters, JsonGenerator jsonGenerator,
          SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        FhirContext ctx = FhirContext.forDstu2();
        IParser jsonParser = ctx.newJsonParser();
        jsonParser.setPrettyPrint(true);
        String json = jsonParser.encodeResourceToString(parameters);

        jsonGenerator.writeRaw(json);
      }
    });

    SimpleDeserializers simpleDeserializers = new SimpleDeserializers() {

      @Override
      public JsonDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config,
          BeanDescription beanDesc) throws JsonMappingException {
        Class<?> clazz = type.getRawClass();
        JsonDeserializer<?> jsonDeserializer = null;

        while (jsonDeserializer == null && clazz != null) {
          jsonDeserializer = super
              .findBeanDeserializer(SimpleType.constructUnsafe(clazz), config, beanDesc);
          clazz = clazz.getSuperclass();
        }

        return jsonDeserializer;
      }
    };
    simpleDeserializers.addDeserializer(BaseResource.class, new JsonDeserializer<BaseResource>() {

      @Override
      public BaseResource deserialize(JsonParser jsonParser,
          DeserializationContext deserializationContext)
          throws IOException, JsonProcessingException {
        FhirContext ctx = FhirContext.forDstu2();
        IParser fhirJsonParser = ctx.newJsonParser();

        return (BaseResource) fhirJsonParser.parseResource(jsonParser.readValueAsTree().toString());
      }
    });

    setSerializers(simpleSerializers);
    setDeserializers(simpleDeserializers);
  }
}