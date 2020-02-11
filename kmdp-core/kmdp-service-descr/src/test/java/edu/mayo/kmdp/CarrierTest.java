package edu.mayo.kmdp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import edu.mayo.kmdp.util.XMLUtil;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;

public class CarrierTest {

  @Test
  void testCarrierAs() {
    KnowledgeCarrier kc = AbstractCarrier.ofAst(42);
    assertTrue(kc.as(Integer.class).isPresent());
    assertFalse(kc.as(String.class).isPresent());
  }

  @Test
  void testASTCarrierAsString() {
    KnowledgeCarrier kc = AbstractCarrier.ofAst(42);
    assertFalse(kc.asString().isPresent());
  }

  @Test
  void testDoxCarrierAsString() {
    KnowledgeCarrier kc = AbstractCarrier.of(XMLUtil.emptyDocument());
    assertEquals("",kc.asString().orElse("FAILED"));
  }

  @Test
  void testJxCarrierAsString() {
    KnowledgeCarrier kc = AbstractCarrier.of(JsonNodeFactory.instance.textNode("foo"));
    assertEquals("\"foo\"",kc.asString().orElse("FAILED"));
  }

  @Test
  void testExprCarrierAsString() {
    KnowledgeCarrier kc = AbstractCarrier.of("bar");
    assertEquals("bar",kc.asString().orElse("FAILED"));
  }

  @Test
  void testExprCarrierAsStringBytes() {
    KnowledgeCarrier kc = AbstractCarrier.of("bar".getBytes());
    assertEquals("bar",kc.asString().orElse("FAILED"));
  }



}
