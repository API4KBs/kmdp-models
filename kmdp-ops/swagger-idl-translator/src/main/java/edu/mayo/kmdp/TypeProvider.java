package edu.mayo.kmdp;

import edu.mayo.kmdp.idl.Module;
import edu.mayo.kmdp.idl.Struct;
import edu.mayo.kmdp.idl.Struct.Field;
import edu.mayo.kmdp.idl.Type;
import edu.mayo.kmdp.terms.generator.util.HierarchySorter;
import edu.mayo.kmdp.util.StreamUtil;
import edu.mayo.ontology.taxonomies.api4kp.responsecodes._2011.ResponseCode;
import io.swagger.models.ComposedModel;
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
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

class TypeProvider {

  private static final String VOID = "void";

  private final Module context;
  private List<Swagger> swaggers;

  TypeProvider(List<Swagger> swaggers, Module rootModule) {
    this.swaggers = new ArrayList<>(swaggers);
    this.context = rootModule;
  }


  public Type registerType(Type type) {
    if (isPrimitive(type)) {
      return new Type(primitiveMap(type.getName()), type.isCollection());
    }

    Struct struct = new Struct(type.getName(), type.getPackageName());
    Model model = find(type.getName(), swaggers);

    if (model.getProperties() != null) {
      model.getProperties().forEach(
          (key, prop) -> struct.addField(key, toIDLType(prop)));
    }

    context.addStruct(struct);

    return type;
  }


  Type getParameterType(Parameter parameter) {
    Type t = null;
    if (parameter instanceof BodyParameter) {
      Model m = ((BodyParameter) parameter).getSchema();
      if (m instanceof RefModel) {
        RefModel ref = (RefModel) m;
        // This is where we'd need to resolve the actual UML-driven YAML
        t = new Type(ref.getSimpleRef());
      }
    } else if (parameter instanceof PathParameter) {
      PathParameter path = ((PathParameter) parameter);
      t = new Type(path.getType());
    } else if (parameter instanceof QueryParameter) {
      QueryParameter qry = ((QueryParameter) parameter);
      t = new Type(qry.getType());
    } else if (parameter instanceof FormParameter) {
      FormParameter frm = ((FormParameter) parameter);
      t = new Type(frm.getType());
    }
    if (t == null) {
      throw new UnsupportedOperationException("TODO");
    }
    t = registerType(t);
    return t;
  }

  Type getReturnType(Operation swaggerOp) {
    return Optional.ofNullable(swaggerOp.getResponses()
        .getOrDefault(ResponseCode.Created.getTag(),
            swaggerOp.getResponses().getOrDefault(ResponseCode.OK.getTag(),
                null)))
        .map(this::toIDLType)
        .orElse(new Type(VOID));
  }

  private Type toIDLType(Response response) {
    Property schema = response.getSchema();
    return toIDLType(schema);
  }

  Type toIDLType(Property schema) {
    Type t;
    if (schema instanceof ArrayProperty) {
      ArrayProperty ap = (ArrayProperty) schema;
      t = toIDLType(ap.getItems());
      t.setCollection(true);
    } else if (schema instanceof RefProperty) {
      RefProperty rp = (RefProperty) schema;
      String ref = rp.getSimpleRef();
      ref = ref.substring(ref.lastIndexOf('/') + 1);
      t = new Type(ref);
    } else {
      t = new Type(schema != null && schema.getType() != null ? schema.getType() : VOID);
    }
    t = registerType(t);
    return t;
  }


  private Model find(String typeName, List<Swagger> swaggers) {
    return swaggers.stream()
        .map(Swagger::getDefinitions)
        .map(m -> Optional.ofNullable(m.get(typeName)))
        .flatMap(StreamUtil::trimStream)
        .map(this::asConcreteModel)
        .flatMap(StreamUtil::trimStream)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Unable to resolve type " + typeName))
        ;
  }

  private Optional<Model> asConcreteModel(Model m) {
    if (m.getProperties() != null && !m.getProperties().isEmpty()) {
      return Optional.of(m);
    }
    if (m instanceof ComposedModel) {
      ComposedModel cm = (ComposedModel) m;
      if (cm.getChild() != null) {
        return asConcreteModel(((ComposedModel) m).getChild());
      }
    }
    return Optional.empty();
  }


  private boolean isPrimitive(Type type) {
    switch (type.getName().toLowerCase()) {
      case "integer":
      case "void":
      case "string":
      case "number":
      case "boolean":
        return true;
      default:
        return false;
    }
  }

  private String primitiveMap(String typeName) {
    String mapped = typeName;
    switch (typeName.toLowerCase()) {
      case "integer":
      case "number":
        mapped = "long";
        break;
      default:
    }
    return mapped;
  }

  public void sortTypeDeclarations(Module m) {
    HierarchySorter<Struct> sorter = new HierarchySorter<>();

    List<Struct> sortedStructs = sorter.linearize(
        m.getStructs(),
        getDependencies(m.getStructMap()));

    m.getStructMap().clear();
    sortedStructs.forEach(m::addStruct);
  }

  private Map<Struct, Set<Struct>> getDependencies(Map<String, Struct> structs) {
    Map<Struct, Set<Struct>> deps = new HashMap<>();
    for (Struct s : structs.values()) {
      for (Field f : s.getFields()) {
        String fName = f.getType().getName();
        if (structs.containsKey(fName)) {
          deps.computeIfAbsent(s, x -> new HashSet<>())
              .add(structs.get(fName));
        }
      }
    }
    return deps;
  }
}
