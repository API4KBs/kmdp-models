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
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.TextNode;
import edu.mayo.kmdp.terms.adapters.json.AbstractTermsJsonAdapter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.omg.spec.api4kp._20200801.id.Term;

public abstract class MockTermsJsonAdapter {

  public static class Serializer<T extends Term> extends AbstractTermsJsonAdapter.AbstractSerializer<T> {
    @Override
    public void serialize(T v, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeObject(v.asConceptIdentifier());
    }
  }

  public abstract static class Deserializer<T extends Term> extends AbstractTermsJsonAdapter.AbstractDeserializer<T> {

    @Override
    public T deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException {
      TreeNode t = jp.readValueAsTree();
      TreeNode nsNode = t.get("namespace");
      if ( nsNode == null ) {
        return null;
      }
      TextNode tagNode = (TextNode) t.get("tag");
      TextNode nsIdNode = (TextNode) nsNode.get("@id");
      if ( tagNode == null || nsIdNode == null ) {
        return null;
      }
      return Arrays.stream(getValues())
          .filter((trm) -> trm.getTag().equals(tagNode.asText()))
          .findAny()
          .orElse(null);
    }

    @Override
    protected abstract Optional<T> resolveUUID(UUID uuid);

    protected abstract T[] getValues();

  }

}
