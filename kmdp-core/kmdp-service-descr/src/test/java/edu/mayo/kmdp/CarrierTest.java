package edu.mayo.kmdp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp._20200801.AbstractCompositeCarrier.ofUniformAggregate;
import static org.omg.spec.api4kp._20200801.id.SemanticIdentifier.randomId;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.TXT;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.HTML;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import edu.mayo.kmdp.util.XMLUtil;
import java.nio.charset.Charset;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.AbstractCarrier;
import org.omg.spec.api4kp._20200801.services.CompositeKnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;

class CarrierTest {

  @Test
  void testCarrierAs() {
    KnowledgeCarrier kc = AbstractCarrier.ofAst(42);
    assertTrue(kc.as(Integer.class).isPresent());
    assertTrue(kc.is(Integer.class));
    assertFalse(kc.as(String.class).isPresent());
    assertFalse(kc.is(String.class));
  }

  @Test
  void testCarrierAsWithNull() {
    KnowledgeCarrier kc = AbstractCarrier.ofAst(null);
    assertFalse(kc.as(Integer.class).isPresent());
    assertFalse(kc.is(String.class));
  }

  @Test
  void testKnowledgeCarrierAsString() {
    KnowledgeCarrier kc = AbstractCarrier.ofAst(42);
    assertEquals("42", kc.asString().orElse(""));
  }

  @Test
  void testDoxCarrierAsString() {
    KnowledgeCarrier kc = AbstractCarrier.of(XMLUtil.emptyDocument());
    assertEquals("", kc.asString().orElse("FAILED"));
  }

  @Test
  void testJxCarrierAsString() {
    KnowledgeCarrier kc = AbstractCarrier.of(JsonNodeFactory.instance.textNode("foo"));
    assertEquals("\"foo\"", kc.asString().orElse("FAILED"));
  }

  @Test
  void testExprCarrierAsString() {
    KnowledgeCarrier kc = AbstractCarrier.of("bar");
    assertEquals("bar", kc.asString().orElse("FAILED"));
  }

  @Test
  void testExprCarrierAsStringBytes() {
    KnowledgeCarrier kc = AbstractCarrier.of("bar".getBytes());
    assertEquals("bar", kc.asString().orElse("FAILED"));
  }


  @Test
  void testSetOrientedAggregateCarrrier() {
    SyntacticRepresentation rep = rep(HTML, TXT, Charset.defaultCharset());
    CompositeKnowledgeCarrier ckc = ofUniformAggregate(
        Arrays.asList("a", "b", "c"),
        rep,
        s -> randomId(),
        s -> randomId(),
        s -> s);

    assertNotNull(ckc);
    assertNotNull(ckc.getAssetId());

    assertEquals(3, ckc.getComponent().size());
    assertTrue(ckc.getComponent().stream().allMatch(
        comp -> comp.getRepresentation().equals(rep)
    ));
    assertTrue(ckc.getComponent().stream().allMatch(
        comp -> {
          String s = comp.asString().orElse("");
          return "a".equals(s) || "b".equals(s) || "c".equals(s);
        }
    ));

    assertNull(ckc.getStruct());
  }


}

