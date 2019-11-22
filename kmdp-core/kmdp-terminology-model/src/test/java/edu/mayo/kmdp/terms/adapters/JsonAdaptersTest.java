package edu.mayo.kmdp.terms.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.adapters.json.GenericURITermsJsonAdapter;
import edu.mayo.kmdp.util.JSonUtil;
import java.net.URI;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;

public class JsonAdaptersTest {

  @Test
  public void testConceptAdapter() {

    Bean b = new Bean();

    Optional<String> json = JSonUtil.writeJsonAsString(b);
    assertTrue(json.isPresent());

    System.out.println(json.get());

    Bean b2 = json
        .flatMap(s -> JSonUtil.parseJson(s,Bean.class))
        .orElse(null);

    assertNotNull(b2);

    assertSame(Colors.RED, b2.col1);
    assertSame(ColorsSeries.GREEN.getLatest(), b2.col2);
    assertSame(Colors.BLUE, b2.col3);
  }



  @Test
  public void testRoundTripWithMixedNamespaces() {
    ConceptIdentifier c1 = new ConceptIdentifier()
        .withConceptId(URI.create("http://foo.bar#bar"))
        .withTag("bar")
        .withNamespace(new NamespaceIdentifier()
            .withId(URI.create("http://baz.com")));
    Foo f = new Foo(c1);

    Optional<String> s = JSonUtil.writeJsonAsString(f);
    assertTrue(s.isPresent());
    System.out.println(s);
    assertTrue(s.orElse("").contains("{http://baz.com} http://foo.bar#bar"));

    Foo f2 = JSonUtil.parseJson(s.get(), Foo.class).orElse(null);
    assertNotNull(f2);
    assertEquals(c1.getConceptId(), f2.t.getConceptId());
    assertEquals(c1.getConceptUUID(), f2.t.getConceptUUID());
    assertEquals(c1.getNamespace().getId(),((NamespaceIdentifier)f2.t.getNamespace()).getId());

    c1.setConceptId(URI.create("http://baz.com#bar"));
    Optional<String> s2 = JSonUtil.writeJsonAsString(f);
    assertTrue(s2.isPresent());
    System.out.println(s2);
    assertTrue(s2.orElse("").contains("http://baz.com#bar"));

    Foo f3 = JSonUtil.parseJson(s2.get(), Foo.class).orElse(null);
    assertNotNull(f3);
    assertEquals(c1.getConceptId(), f3.t.getConceptId());
    assertEquals(c1.getConceptUUID(), f3.t.getConceptUUID());
    assertEquals(c1.getNamespace().getId(),((NamespaceIdentifier)f3.t.getNamespace()).getId());
  }

  public static class Foo {
    @JsonSerialize(using = GenericURITermsJsonAdapter.GenericURISerializer.class)
    @JsonDeserialize(using = GenericURITermsJsonAdapter.GenericURIDeserializer.class)
    private Term t;

    public Foo() {
      // support newInstance()
    }

    public Foo(Term t) {
      this.t = t;
    }
  }


}
