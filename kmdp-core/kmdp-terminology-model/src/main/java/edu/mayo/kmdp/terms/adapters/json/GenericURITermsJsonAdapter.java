package edu.mayo.kmdp.terms.adapters.json;

import static edu.mayo.kmdp.util.Util.ensureUUID;
import static edu.mayo.kmdp.util.Util.isUUID;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GenericURITermsJsonAdapter {

  static final Logger logger = LoggerFactory.getLogger(GenericURITermsJsonAdapter.class);


  protected GenericURITermsJsonAdapter() {
    // nothing to do
  }

  public static class GenericURISerializer
      extends URITermsJsonAdapter.Serializer<ConceptIdentifier> {

    protected GenericURISerializer() {
      // nothing to do
    }

  }

  public static class GenericURIDeserializer
      extends URITermsJsonAdapter.Deserializer<ConceptIdentifier> {

    protected GenericURIDeserializer() {
      // don't instantiate
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

    @Override
    protected ConceptIdentifier resolveGeneric(TreeNode t) {
      return parseAsConceptIdentifier(t);
    }

    @Override
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
