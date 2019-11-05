package edu.mayo.kmdp.terms.adapters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.node.TextNode;
import edu.mayo.kmdp.id.Term;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Class that supports both the 'extended' and 'compact' serializations of Terms
 * While the latter is preferred, the former is maintained for backward compatibility/generality
 */
public abstract class LegacyURITermsJsonAdapter extends URITermsJsonAdapter {

  private static Logger logger = LoggerFactory.getLogger(LegacyURITermsJsonAdapter.class);

  protected LegacyURITermsJsonAdapter() {
    // nothing to do
  }

  public static class Serializer<T extends Term> extends URITermsJsonAdapter.Serializer<T> {
    protected Serializer() {
      // nothing to do
    }
  }

  public abstract static class Deserializer<T extends Term> extends URITermsJsonAdapter.Deserializer<T> {
    protected Deserializer() {}

    @Override
    public T deserialize(JsonParser jp, DeserializationContext ctxt) {
      try {
        TreeNode t = jp.readValueAsTree();
        if (t.isMissingNode()) {
          return null;
        } else if (t.isObject()) {
          T[] vals = getValues();
          return (new ConceptTermsJsonAdapter.Deserializer<T>() {
            @Override
            protected T[] getValues() {
              return vals;
            }
          }).parse(t);
        } else {
          return parse(((TextNode) t).asText());
        }
      } catch (IOException e) {
        logger.error(e.getMessage(),e);
        return null;
      }
    }
  }


}
