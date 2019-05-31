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
package edu.mayo.kmdp;

import edu.mayo.kmdp.idl.Direction;
import edu.mayo.kmdp.idl.Interface;
import edu.mayo.kmdp.idl.Module;
import edu.mayo.kmdp.idl.Type;
import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.NameUtils;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.api4kp.responsecodes.ResponseCode;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.parameters.RefParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.parser.Swagger20Parser;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

public class SwaggerToIDLTranslator {

  private Swagger20Parser parser = new Swagger20Parser();
  private final static String Void = "Void";

  public Optional<String> translate(InputStream input) {
    return FileUtil.read(input)
        .flatMap(this::parse)
        .flatMap(this::doTranslate);
  }

  private Optional<String> doTranslate(Swagger swagger) {
    return initModule(swagger.getBasePath())
        .map((m) -> this.withOperations(m, swagger))
        .map(IDLSerializer::serialize);
  }

  private Module withOperations(Module m, Swagger swagger) {
    Module leafModule = getLeafModule(m);
    swagger.getPaths()
        .forEach(
            (pathStr, path) -> path.getOperations().forEach((op) -> addOperation(leafModule, op, swagger)));
    return m;
  }

  private void addOperation(Module module, Operation op, Swagger swagger) {
    op.getTags().forEach((tag) -> {
      Optional<Interface> intf = module.getInterface(tag);

      if (!intf.isPresent()) {
          this.addInterface(module, tag);
      }

      intf = module.getInterface(tag);

      intf.get().addOperation(toIDLOperation(op, swagger));
    });
  }

  private edu.mayo.kmdp.idl.Operation toIDLOperation(Operation swaggerOp,
      Swagger swagger) {
    edu.mayo.kmdp.idl.Operation idlOp = new edu.mayo.kmdp.idl.Operation();
    idlOp.setName(swaggerOp.getOperationId());

    idlOp.setReturnType(getReturnType(swaggerOp));

    swaggerOp.getParameters().stream()
        .map((p) -> toIDLParameter(p,swagger))
        .forEach(idlOp::addInput);
    return idlOp;
  }

  private edu.mayo.kmdp.idl.Parameter toIDLParameter(Parameter parameter,
      Swagger swagger) {
    if (parameter instanceof RefParameter) {
      RefParameter refParam = (RefParameter) parameter;;
        parameter = swagger.getParameter(refParam.getSimpleRef());
    }
    edu.mayo.kmdp.idl.Parameter idlParam = new edu.mayo.kmdp.idl.Parameter();
    idlParam.setDirection(Direction.IN);
    idlParam.setName(parameter.getName());
    idlParam.setType(getParameterType(parameter,swagger));

    return idlParam;
  }

  private Type getParameterType(Parameter parameter, Swagger swagger) {
    if (parameter instanceof BodyParameter) {
      Model m = ((BodyParameter) parameter).getSchema();
      if (m instanceof RefModel) {
        RefModel ref = (RefModel) m;
        //m = swagger.getDefinitions().get(ref.getSimpleRef());
        // This is where we'd need to resolve the actual UML-driven YAML
        return new Type(ref.getSimpleRef());
      }
    } else if (parameter instanceof PathParameter) {
      PathParameter path = ((PathParameter) parameter);
      return new Type(path.getType());
    } else if (parameter instanceof QueryParameter) {
      QueryParameter qry = ((QueryParameter) parameter);
      return new Type(qry.getType());
    } else if (parameter instanceof FormParameter) {
      FormParameter frm = ((FormParameter) parameter);
      return new Type(frm.getType());
    }
    throw new UnsupportedOperationException("TODO");
  }

  private Type getReturnType(Operation swaggerOp) {
    return Optional.ofNullable(swaggerOp.getResponses()
        .getOrDefault(ResponseCode.Created.getTag(),
            swaggerOp.getResponses().getOrDefault(ResponseCode.OK.getTag(),
                null)))
        .map(this::toIDLType)
        .orElse(new Type(Void));
  }

  private Type toIDLType(Response response) {
    Property schema = response.getSchema();
    return toIDLType(schema);
  }

  private Type toIDLType(Property schema) {
    if (schema instanceof ArrayProperty) {
      ArrayProperty ap = (ArrayProperty) schema;
      Type t = toIDLType(ap.getItems());
      t.setCollection(true);
      return t;
    } else if (schema instanceof RefProperty) {
      RefProperty rp = (RefProperty) schema;
      return new Type(rp.getSimpleRef());
    } else {
      return new Type(schema != null && schema.getType() != null ? schema.getType() : Void);
    }
  }

  private String s(int status) {
    return ""+status;
  }

  private Interface addInterface(Module module, String tag) {
    Interface itf = new Interface(tag);
    module.addInterface(itf);
    return itf;
  }

  private Module getLeafModule(Module m) {
    return m.getSubModules().isEmpty() ? m : getLeafModule(m.getSubModules().iterator().next());
  }

  private Optional<Module> initModule(String basePath) {
    String packageName = NameUtils.namespaceURIToPackage(basePath);
    if (Util.isEmpty(packageName)) {
      return Optional.empty();
    }
    return Arrays.stream(packageName.split("\\."))
        .map(Module::new)
        .reduce(Module::addModule);
  }

  private Optional<Swagger> parse(String s) {
    try {
      return Optional.ofNullable(parser.parse(s));
    } catch (IOException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }
}