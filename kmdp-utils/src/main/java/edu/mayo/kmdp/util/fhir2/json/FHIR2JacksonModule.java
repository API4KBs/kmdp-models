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

import ca.uhn.fhir.model.api.BaseElement;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.type.SimpleType;

public class FHIR2JacksonModule extends SimpleModule {

  private static final String NAME = "FHIR-DSTU2-Jacson-Module";


  public FHIR2JacksonModule() {
    this(true);
  }

  public FHIR2JacksonModule(boolean withDataTypesAsRoot) {
    super(NAME, new Version(1, 0, 2, "LATEST", FHIR2JacksonModule.class.getPackage().toString(),
        FHIR2JacksonModule.class.getName()));

    SimpleSerializers simpleSerializers = new SimpleSerializers();
    SimpleDeserializers simpleDeserializers = new HierarchicalSimpleDeserializers();

    simpleSerializers.addSerializer(BaseElement.class, new FHIR2JsonAdapter.FHIRSerializer());
    simpleDeserializers.addDeserializer(BaseElement.class, new FHIR2JsonAdapter.FHIRDeserializer());

    setSerializers(simpleSerializers);
    setDeserializers(simpleDeserializers);
  }

  private static class HierarchicalSimpleDeserializers extends SimpleDeserializers {

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
  }
}