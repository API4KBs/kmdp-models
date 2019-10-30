package edu.mayo.kmdp.terms.adapters;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.util.JSonUtil;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class JsonAdaptersTest {

  @Test
  public void testConceptAdapter() {

    Bean b = new Bean();

    Optional<String> json = JSonUtil.writeJsonAsString(b);
    assertTrue(json.isPresent());

    Bean b2 = json
        .flatMap(s -> JSonUtil.parseJson(s,Bean.class))
        .orElse(null);

    assertNotNull(b2);

    assertSame(Colors.RED, b2.col1);
    assertSame(ColorsSeries.GREEN.getLatest(), b2.col2);
    assertSame(Colors.BLUE, b2.col3);
  }

}
