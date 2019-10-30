package edu.mayo.kmdp.terms.adapters;

import static edu.mayo.kmdp.util.Util.ensureUUID;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.series.Versionable;
import edu.mayo.kmdp.terms.TermSeries;
import edu.mayo.kmdp.util.NameUtils;
import edu.mayo.kmdp.util.URIUtil;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;

public abstract class URITermsJsonAdapter extends AbstractTermsJsonAdapter {

  protected URITermsJsonAdapter() {
    // nothing to do
  }

  public static class Serializer<T extends Term> extends AbstractJsonSerializer<T> {
    protected Serializer() {
      // nothing to do
    }

    @Override
    public void serialize(Term v, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (v == null) {
        return;
      }
      gen.writeString(String.format("%s#%s | %s |",
          ((NamespaceIdentifier)v.getNamespace()).getId(),
          v.getConceptUUID().toString(),
          v.getLabel()));
    }
  }

  public abstract static class Deserializer<T extends Term> extends AbstractJsonDeserializer<T> {
    protected Deserializer() {}

    @Override
    public T deserialize(JsonParser jp, DeserializationContext ctxt) {
      try {
        String tok = jp.getText();
        if (tok == null) {
          return null;
        }
        URI uri = URI.create(tok.substring(0,tok.indexOf('|')).trim());
        String uuid = extractIdentifier(uri);
        return ensureUUID(uuid)
            .flatMap(this::resolveUUID)
            .map(t -> resolveInSeries(t, URIUtil.normalizeURI(uri)))
            .orElse(null);
      } catch (IOException e) {
        logger.error(e.getMessage(),e);
      }
      return null;
    }

    private T resolveInSeries(T t, URI uri) {
      if (t instanceof TermSeries) {
        TermSeries<? extends Versionable,?> s = ((TermSeries) t);
        Optional<?> opt = s.getVersions().stream()
            .filter(v -> ((NamespaceIdentifier) v.getNamespace()).getId().equals(uri))
            .findAny();
        return opt.map(o -> (T) o).orElse(t);
      }
      return t;
    }

    protected String extractIdentifier(URI uri) {
      String fragment = uri.getFragment();
      if (fragment == null) {
        if ("urn".equals(uri.getScheme())) {
          fragment = uri.toString();
          fragment = fragment.substring(fragment.lastIndexOf(':') + 1);
        } else {
          fragment = NameUtils.getTrailingPart(uri.toString());
        }
      }
      return fragment;
    }

    protected abstract Optional<T> resolveUUID(UUID uuid);
  }


}
