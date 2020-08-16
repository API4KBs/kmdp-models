package edu.mayo.kmdp.surrogate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.mayo.kmdp.util.JSonUtil;
import java.io.IOException;
import org.omg.spec.api4kp._20200801.identifiers.ConceptIdentifier;

public interface LegacyConceptIdentifierTermsJsonAdapter {

  class Serializer
      extends JsonSerializer<ConceptIdentifier> {
    @Override
    public void serialize(ConceptIdentifier v, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeObject(v);
    }

    @Override
    public void serializeWithType(ConceptIdentifier value, JsonGenerator gen, SerializerProvider serializers,
        TypeSerializer typeSer) throws IOException {
      serialize(value,gen,serializers);
    }
  }

  class Deserializer
      extends JsonDeserializer<ConceptIdentifier> {

    @Override
    public ConceptIdentifier deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException {
      TreeNode t = jp.readValueAsTree();
      return parseAsConceptIdentifier(t);
    }

    protected ConceptIdentifier parseAsConceptIdentifier(TreeNode t) {
      return JSonUtil.parseJson(((ObjectNode) t), ConceptIdentifier.class)
          .orElse(null);
    }

  }

}
