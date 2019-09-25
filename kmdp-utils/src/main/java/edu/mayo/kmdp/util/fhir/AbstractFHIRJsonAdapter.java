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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;

public abstract class AbstractFHIRJsonAdapter<
    R extends IBaseResource, // Root class of Resources
    I extends IBaseResource, // Root class of Clinical Resources
    P extends IBaseParameters, // Root class of Parameters
    D extends IBaseDatatype // Root class of Datatypes
    > {

  protected abstract IParser getParser();

  protected AbstractFHIRJsonUtil<I> jsonHelper;

  protected Class<P> paramClass;
  protected Supplier<P> paramConstructor;
  protected Function<P, D> paramGetter;
  protected BiConsumer<P, D> paramSetter;

  protected AbstractFHIRJsonAdapter(
      AbstractFHIRJsonUtil<I> jsonHelper,
      Class<P> paramClass,
      Supplier<P> paramConstructor,
      Function<P, D> paramGetter,
      BiConsumer<P, D> paramSetter) {
    this.jsonHelper = jsonHelper;
    this.paramClass = paramClass;
    this.paramConstructor = paramConstructor;
    this.paramGetter = paramGetter;
    this.paramSetter = paramSetter;
  }

  // Will parse as either Resource, or Datatype
  protected IBase tryParse(JsonNode jn) {
    if (isResource(jn)) {
      return getParser().parseResource(jn.toString());
    } else {
      return tryParseType(jn);
    }
  }

  private boolean isResource(JsonNode jn) {
    return jn.get("resourceType") != null;
  }

  // Will parse as either Resource, or - in case of Datatypes - as a wrapping Parameter
  protected R tryParseAsResource(JsonNode jn) {
    if (isResource(jn)) {
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
    JsonNode parent = JsonNodeFactory.instance.objectNode()
        .set("resourceType", JsonNodeFactory.instance.textNode("Parameters"));
    ((ArrayNode) parent.withArray("parameter")).add(jn);
    return getParser().parseResource(paramClass, parent.toString());
  }


  protected String trySerializeType(D t) {
    // wrap in P to serialize
    P p = paramConstructor.get();
    paramSetter.accept(p, t);
    String paramS = jsonHelper.toJsonString((I) p);
    paramS = paramS.substring(
        paramS.indexOf('{', 1),
        paramS.length() - 2);
    return paramS;
  }


}
