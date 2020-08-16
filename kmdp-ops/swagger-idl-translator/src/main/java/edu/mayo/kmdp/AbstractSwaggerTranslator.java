package edu.mayo.kmdp;

import static edu.mayo.kmdp.idl.IDLNameUtil.applyTypeNameMappings;

import edu.mayo.kmdp.idl.Direction;
import edu.mayo.kmdp.idl.Exception;
import edu.mayo.kmdp.idl.IDLNameUtil;
import edu.mayo.kmdp.idl.Interface;
import edu.mayo.kmdp.idl.Module;
import edu.mayo.kmdp.idl.Operation;
import edu.mayo.kmdp.idl.Type;
import edu.mayo.kmdp.idl.ext.Service;
import edu.mayo.kmdp.util.NameUtils;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.api4kp.responsecodes.ResponseCodeSeries;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.RefParameter;
import io.swagger.parser.Swagger20Parser;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractSwaggerTranslator {

  protected Swagger20Parser parser = new Swagger20Parser();
  protected String title;


  protected Service toService(Module m, Swagger swagger) {
    return new Service(swagger.getInfo().getTitle(), swagger.getInfo().getDescription(),
        toInterfaceMap(m));
  }

  protected Map<Interface, List<Operation>> toInterfaceMap(Module m) {
    return flatten(m)
        .reduce(new HashMap<>(), (acc, map) -> {
          acc.putAll(map);
          return acc;
        });
  }


  public static Stream<Map<Interface, List<Operation>>> flatten(Module module) {
    return Stream.concat(
        Stream.of(getInterfacesMap(module)).filter(m -> !m.isEmpty()),
        module.getSubModules().stream()
            .flatMap(SwaggerToDocXTranslator::flatten)); // recursion here
  }

  protected static Map<Interface, List<edu.mayo.kmdp.idl.Operation>> getInterfacesMap(Module module) {
    return module.getInterfaces().stream()
        .collect(Collectors.toMap(
            Function.identity(),
            i -> new ArrayList<>(i.listOperations())));
  }


  protected Module withStructs(Module m, Swagger swagger, TypeProvider provider) {
    swagger.getDefinitions()
        .forEach((name, model) -> provider.registerType(new Type(applyTypeNameMappings(name))));
    return m;
  }

  protected Module withOperations(Module m, Swagger swagger, TypeProvider provider) {
    Module leafModule = getLeafModule(m);
    if (swagger.getPaths() != null) {
      swagger.getPaths()
          .forEach(
              (pathStr, path) -> path.getOperations()
                  .forEach(op -> addOperation(leafModule, op, swagger, provider, pathStr, path)));
    }
    return m;
  }

  protected void addOperation(
      Module module, io.swagger.models.Operation op, Swagger swagger, TypeProvider provider,
      String pathStr, Path path) {
    op.getTags()
        .stream()
        .map(IDLNameUtil::toIdentifier)
        .forEach(tag -> {
          Optional<Interface> intf = module.getInterface(tag);

          if (!intf.isPresent()) {
            module.addInterface(tag);
          }
          intf = module.getInterface(tag);

          intf.ifPresent(i -> {
            if (swagger.getInfo() != null) {
              i.setDocumentation(swagger.getInfo().getDescription());
            }
            i.addOperation(toIDLOperation(op, swagger, provider, pathStr, path));
          });
        });
  }

  protected edu.mayo.kmdp.idl.Operation toIDLOperation(
      io.swagger.models.Operation swaggerOp,
      Swagger swagger,
      TypeProvider provider,
      String pathStr, Path path) {
    edu.mayo.kmdp.idl.Operation idlOp = new edu.mayo.kmdp.idl.Operation();
    idlOp.setName(swaggerOp.getOperationId());
    idlOp.setSummary(swaggerOp.getSummary());
    idlOp.setDescription(swaggerOp.getDescription());

    idlOp.setRestPath(pathStr);
    if (path.getGet() == swaggerOp) {
      idlOp.setActionVerb("GET");
    } else if (path.getDelete() == swaggerOp) {
      idlOp.setActionVerb("DELETE");
    } else if (path.getHead() == swaggerOp) {
      idlOp.setActionVerb("HEAD");
    } else if (path.getPatch() == swaggerOp) {
      idlOp.setActionVerb("PATCH");
    } else if (path.getPost() == swaggerOp) {
      idlOp.setActionVerb("POST");
    } else if (path.getPut() == swaggerOp) {
      idlOp.setActionVerb("PUT");
    }

    idlOp.setReturnType(provider.getReturnType(swaggerOp));

    swaggerOp.getResponses().forEach((code,resp) -> {
      if (! code.startsWith("2")) {
        Exception ex = new Exception();
        ex.setCodeLabel(ResponseCodeSeries.resolve(code).orElseThrow().getName());
        ex.setCode(Integer.parseInt(code));
        ex.setType(provider.toIDLType(resp));
        ex.setDescription(resp.getDescription());
        idlOp.addException(ex);
      }
    });

    swaggerOp.getParameters().stream()
        .map(p -> toIDLParameter(p, swagger, provider))
        .forEach(idlOp::addInput);
    return idlOp;
  }

  protected edu.mayo.kmdp.idl.Parameter toIDLParameter(Parameter parameter,
      Swagger swagger,
      TypeProvider provider) {
    if (parameter instanceof RefParameter) {
      RefParameter refParam = (RefParameter) parameter;
      parameter = swagger.getParameter(refParam.getSimpleRef());
    }
    edu.mayo.kmdp.idl.Parameter idlParam = new edu.mayo.kmdp.idl.Parameter();
    idlParam.setDirection(Direction.IN);
    idlParam.setName(IDLNameUtil.toIdentifier(parameter.getName()));
    idlParam.setType(provider.getParameterType(parameter));
    idlParam.setRequired(parameter.getRequired());
    idlParam.setDescription(parameter.getDescription());

    return idlParam;
  }


  protected Module getLeafModule(Module m) {
    return m.getSubModules().isEmpty() ? m : getLeafModule(m.getSubModules().iterator().next());
  }

  protected Optional<Module> initModule(String basePath) {
    String packageName = NameUtils.namespaceURIStringToPackage("http:/" + basePath);
    if (Util.isEmpty(packageName)) {
      return Optional.empty();
    }

    List<String> packageNames =
        Arrays.stream(packageName.split("\\."))
            .collect(Collectors.toCollection(LinkedList::new));
    packageNames.add(0, "ROOT");

    return Optional.ofNullable(
        buildModuleChain(
            new ArrayDeque<>(packageNames), null));
  }

  protected Module buildModuleChain(Deque<String> names, Module parent) {
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

  protected Optional<Swagger> parse(String s) {
    try {
      return Optional.ofNullable(parser.parse(s));
    } catch (IOException e) {
      logError(e.getMessage(), e);
      return Optional.empty();
    }
  }

  protected abstract void logError(String message, IOException e);
}
