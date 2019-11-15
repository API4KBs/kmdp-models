package edu.mayo.kmdp.terms.adapters.json;

import static edu.mayo.kmdp.util.Util.ensureUUID;
import static edu.mayo.kmdp.util.Util.isUUID;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.URIUtil;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;

public abstract class GenericURITermsJsonAdapter extends URITermsJsonAdapter {

  protected GenericURITermsJsonAdapter() {
    // nothing to do
  }

  public static class GenericSerializer extends URITermsJsonAdapter.Serializer<ConceptIdentifier> {

    protected GenericSerializer() {
      // nothing to do
    }

    @Override
    public void serializeWithType(ConceptIdentifier v, JsonGenerator gen,
        SerializerProvider serializers,
        TypeSerializer typeSer)
        throws IOException {
      serialize(v, gen, serializers);
    }
  }

  public static class GenericDeserializer extends
      URITermsJsonAdapter.Deserializer<ConceptIdentifier> {

    protected GenericDeserializer() {
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt,
        TypeDeserializer typeDeserializer) {
      return deserialize(p, ctxt);
    }

    @Override
    public ConceptIdentifier deserialize(JsonParser jp, DeserializationContext ctxt) {
      try {
        TreeNode t = jp.readValueAsTree();
        if (t.isMissingNode()) {
          return null;
        } else if (t.isObject()) {
          return JSonUtil.parseJson(((ObjectNode) t), ConceptIdentifier.class)
              .orElse(null);
        } else {
          return parse(((TextNode) t).asText());
        }
      } catch (IOException e) {
        logger.error(e.getMessage(), e);
        return null;
      }
    }

    protected ConceptIdentifier parse(String asText) {
      URI nsURI = null;

      if (asText.charAt(0) == '{') {
        int nsEnd = asText.indexOf('}');
        nsURI = URI.create(asText.substring(1,nsEnd).trim());
        asText = asText.substring(nsEnd + 1);
      }

      URI uri = extractURI(asText).orElse(null);
      if (uri == null) {
        return null;
      }

      ConceptIdentifier cid = new ConceptIdentifier()
          .withNamespace(new NamespaceIdentifier()
              .withId(URIUtil.normalizeURI(nsURI != null ? nsURI : uri)));

      String id = extractIdentifier(uri).orElse(null);
      if (id != null && isUUID(id)) {
        cid.withConceptUUID(ensureUUID(id).orElse(null));
      } else {
        cid.withTag(id);
      }

      cid.withConceptId(uri);

      String label = extractLabel(asText).orElse(null);
      cid.withLabel(label);

      return cid;
    }

    @Override
    protected ConceptIdentifier[] getValues() {
      return new ConceptIdentifier[0];
    }

    @Override
    protected Optional<ConceptIdentifier> resolveUUID(UUID uuid) {
      return Optional.empty();
    }

  }

}
