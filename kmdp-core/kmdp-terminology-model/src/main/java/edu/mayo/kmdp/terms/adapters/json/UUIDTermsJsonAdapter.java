package edu.mayo.kmdp.terms.adapters.json;

import static edu.mayo.kmdp.util.Util.ensureUUID;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.omg.spec.api4kp._20200801.id.Term;

public interface UUIDTermsJsonAdapter {

  class Serializer<T extends Term>
      extends AbstractTermsJsonAdapter.AbstractSerializer<T> {
    public Serializer() {
      // nothing to do
    }

    @Override
    public void serialize(Term v, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeString(v != null ? v.getUuid().toString() : null);
    }
  }

  abstract class Deserializer<T extends Term>
      extends AbstractTermsJsonAdapter.AbstractDeserializer<T> {

    @Override
    public T deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException {
      String uuid = jp.getText();
      if (uuid == null) {
        return null;
      }
      return ensureUUID(uuid)
          .flatMap(this::resolveUUID)
          .orElse(null);
    }

    @Override
    protected Optional<T> resolveUUID(UUID uuid) {
      return Arrays.stream(getValues())
          .filter(x -> uuid.equals(x.getUuid()))
          .findFirst();
    }

  }

  class KeySerializer<T extends Term>
      extends AbstractTermsJsonAdapter.AbstractKeySerializer<T> {
    @Override
    public void serialize(T v, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeFieldName(v.getUuid().toString());
    }
  }

  abstract class KeyDeserializer<T extends Term>
      extends AbstractTermsJsonAdapter.AbstractKeyDeserializer<T> {

    @Override
    public T deserializeKey(String key, DeserializationContext ctxt) throws IOException {
      return ensureUUID(key)
          .flatMap(this::resolveUUID)
          .orElse(null);
    }

    protected abstract Optional<T> resolveUUID(UUID uuid);
  }


}
