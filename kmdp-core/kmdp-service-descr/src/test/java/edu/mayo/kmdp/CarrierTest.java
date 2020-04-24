package edu.mayo.kmdp;

import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.TXT;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.HTML;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.OWL_2;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import edu.mayo.kmdp.util.JenaUtil;
import edu.mayo.kmdp.util.XMLUtil;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.services.CompositeKnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

public class CarrierTest {

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


  @Test
  void testSetOrientedCompositeCarrrier() {
    SyntacticRepresentation rep = rep(HTML,TXT, Charset.defaultCharset());
    KnowledgeCarrier kc = AbstractCarrier.ofSet(
        rep,
        Arrays.asList("a", "b", "c"));

    assertTrue(kc instanceof CompositeKnowledgeCarrier);
    CompositeKnowledgeCarrier ckc = (CompositeKnowledgeCarrier) kc;

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

    assertTrue(ckc.getStruct().getRepresentation().getLanguage().sameAs(OWL_2));
    Model m = ModelFactory.createDefaultModel()
        .read(new ByteArrayInputStream(ckc.getStruct().asString().orElse("").getBytes()),
            null,"TTL");
    assertEquals(3,JenaUtil.sizeOf(m));
  }


}

