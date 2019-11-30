/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp;

import edu.mayo.kmdp.idl.Direction;
import edu.mayo.kmdp.idl.IDLNameUtil;
import edu.mayo.kmdp.idl.Interface;
import edu.mayo.kmdp.idl.Module;
import edu.mayo.kmdp.idl.Type;
import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.NameUtils;
import edu.mayo.kmdp.util.StreamUtil;
import edu.mayo.kmdp.util.Util;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.RefParameter;
import io.swagger.parser.Swagger20Parser;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwaggerToIDLTranslator {

  private Swagger20Parser parser = new Swagger20Parser();

  private static final Logger logger = LoggerFactory.getLogger(SwaggerToIDLTranslator.class);


  public Optional<String> translate(List<InputStream> inputs) {
    return doTranslate(
        inputs.stream()
            .map(FileUtil::read)
            .flatMap(StreamUtil::trimStream)
            .map(this::parse)
            .flatMap(StreamUtil::trimStream)
            .collect(Collectors.toList()));
  }

  private Optional<String> doTranslate(List<Swagger> swaggers) {
    Swagger swagger = swaggers.get(0);

    Optional<Module> root = initModule(swagger.getBasePath());
    if (!root.isPresent()) {
      return Optional.empty();
    }

    TypeProvider provider = new TypeProvider(swaggers, root.get());

    return root
        .map(m -> this.withOperations(m, swagger, provider))
        .map(m -> this.withStructs(m, swagger, provider))
        .map(m -> new ModuleSorter().sort(m))
        .map(IDLSerializer::serialize);
  }


  private Module withStructs(Module m, Swagger swagger, TypeProvider provider) {
    swagger.getDefinitions()
        .forEach((name, model) -> provider.registerType(new Type(name)));
    return m;
  }

  private Module withOperations(Module m, Swagger swagger, TypeProvider provider) {
    Module leafModule = getLeafModule(m);
    swagger.getPaths()
        .forEach(
            (pathStr, path) -> path.getOperations()
                .forEach(op -> addOperation(leafModule, op, swagger, provider)));
    return m;
  }

  private void addOperation(Module module, Operation op, Swagger swagger, TypeProvider provider) {
    op.getTags()
        .stream()
        .map(IDLNameUtil::toIdentifier)
        .forEach(tag -> {
          Optional<Interface> intf = module.getInterface(tag);

          if (!intf.isPresent()) {
            module.addInterface(tag);
          }
          intf = module.getInterface(tag);

          intf.ifPresent(i -> i.addOperation(toIDLOperation(op, swagger, provider)));
        });
  }

  private edu.mayo.kmdp.idl.Operation toIDLOperation(
      Operation swaggerOp,
      Swagger swagger,
      TypeProvider provider) {
    edu.mayo.kmdp.idl.Operation idlOp = new edu.mayo.kmdp.idl.Operation();
    idlOp.setName(swaggerOp.getOperationId());

    idlOp.setReturnType(provider.getReturnType(swaggerOp));

    swaggerOp.getParameters().stream()
        .map(p -> toIDLParameter(p, swagger, provider))
        .forEach(idlOp::addInput);
    return idlOp;
  }

  private edu.mayo.kmdp.idl.Parameter toIDLParameter(Parameter parameter,
      Swagger swagger,
      TypeProvider provider) {
    if (parameter instanceof RefParameter) {
      RefParameter refParam = (RefParameter) parameter;
      parameter = swagger.getParameter(refParam.getSimpleRef());
    }
    edu.mayo.kmdp.idl.Parameter idlParam = new edu.mayo.kmdp.idl.Parameter();
    idlParam.setDirection(Direction.IN);
    idlParam.setName(parameter.getName());
    idlParam.setType(provider.getParameterType(parameter));

    return idlParam;
  }


  private Module getLeafModule(Module m) {
    return m.getSubModules().isEmpty() ? m : getLeafModule(m.getSubModules().iterator().next());
  }

  private Optional<Module> initModule(String basePath) {
    String packageName = NameUtils.namespaceURIStringToPackage("http:/"+basePath);
    if (Util.isEmpty(packageName)) {
      return Optional.empty();
    }

    List<String> packageNames = new LinkedList<>(
        Arrays.stream(packageName.split("\\."))
            .collect(Collectors.toList()));
    packageNames.add(0,"ROOT");

    return Optional.ofNullable(
        buildModuleChain(
            new ArrayDeque<>(packageNames), null));
  }

  private Module buildModuleChain(Deque<String> names, Module parent) {
    if (names.isEmpty()) {
      return null;
    }
    String moduleName = names.pop();
    Module m = new Module(moduleName);
    if (parent != null) {
      parent.addModule(m);
    }
    buildModuleChain(names, m);
    return m;
  }

  private Optional<Swagger> parse(String s) {
    try {
      return Optional.ofNullable(parser.parse(s));
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      return Optional.empty();
    }
  }
}