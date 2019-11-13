package edu.mayo.kmdp.terms.adapters.json;

import static edu.mayo.kmdp.util.Util.ensureUUID;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import edu.mayo.kmdp.id.Term;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public abstract class UUIDTermsJsonAdapter extends AbstractTermsJsonAdapter {

  protected UUIDTermsJsonAdapter() {
    // nothing to do
  }

  public static class Serializer<T extends Term> extends AbstractJsonSerializer<T> {
    public Serializer() {
      // nothing to do
    }

    @Override
    public void serialize(Term v, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeString(v != null ? v.getConceptUUID().toString() : null);
    }
  }

  public abstract static class Deserializer<T extends Term> extends AbstractJsonDeserializer<T> {
    public Deserializer() {
      // nothing to do
    }

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
    protected abstract Optional<T> resolveUUID(UUID uuid);

  }

  public static class KeySerializer<T extends Term> extends AbstractTermsJsonAdapter.AbstractKeySerializer<T> {
    @Override
    public void serialize(T v, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeFieldName(v.getConceptUUID().toString());
    }
  }

  public abstract static class KeyDeserializer<T extends Term> extends AbstractTermsJsonAdapter.AbstractKeyDeserializer<T> {

    @Override
    public T deserializeKey(String key, DeserializationContext ctxt) throws IOException {
      return ensureUUID(key)
          .flatMap(this::resolveUUID)
          .orElse(null);
    }

    protected abstract Optional<T> resolveUUID(UUID uuid);
  }


}
