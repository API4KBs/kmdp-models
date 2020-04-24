package edu.mayo.kmdp;

import static edu.mayo.kmdp.util.Util.uuid;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.TXT;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.HTML;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;
import static org.omg.spec.api4kp._1_0.id.IdentifierConstants.VERSION_LATEST;
import static org.omg.spec.api4kp._1_0.id.IdentifierConstants.VERSION_ZERO;
import static org.omg.spec.api4kp._1_0.id.SemanticIdentifier.hashIdentifiers;
import static org.omg.spec.api4kp._1_0.id.SemanticIdentifier.newId;

import edu.mayo.kmdp.util.StreamUtil;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.id.ResourceIdentifier;
import org.omg.spec.api4kp._1_0.services.CompositeKnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.CompositeStructType;
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


  @Test
  void testConstructSetComposite() {
    CompositeKnowledgeCarrier ckc = (CompositeKnowledgeCarrier) AbstractCarrier.ofIdentifiableSet(
        rep(HTML, TXT, Charset.defaultCharset()),
        s -> newId(uuid(s),VERSION_ZERO),
        s -> newId(uuid(s.toLowerCase()),VERSION_ZERO),
        Arrays.asList("A","B","C"));

    assertEquals(3, ckc.getComponent().size());
    assertNotNull(ckc.getStruct());

    assertEquals(ckc.getAssetId(), ckc.getStruct().getAssetId());
    assertNotEquals(ckc.getAssetId(),ckc.getArtifactId());
    assertNotEquals(ckc.getStruct().getAssetId(),ckc.getStruct().getArtifactId());

    assertNull(ckc.getArtifactId());
    assertNotEquals(ckc.getArtifactId(),ckc.getStruct().getArtifactId());

    assertTrue(ckc.getComponent().stream().noneMatch(kc -> kc.getAssetId().sameAs(ckc.getAssetId())));
    assertTrue(ckc.getComponent().stream().noneMatch(kc -> kc.getArtifactId().sameAs(ckc.getArtifactId())));

    ResourceIdentifier aid1 = newId(uuid("A"),VERSION_ZERO);
    ResourceIdentifier aid2 = newId(uuid("B"),VERSION_ZERO);
    ResourceIdentifier aid3 = newId(uuid("C"),VERSION_ZERO);
    ResourceIdentifier aidSet = newId(uuid(CompositeStructType.SET), VERSION_LATEST);
    ResourceIdentifier compositeAssetId = hashIdentifiers(hashIdentifiers(hashIdentifiers(aid2,aid1),aid3),aidSet);
    assertEquals(compositeAssetId,ckc.getAssetId());
  }
}
