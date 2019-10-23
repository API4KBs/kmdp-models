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
package edu.mayo.kmdp.terms;

import static edu.mayo.kmdp.util.Util.ensureUUID;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.util.URIUtil;
import edu.mayo.kmdp.util.Util;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public interface TermsJsonAdapter {

  class SimpleSerializer extends JsonSerializer<Term> {
    @Override
    public void serialize(Term v, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeString(v != null ? v.getTag() : null);
    }
  }

  class UniversalSerializer extends JsonSerializer<Term> {
    @Override
    public void serialize(Term v, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeString(v != null ? v.getConceptUUID().toString() : null);
    }
  }


  class KeySerializer extends JsonSerializer<Term> {

    @Override
    public void serialize(Term v, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeFieldName(v.getTag());
    }
  }

  class Serializer extends JsonSerializer<Term> {

    @Override
    public void serialize(Term v, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeObject(v.asConcept());
    }
  }

  abstract class Deserializer extends JsonDeserializer<Term> {

    public static final Logger logger = LoggerFactory.getLogger(Deserializer.class);
    private static final String DEFAULT_KEY = "tag";

    @Override
    public Term deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException {
      TreeNode t = jp.readValueAsTree();
      if (t == null || t.isMissingNode()) {
        return null;
      }

      Optional<String> tagNode = t.isObject()
          ? getTag(t)
          : Optional.ofNullable(((TextNode) t).asText());
      if (!tagNode.isPresent()) {
        logger.warn("Unable to resolve concept {}", t);
      }

      Optional<Term> resolved = tagNode.flatMap(this::resolve);
      if (tagNode.isPresent() && !resolved.isPresent()) {
        logger.warn("Unable to resolve concept ID {}", tagNode.get());
      }

      Term resolvedTerm = resolved.orElse(null);
      if (resolvedTerm instanceof TermSeries<?>) {
        String versionTag = getVersionNode(t).orElse(null);
        if (versionTag != null) {
          resolvedTerm = ((TermSeries<?>) resolvedTerm).getVersion(versionTag).orElse(null);
        } else {
          resolvedTerm = ((TermSeries<?>) resolvedTerm).getLatest();
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

    protected abstract Term[] getValues();

    protected Optional<Term> resolve(String tag) {
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

  abstract class UUIDBasedDeserializer extends Deserializer {
    @Override
    protected Optional<Term> resolve(String tag) {
      return ensureUUID(tag)
          .flatMap(this::resolveUUID)
          .map(Term.class::cast);
    }

    protected abstract <U> Optional<U> resolveUUID(UUID uuid);

    protected String getKey() {
      return "conceptUUID";
    }
  }


}
