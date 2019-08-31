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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.TextNode;
import edu.mayo.kmdp.id.Term;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
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

    public static final Logger logger = LoggerFactory.getLogger(TermsJsonAdapter.class);

    @Override
    public Term deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException {
      TreeNode t = jp.readValueAsTree();

      Optional<String> tagNode = getTagNode(t);
      if (!tagNode.isPresent()) {
        logger.warn("Unable to resolve concept {}", t);
      }

      Optional<Term> resolved = Arrays.stream(getValues())
            .filter(trm -> trm.getTag().equals(tagNode.orElse("")))
            .findAny();
      if (tagNode.isPresent() && !resolved.isPresent()) {
        logger.warn("Unable to resolve concept ID {}", tagNode.get());
      }
      return resolved.orElse(null);
    }

    private Optional<String> getTagNode(TreeNode t) {
      if (t.isObject()) {
        TreeNode nsNode = t.get("namespace");
        if (nsNode == null) {
          return Optional.empty();
        }
        TextNode tagNode = (TextNode) t.get("tag");
        TextNode nsIdNode = (TextNode) nsNode.get("@id");
        if (tagNode == null || nsIdNode == null) {
          return Optional.empty();
        }
        return Optional.of(tagNode)
            .map(TextNode::asText);
      } else if (t instanceof TextNode) {
        return Optional.of((TextNode) t)
            .map(TextNode::asText);
      } else {
        return Optional.empty();
      }
    }

    protected abstract Term[] getValues();
  }
}
