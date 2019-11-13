package edu.mayo.kmdp.terms.adapters.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import edu.mayo.kmdp.id.Term;
import java.io.IOException;

public abstract class ConceptTermsJsonAdapter extends AbstractTermsJsonAdapter {

  protected ConceptTermsJsonAdapter() {
    // nothing to do
  }

  public static class Serializer<T extends Term> extends AbstractJsonSerializer<T> {
    // default behavior
    public Serializer() {
      // nothing to do
    }

    public void serializeWithType(T value, JsonGenerator gen, SerializerProvider serializers,
        TypeSerializer typeSer) throws IOException {
      serialize(value,gen,serializers);
    }

  }

  public abstract static class Deserializer<T extends Term> extends AbstractJsonDeserializer<T> {
    // default behavior
    public Deserializer() {
      // nothing to do
    }
  }
}
