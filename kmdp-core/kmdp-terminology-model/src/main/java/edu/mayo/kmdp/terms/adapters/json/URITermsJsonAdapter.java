package edu.mayo.kmdp.terms.adapters.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import edu.mayo.kmdp.id.helper.DatatypeHelper;
import edu.mayo.kmdp.series.Series;
import edu.mayo.kmdp.series.Versionable;
import edu.mayo.kmdp.util.NameUtils;
import edu.mayo.kmdp.util.StreamUtil;
import edu.mayo.kmdp.util.URIUtil;
import edu.mayo.kmdp.util.Util;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import org.omg.spec.api4kp._1_0.id.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface URITermsJsonAdapter {

  Logger logger = LoggerFactory.getLogger(URITermsJsonAdapter.class);

  class Serializer<T extends Term>
      extends AbstractTermsJsonAdapter.AbstractSerializer<T> {
    protected Serializer() {
      // nothing to do
    }

    @Override
    public void serialize(Term v, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (v == null) {
        gen.writeNull();
        return;
      }
      Optional<String> uri = DatatypeHelper.encodeConcept(v);
      if(uri.isPresent()) {
        gen.writeString(uri.get());
      } else {
        gen.writeNull();
      }
    }
  }

  abstract class Deserializer<T extends Term>
      extends AbstractTermsJsonAdapter.AbstractDeserializer<T> {

    @Override
    public T deserialize(JsonParser jp, DeserializationContext ctxt) {
      try {
        String tok = jp.getText();
        return parse(tok);
      } catch (IOException e) {
        logger.error(e.getMessage(), e);
      }
      return null;
    }

    protected T parse(String tok) {
      return extractURI(tok)
          .flatMap(uri -> extractIdentifier(uri)
              .flatMap(Util::ensureUUID)
              .flatMap(this::resolveUUID)
              .map(t -> resolveInSeries(t, URIUtil.normalizeURI(uri))))
          .orElse(null);
    }

    @SuppressWarnings("unchecked")
    private T resolveInSeries(T t, URI uri) {
      if (t instanceof Series) {
        Series<? extends Versionable<?>> s = ((Series<? extends Versionable<?>>) t);
        Optional<?> opt = s.getVersions().stream()
            .flatMap(StreamUtil.filterAs(Term.class))
            .filter(v -> v.getNamespaceUri().equals(uri))
            .findAny();
        return opt.map(o -> (T) o).orElse(t);
      }
      return t;
    }

    protected Optional<URI> extractURI(String tok) {
      if (Util.isEmpty(tok)) {
        return Optional.empty();
      }

      if (tok.charAt(0) == '{') {
        int nsEnd = tok.indexOf('}');
        String nsURI = tok.substring(1,nsEnd).trim();
        tok = nsURI + tok.substring(nsEnd + 1).trim();
      }

      int delim = tok.indexOf('|');
      if (delim == 0) {
        return Optional.empty();
      }
      if (delim <= 0) {
        return Optional.of(URI.create(tok.trim()));
      }
      return Optional.of(URI.create(tok.substring(0, delim).trim()));
    }


    protected Optional<String> extractLabel(String tok) {
      if (Util.isEmpty(tok)) {
        return Optional.empty();
      }
      int delim = tok.indexOf('|');
      int endDelim = tok.lastIndexOf('|');
      if (delim < 0 || endDelim <= 0) {
        return Optional.empty();
      }
      return Optional.of(tok.substring(delim + 1, endDelim).trim());
    }

    protected Optional<String> extractIdentifier(URI uri) {
      String fragment = uri.getFragment();
      if (Util.isEmpty(fragment)) {
        if ("urn".equals(uri.getScheme())) {
          fragment = uri.toString();
          fragment = fragment.substring(fragment.lastIndexOf(':') + 1);
        } else {
          fragment = NameUtils.getTrailingPart(uri.toString());
        }
      }
      return Optional.ofNullable(fragment);
    }

    @Override
    protected abstract Optional<T> resolveUUID(UUID uuid);
  }


}
