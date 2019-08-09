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

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;

public abstract class AbstractFHIRJacksonModule<B> extends SimpleModule {

  protected Class<B> bClass;
  protected transient JsonDeserializer<B> deserializer;


  public AbstractFHIRJacksonModule(String name, Version latest,
      Class<B> bClass,
      JsonSerializer<B> fhirSerializer,
      JsonDeserializer<B> fhirDeserializer) {
    super(name, latest);
    this.bClass = bClass;
    this.deserializer = fhirDeserializer;

    SimpleSerializers simpleSerializers = new SimpleSerializers();
    SimpleDeserializers simpleDeserializers = new HierarchicalSimpleDeserializers();

    simpleSerializers.addSerializer(bClass, fhirSerializer);
    simpleDeserializers.addDeserializer(bClass, fhirDeserializer);

    setSerializers(simpleSerializers);
    setDeserializers(simpleDeserializers);
  }

  public class HierarchicalSimpleDeserializers extends SimpleDeserializers {
    @Override
    public JsonDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config,
        BeanDescription beanDesc) {
      Class<?> clazz = type.getRawClass();

      if (isFHIRClass(clazz)) {
        return getFHIRDeserializer();
      } else {
        return null;
      }
    }
  }

  protected JsonDeserializer<B> getFHIRDeserializer() {
    return deserializer;
  }

  protected boolean isFHIRClass(Class<?> clazz) {
    return bClass.isAssignableFrom(clazz);
  }

}
