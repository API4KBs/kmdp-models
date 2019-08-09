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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.TriConsumer;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;

public abstract class AbstractFHIRJsonAdapter<
    R extends IBaseResource, // Root class of Resources
    I extends IBaseResource, // Root class of Clinical Resources
    P extends IBaseParameters, // Root class of Parameters
    B extends IBaseBundle, // Root class of Bundle
    D extends IBaseDatatype // Root class of Datatypes
    > {

  protected abstract IParser getParser();

  protected AbstractFHIRJsonUtil<R, I> jsonHelper;

  protected Class<P> paramClass;
  protected Supplier<P> paramConstructor;
  protected Function<P, D> paramGetter;
  protected TriConsumer<P, String, D> paramSetter;

  protected AbstractFHIRJsonAdapter(
      AbstractFHIRJsonUtil<R, I> jsonHelper,
      Class<P> paramClass,
      Supplier<P> paramConstructor,
      Function<P, D> paramGetter,
      TriConsumer<P, String, D> paramSetter) {
    this.jsonHelper = jsonHelper;
    this.paramClass = paramClass;
    this.paramConstructor = paramConstructor;
    this.paramGetter = paramGetter;
    this.paramSetter = paramSetter;
  }

  // Will parse as either Resource, or Datatype
  protected IBase tryParse(JsonNode jn) {
    if (jn.get("resourceType") != null) {
      return getParser().parseResource(jn.toString());
    } else {
      return tryParseType(jn);
    }
  }

  // Will parse as either Resource, or - in case of Datatypes - as a wrapping Parameter
  protected R tryParseAsResource(JsonNode jn) {
    if (jn.get("resourceType") != null) {
      return (R) getParser().parseResource(jn.toString());
    } else {
      return (R) parseTypeAsParam(jn);
    }
  }

  private D tryParseType(JsonNode jn) {
    P parameters = parseTypeAsParam(jn);
    return parameters != null ? paramGetter.apply(parameters) : null;
  }

  private P parseTypeAsParam(JsonNode jn) {
    P paramShell = paramConstructor.get();
    paramSetter.accept(paramShell, "value", null);
    String template = jsonHelper.toJsonString(paramShell);
    Optional<JsonNode> parent = JSonUtil.readJson(template.getBytes());
    if (parent.isPresent()) {

      Iterator<String> jnFields = jn.fieldNames();
      // skip the JSON field called "name" to get the value
      jnFields.next();
      String pName = jnFields.next();

      ((ObjectNode) parent.get().get("parameter").get(0))
          .set(pName, jn.get(pName));

      return getParser().parseResource(paramClass, parent.get().toString());
    } else {
      return paramConstructor.get();
    }
  }


  protected JsonNode trySerializeType(D t) {
    // wrap in P to serialize
    P p = paramConstructor.get();
    paramSetter.accept(p, "value", t);
    Optional<JsonNode> node = JSonUtil.readJson(jsonHelper.toJsonString(p).getBytes());
    return node
        .map(jsonNode -> jsonNode.get("parameter").get(0))
        .orElse(JsonNodeFactory.instance.nullNode());
  }


}
