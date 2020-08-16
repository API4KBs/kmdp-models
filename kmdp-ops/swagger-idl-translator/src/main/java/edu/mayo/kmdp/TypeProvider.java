package edu.mayo.kmdp;

import static edu.mayo.kmdp.idl.IDLNameUtil.applyFieldNameMappings;
import static edu.mayo.kmdp.idl.IDLNameUtil.applyTypeNameMappings;

import edu.mayo.kmdp.idl.IDLNameUtil;
import edu.mayo.kmdp.idl.Module;
import edu.mayo.kmdp.idl.Struct;
import edu.mayo.kmdp.idl.Type;
import edu.mayo.kmdp.util.NameUtils;
import edu.mayo.kmdp.util.StreamUtil;
import edu.mayo.ontology.taxonomies.api4kp.responsecodes._2011.ResponseCode;
import io.swagger.models.ComposedModel;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.models.properties.UUIDProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class TypeProvider {

  private static final String VOID = "void";

  private final Module context;
  private List<Swagger> swaggers;
  private Map<String,Type> index;

  TypeProvider(List<Swagger> swaggers, Module rootModule) {
    this.swaggers = new ArrayList<>(swaggers);
    this.context = rootModule;
    this.index = new HashMap<>();
  }


  public Type registerType(Type type) {
    if (isTypeRegistered(type)) {
      return index.get(indexKey(type));
    }

    if (isPrimitive(type)) {
      return new Type(primitiveMap(type.getName()), type.isCollection());
    }

    Model model = find(applyTypeNameMappings(type.getName()), swaggers);

    Struct struct = toStruct(type, model);

    context.insertStruct(struct);
    type.linkStruct(struct);
    index.put(indexKey(type),type);

    if (model.getProperties() != null) {
      model.getProperties().forEach(
          (key, prop) -> addField(struct, key, prop, swaggers));
    } else {
      if ("Bindings".equals(type.getName())) {
        // TODO
        addField(struct, "entries", new StringProperty(), swaggers);
      } else {
        addField(struct, "value", new UUIDProperty(), swaggers);
      }
    }

    return type;
  }

  private void addField(Struct struct, String key, Property prop, List<Swagger> swaggers) {
    String fieldName = IDLNameUtil.toIdentifier(key);
    Type type = toTypeDeclaration(prop);
    Model model = isPrimitive(type) ? null : find(applyTypeNameMappings(type.getName()), swaggers);
    if (model != null) {
      String fqn = IDLNameUtil.toFQName(getNamespace(model)
          .map(NameUtils::namespaceURIStringToPackage)
          .orElse(""), type.getName());
      if (index.containsKey(indexKey(fqn,type.isCollection()))) {
        struct.addField(fieldName, index.get(indexKey(fqn,type.isCollection())));
        return;
      }
    }
    struct.addField(fieldName, toIDLType(prop));
  }


  private Struct toStruct(Type type, Model model) {
    String packageName = getNamespace(model)
        .map(NameUtils::namespaceURIStringToPackage)
        .orElse("");
    return new Struct(type.getName(), packageName);
  }

  private Optional<String> getNamespace(Model model) {
    if (model instanceof ModelImpl) {
      ModelImpl m = (ModelImpl) model;
      if (m.getXml() != null) {
        return Optional.ofNullable(m.getXml().getNamespace());
      }
    } else if (model instanceof ComposedModel) {
      ComposedModel cm = (ComposedModel) model;
      return getNamespace(cm);
    }
    return Optional.empty();
  }


  Type getParameterType(Parameter parameter) {
    Type t = null;
    if (parameter instanceof BodyParameter) {
      Model m = ((BodyParameter) parameter).getSchema();
      if (m instanceof RefModel) {
        RefModel ref = (RefModel) m;
        // This is where we'd need to resolve the actual UML-driven YAML
        t = new Type(applyTypeNameMappings(ref.getSimpleRef()));
      } else if (m instanceof ModelImpl) {
        t = new Type(((ModelImpl) m).getType());
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
    } else if (parameter instanceof HeaderParameter) {
      HeaderParameter hdr = ((HeaderParameter) parameter);
      t = new Type(hdr.getType());
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

  Type toIDLType(Response response) {
    Property schema = response.getSchema();
    return toIDLType(schema);
  }

  Type toIDLType(Property schema) {
    Type t = toTypeDeclaration(schema);
    t = registerType(t);
    return t;
  }

  private Type toTypeDeclaration(Property schema) {
    Type t;
    if (schema instanceof ArrayProperty) {
      ArrayProperty ap = (ArrayProperty) schema;
      t = toTypeDeclaration(ap.getItems());
      t.setCollection(true);
      t.setDescription(ap.getDescription());
    } else if (schema instanceof RefProperty) {
      RefProperty rp = (RefProperty) schema;
      String ref = rp.getSimpleRef();
      ref = ref.substring(ref.lastIndexOf('/') + 1);
      t = new Type(applyTypeNameMappings(ref));
    } else {
      t = new Type(schema != null && schema.getType() != null ? applyTypeNameMappings(schema.getType()) : VOID);
    }
    return t;
  }


  private Model find(String typeName, List<Swagger> swaggers) {
    return swaggers.stream()
        .map(Swagger::getDefinitions)
        .map(m -> Optional.ofNullable(m.get(typeName)))
        .flatMap(StreamUtil::trimStream)
        .map(m -> asConcreteModel(m, typeName, swaggers))
        .flatMap(StreamUtil::trimStream)
        .reduce((m1,m2) -> m1.getProperties() == null ? m2 : m1)
        .orElseThrow(() -> new IllegalStateException("Unable to resolve type " + typeName))
        ;
  }

  private Optional<Model> asConcreteModel(Model m, String typeName,
      List<Swagger> swaggers) {
    if (isEnumeration(m)) {
      return Optional.ofNullable(find("ConceptIdentifier", swaggers));
    }

    if (m.getProperties() != null && !m.getProperties().isEmpty()) {
      return Optional.of(m);
    }
    if (m instanceof ComposedModel) {
      ComposedModel cm = (ComposedModel) m;
      if (cm.getChild() != null) {
        return asConcreteModel(((ComposedModel) m).getChild(), typeName, swaggers);
      }
    }
    if (m instanceof ModelImpl) {
      return Optional.of(m);
    }return Optional.empty();
  }

  private boolean isEnumeration(Model m) {
    // this needs improvement...
    if (!(m instanceof ComposedModel)) {
      return false;
    }
    ComposedModel cm = (ComposedModel) m;
    if (! cm.getInterfaces().isEmpty()) {
      RefModel rf = cm.getInterfaces().get(0);
      return rf.get$ref().contains("ConceptIdentifier");
    }
    return true;
  }


  private boolean isPrimitive(Type type) {
    switch (type.getName().toLowerCase()) {
      case "integer":
      case "void":
      case "string":
      case "number":
      case "boolean":
      case "uuid":
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
      case "uuid":
        mapped = "string";
      default:
    }
    return mapped;
  }


  private boolean isTypeRegistered(Type type) {
    return index.containsKey(indexKey(type));
  }

  private String indexKey(Type type) {
    return indexKey(type.getFullyQualifiedName(), type.isCollection());
  }


  private String indexKey(String fqn, boolean isCollection) {
    return fqn
        + (isCollection ? "*" : "");
  }

}
