package edu.mayo.kmdp.terms.adapters.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.series.Series;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.URIUtil;
import edu.mayo.kmdp.util.Util;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface AbstractTermsJsonAdapter {

  Logger logger = LoggerFactory.getLogger(AbstractTermsJsonAdapter.class);

  class AbstractSerializer<T extends Term> extends JsonSerializer<T> {
    @Override
    public void serialize(T v, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeObject(v.asConcept());
    }

    @Override
    public void serializeWithType(T value, JsonGenerator gen, SerializerProvider serializers,
        TypeSerializer typeSer) throws IOException {
      serialize(value,gen,serializers);
    }
  }

  abstract class AbstractKeySerializer<T extends Term> extends AbstractSerializer<T> {

    @Override
    public void serialize(T v, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeFieldName(v.getConceptUUID() != null ? v.getConceptUUID().toString() : v.getTag());
    }
  }


  abstract class AbstractKeyDeserializer<T extends Term> extends KeyDeserializer {

    public abstract T deserializeKey(String key, DeserializationContext ctxt) throws IOException;

  }


  abstract class AbstractDeserializer<T extends Term> extends JsonDeserializer<T> {
    private static final String DEFAULT_KEY = "tag";

    @Override
    public T deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException {
      TreeNode t = jp.readValueAsTree();
      return parse(t);
    }

    @Override
    public T deserializeWithType(JsonParser p, DeserializationContext ctxt,
        TypeDeserializer typeDeserializer) throws IOException {
      return deserialize(p, ctxt);
    }

    protected T parse(TreeNode t) {
      if (t == null || t.isMissingNode()) {
        return null;
      }

      Optional<String> tagNode = t.isObject()
          ? getTag(t)
          : Optional.ofNullable(((TextNode) t).asText());
      if (!tagNode.isPresent()) {
        logger.warn("Unable to resolve concept {}", t);
      }

      return tagNode
          .flatMap(tag -> this.resolveAsKnownTerm(t,tag))
          .orElseGet(() -> this.resolveGeneric(t));
    }


    protected abstract T[] getValues();

    protected T resolveGeneric(TreeNode t) {
      throw new UnsupportedOperationException();
    }

    protected Optional<T> resolveAsKnownTerm(TreeNode t,String tagNode) {
      return resolve(tagNode)
          .flatMap(resTerm -> resolveVersion(resTerm, t));
    }

    @SuppressWarnings("unchecked")
    protected Optional<T> resolveVersion(T resolvedTerm, TreeNode t) {
      if (resolvedTerm instanceof Series<?>) {
        Optional<T> versionedTerm = (Optional<T>) getVersionNode(t)
            .flatMap(((Series<?>) resolvedTerm)::getVersion);
        if (versionedTerm.isPresent()) {
          return versionedTerm;
        }
      }
      return Optional.of(resolvedTerm);
    }

    protected ConceptIdentifier parseAsConceptIdentifier(TreeNode t) {
      return JSonUtil.parseJson(((ObjectNode) t), ConceptIdentifier.class)
          .orElse(null);
    }

    protected Optional<String> getTag(TreeNode t) {
      return getIdNode(t,getKey())
          .map(TextNode::asText);
    }

    protected String getKey() {
      return DEFAULT_KEY;
    }

    protected Optional<TextNode> getIdNode(TreeNode t, String key) {
      if (! t.isObject()) {
        return Optional.empty();
      }
      JsonNode node = ((ObjectNode) t).get(key);
      return Optional.ofNullable((TextNode) node);
    }

    protected Optional<String> getVersionNode(TreeNode t) {
      if (t.isObject()) {
        TreeNode nsNode = t.get("namespace");
        if (nsNode == null) {
          return Optional.empty();
        }
        TextNode versionNode = (TextNode) nsNode.get("version");
        if (versionNode == null) {
          return Optional.empty();
        }
        return Optional.of(versionNode)
            .map(TextNode::asText);
      } else {
        return Optional.empty();
      }
    }

    protected Optional<T> resolveUUID(UUID uuid) {
      return resolve(uuid.toString());
    }

    protected Optional<T> resolve(String tag) {
      if (Util.isEmpty(tag)) {
        return Optional.empty();
      }
      return Arrays.stream(getValues())
          .filter(trm -> matches(trm,tag))
          .findAny();
    }

    private boolean matches(Term trm, String code) {
      if (code == null) {
        return false;
      }
      return
          trm.getTag().equals(code)
              || (Util.isUUID(code) && trm.getConceptUUID().equals(Util.toUUID(code)))
              || (URIUtil.isUri(code) && trm.getConceptId().toString().equals(code))
              || trm.getTags().contains(code)
          ;
    }

  }

}
