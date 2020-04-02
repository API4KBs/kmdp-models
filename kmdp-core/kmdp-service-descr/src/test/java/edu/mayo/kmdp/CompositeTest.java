package edu.mayo.kmdp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.util.StreamUtil;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.services.CompositeKnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;

public class CompositeTest {

  @Test
  void testCompositeMapping() {
    KnowledgeCarrier ckc = new CompositeKnowledgeCarrier()
        .withComponent(AbstractCarrier.of("Foo"))
        .withComponent(AbstractCarrier.of("Bar"));

    Answer<KnowledgeCarrier> out = Answer.of(ckc)
        .flatMap(this::upCase);

    assertTrue(out.isSuccess());

    KnowledgeCarrier result = out.get();
    assertTrue(result instanceof CompositeKnowledgeCarrier);

    CompositeKnowledgeCarrier ckc2 = (CompositeKnowledgeCarrier) result;
    Set<String> strs = ckc2.getComponent().stream()
        .map(KnowledgeCarrier::asString)
        .flatMap(StreamUtil::trimStream)
        .collect(Collectors.toSet());
    assertEquals(new HashSet<>(Arrays.asList("FOO","BAR")), strs);
  }

  private Answer<KnowledgeCarrier> upCase(KnowledgeCarrier carrier) {
    String src = carrier.asString().orElse("");
    return Answer.of(AbstractCarrier.of(src.toUpperCase()));
  }

}
