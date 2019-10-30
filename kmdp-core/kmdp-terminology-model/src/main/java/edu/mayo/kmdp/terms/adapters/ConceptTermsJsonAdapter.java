package edu.mayo.kmdp.terms.adapters;

import edu.mayo.kmdp.id.Term;

public abstract class ConceptTermsJsonAdapter extends AbstractTermsJsonAdapter {

  protected ConceptTermsJsonAdapter() {
    // nothing to do
  }

  public static class Serializer<T extends Term> extends AbstractJsonSerializer<T> {
    // default behavior
    public Serializer() {
      // nothing to do
    }
  }

  public abstract static class Deserializer<T extends Term> extends AbstractJsonDeserializer<T> {
    // default behavior
    public Deserializer() {
      // nothing to do
    }
  }
}
