package edu.mayo.kmdp.surrogate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.mayo.kmdp.SurrogateHelper;
import edu.mayo.kmdp.series.Series;
import edu.mayo.kmdp.terms.adapters.json.AbstractTermsJsonAdapter;
import edu.mayo.kmdp.util.JSonUtil;
import java.io.IOException;
import java.util.Optional;
import org.omg.spec.api4kp._1_0.id.Term;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;

public class LegacyTermsJsonAdapter {

  protected LegacyTermsJsonAdapter() {
    // don't instantiate
  }

  public static class Serializer
      extends AbstractTermsJsonAdapter.AbstractSerializer<Term> {
    @Override
    public void serialize(Term v, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (v instanceof Series) {
        v = (Term) ((Series) v).getLatest();
      }
      gen.writeObject(SurrogateHelper.toLegacyConceptIdentifier(v));
    }

    @Override
    public void serializeWithType(Term value, JsonGenerator gen, SerializerProvider serializers,
        TypeSerializer typeSer) throws IOException {
      serialize(value,gen,serializers);
    }
  }


  public static class Deserializer
      extends AbstractTermsJsonAdapter.AbstractDeserializer<Term> {

    @Override
    protected Optional<Term> resolveAsKnownTerm(TreeNode t, String tagNode) {
      return resolveWithNamespace(t)
          .flatMap(resTerm -> resolveVersion(resTerm, t));
    }

    protected ConceptIdentifier parseAsLegacyConceptIdentifier(TreeNode t) {
      if (t.get("tags") != null) {
        ((ObjectNode) t).remove("tags");
      }
      return JSonUtil.parseJson(((ObjectNode) t), ConceptIdentifier.class)
          .orElse(null);
    }

    private Optional<? extends Term> resolveWithNamespace(TreeNode t) {
      ConceptIdentifier ci = parseAsLegacyConceptIdentifier(t);
      if (ci == null) {
        throw new IllegalStateException("Controlled vocabularies should be serialized"
            + " as ConceptIdentifiers in this version");
      }

      return LegacyTermNamespaceMap
          .resolveKnownConceptByNamespace(ci.getConceptId(), ci.getNamespace().getId(),
              ci.getNamespace().getVersion());
    }

    @Override
    protected Term[] getValues() {
      throw new UnsupportedOperationException();
    }
  }

}
