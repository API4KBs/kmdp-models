package edu.mayo.kmdp.terms.adapters;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.mayo.kmdp.terms.adapters.json.ConceptTermsJsonAdapter;
import edu.mayo.kmdp.terms.adapters.json.URITermsJsonAdapter;
import edu.mayo.kmdp.terms.adapters.json.UUIDTermsJsonAdapter;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class Bean {

  @JsonSerialize(using = TrmSerializer.class)
  @JsonDeserialize(using = TrmDeserializer.class)
  public IColors col1 = ColorsSeries.RED;

  @JsonSerialize(using = URISerializer.class)
  @JsonDeserialize(using = URIDeserializer.class)
  public IColors col2 = Colors.GREEN;

  @JsonSerialize(using = UUIDSerializer.class)
  @JsonDeserialize(using = UUIDDeserializer.class)
  public IColors col3 = Colors.BLUE;


  public static class TrmSerializer extends ConceptTermsJsonAdapter.Serializer<IColors> {

  }

  public static class TrmDeserializer extends ConceptTermsJsonAdapter.Deserializer<IColors> {

    @Override
    protected IColors[] getValues() {
      return ColorsSeries.values();
    }
  }

  public static class UUIDSerializer extends UUIDTermsJsonAdapter.Serializer<IColors> {

  }

  public static class UUIDDeserializer extends UUIDTermsJsonAdapter.Deserializer<IColors> {
    @Override
    protected IColors[] getValues() {
      return ColorsSeries.values();
    }

    @Override
    protected Optional<IColors> resolveUUID(UUID uuid) {
      return Colors.resolveUUID(uuid)
          .map(IColors.class::cast);
    }
  }

  public static class URISerializer extends URITermsJsonAdapter.Serializer<IColors> {

  }

  public static class URIDeserializer extends URITermsJsonAdapter.Deserializer<IColors> {

    @Override
    protected IColors[] getValues() {
      return ColorsSeries.values();
    }

    @Override
    protected Optional<IColors> resolveUUID(UUID uuid) {
      return Arrays.stream(Colors.values())
          .filter(c -> c.getConceptUUID().equals(uuid))
          .map(IColors.class::cast)
          .findAny();
    }
  }


}
