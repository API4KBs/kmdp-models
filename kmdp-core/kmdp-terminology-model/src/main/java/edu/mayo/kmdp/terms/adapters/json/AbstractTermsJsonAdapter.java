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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.series.Series;
import edu.mayo.kmdp.util.URIUtil;
import edu.mayo.kmdp.util.Util;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTermsJsonAdapter {

  static final Logger logger = LoggerFactory.getLogger(AbstractTermsJsonAdapter.class);

  protected AbstractTermsJsonAdapter() {
  }



  public abstract static class AbstractJsonSerializer<T extends Term> extends JsonSerializer<T> {
    @Override
    public void serialize(T v, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeObject(v.asConcept());
    }
  }

  public abstract static class AbstractKeySerializer<T extends Term> extends AbstractJsonSerializer<T> {

    @Override
    public void serialize(T v, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeFieldName(v.getConceptUUID() != null ? v.getConceptUUID().toString() : v.getTag());
    }
  }


  public abstract static class AbstractKeyDeserializer<T extends Term> extends KeyDeserializer {

    public abstract T deserializeKey(String key, DeserializationContext ctxt) throws IOException;

  }


  public abstract static class AbstractJsonDeserializer<T extends Term> extends JsonDeserializer<T> {
    private static final String DEFAULT_KEY = "tag";

    @Override
    public T deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException {
      TreeNode t = jp.readValueAsTree();
      return parse(t);
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

      Optional<T> resolved = tagNode.flatMap(this::resolve);
      if (tagNode.isPresent() && !resolved.isPresent()) {
        logger.warn("Unable to resolve concept ID {}", tagNode.get());
      }

      T resolvedTerm = resolved.orElse(null);
      if (resolvedTerm instanceof Series<?>) {
        String versionTag = getVersionNode(t).orElse(null);
        if (versionTag != null) {
          resolvedTerm = (T) ((Series<?>) resolvedTerm).getVersion(versionTag).orElse(null);
        }
      }
      return resolvedTerm;
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

    protected abstract T[] getValues();

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
