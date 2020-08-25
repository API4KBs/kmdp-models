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

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.TermsHelper;
import edu.mayo.kmdp.util.JenaUtil;
import edu.mayo.kmdp.util.StreamUtil;
import edu.mayo.ontology.taxonomies.kao.rel.structuralreltype.StructuralPartTypeSeries;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.id.Link;
import org.omg.spec.api4kp._1_0.id.ResourceIdentifier;
import org.omg.spec.api4kp._1_0.id.SemanticIdentifier;
import org.omg.spec.api4kp._1_0.services.CompositeKnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.CompositeStructType;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;

public class CompositeTest {

  @Test
  void testCompositeMapping() {
    KnowledgeCarrier ckc =
        new CompositeKnowledgeCarrier()
            .withComponent(AbstractCarrier.of("Foo"))
            .withComponent(AbstractCarrier.of("Bar"));

    Answer<KnowledgeCarrier> out = Answer.of(ckc).flatMap(this::upCase);

    assertTrue(out.isSuccess());

    KnowledgeCarrier result = out.get();
    assertTrue(result instanceof CompositeKnowledgeCarrier);

    CompositeKnowledgeCarrier ckc2 = (CompositeKnowledgeCarrier) result;
    Set<String> strs =
        ckc2.getComponent().stream()
            .map(KnowledgeCarrier::asString)
            .flatMap(StreamUtil::trimStream)
            .collect(Collectors.toSet());
    assertEquals(new HashSet<>(Arrays.asList("FOO", "BAR")), strs);
  }

  private Answer<KnowledgeCarrier> upCase(KnowledgeCarrier carrier) {
    String src = carrier.asString().orElse("");
    return Answer.of(AbstractCarrier.of(src.toUpperCase()));
  }

  @Test
  void testConstructSetComposite() {
    CompositeKnowledgeCarrier ckc =
        (CompositeKnowledgeCarrier)
            AbstractCarrier.ofIdentifiableSet(
                rep(HTML, TXT, Charset.defaultCharset()),
                s -> newId(uuid(s), VERSION_ZERO),
                s -> newId(uuid(s.toLowerCase()), VERSION_ZERO),
                Arrays.asList("A", "B", "C"));

    assertEquals(3, ckc.getComponent().size());
    assertNotNull(ckc.getStruct());

    assertEquals(ckc.getAssetId(), ckc.getStruct().getAssetId());
    assertNotEquals(ckc.getAssetId(), ckc.getArtifactId());
    assertNotEquals(ckc.getStruct().getAssetId(), ckc.getStruct().getArtifactId());

    assertNull(ckc.getArtifactId());
    assertNotEquals(ckc.getArtifactId(), ckc.getStruct().getArtifactId());

    assertTrue(
        ckc.getComponent().stream().noneMatch(kc -> kc.getAssetId().sameAs(ckc.getAssetId())));
    assertTrue(
        ckc.getComponent().stream()
            .noneMatch(kc -> kc.getArtifactId().sameAs(ckc.getArtifactId())));

    ResourceIdentifier aid1 = newId(uuid("A"), VERSION_ZERO);
    ResourceIdentifier aid2 = newId(uuid("B"), VERSION_ZERO);
    ResourceIdentifier aid3 = newId(uuid("C"), VERSION_ZERO);
    ResourceIdentifier aidSet = newId(uuid(CompositeStructType.TREE), VERSION_LATEST);
    ResourceIdentifier compositeAssetId =
        hashIdentifiers(hashIdentifiers(hashIdentifiers(aid2, aid1), aid3), aidSet);
    assertEquals(compositeAssetId, ckc.getAssetId());
  }

  @Test
  void testConstructSetComposite_Tree() throws IOException {
    List<String> artifacts = Arrays.asList("A", "B", "C");
    Map<SemanticIdentifier, String> artifactsMap = new HashMap<>();
    artifacts.stream()
        .forEach(artifact -> artifactsMap.put(newId(uuid(artifact), VERSION_ZERO), artifact));
    MockLink mockLinkB = new MockLink("B");
    MockLink mockLinkC = new MockLink("C");

    CompositeKnowledgeCarrier ckc =
        (CompositeKnowledgeCarrier)
            AbstractCarrier.ofIdentifiableTree(
                rep(HTML, TXT, Charset.defaultCharset()),
                s -> newId(uuid(s), VERSION_ZERO),
                s -> newId(uuid(s), VERSION_ZERO),
                s -> {
                  switch (s) {
                    case "A":
                      return Arrays.asList(mockLinkB, mockLinkC);
                    case "B":
                    case "C":
                    default:
                      return Collections.emptyList();
                  }
                },
                newId(uuid("A"), VERSION_ZERO),
                artifactsMap);

    assertEquals(3, ckc.getComponent().size());

    List<Statement> hasStructuralStatements =
        artifacts.stream()
            .map(
                artifact ->
                    JenaUtil.objA(
                        ckc.getAssetId().getVersionId().toString(),
                        StructuralPartTypeSeries.Has_Structural_Component.getRef().toString(),
                        newId(uuid(artifact), VERSION_ZERO).getVersionId().toString()))
            .collect(Collectors.toList());
    hasStructuralStatements.add(
        JenaUtil.objA(
            newId(uuid("A"), VERSION_ZERO).getVersionId().toString(),
            mockLinkB.getRel().getConceptId().toString(),
            mockLinkB.getHrefVersionURI().toString()));
    hasStructuralStatements.add(
        JenaUtil.objA(
            newId(uuid("A"), VERSION_ZERO).getVersionId().toString(),
            mockLinkC.getRel().getConceptId().toString(),
            mockLinkC.getHrefVersionURI().toString()));

    String actualStruct = ckc.getStruct().getExpression().toString();
    Model actualModel =
        ModelFactory.createDefaultModel()
            .read(IOUtils.toInputStream(actualStruct, "UTF-8"), null, "TURTLE");

    Model expectedModel = ModelFactory.createDefaultModel().add(hasStructuralStatements);
    assertTrue(actualModel.isIsomorphicWith(expectedModel));

    assertEquals(ckc.getAssetId(), ckc.getStruct().getAssetId());
    assertNotEquals(ckc.getAssetId(), ckc.getArtifactId());
    assertNotEquals(ckc.getStruct().getAssetId(), ckc.getStruct().getArtifactId());

    assertNull(ckc.getArtifactId());
    assertNotEquals(ckc.getArtifactId(), ckc.getStruct().getArtifactId());

    assertTrue(
        ckc.getComponent().stream().noneMatch(kc -> kc.getAssetId().sameAs(ckc.getAssetId())));
    assertTrue(
        ckc.getComponent().stream()
            .noneMatch(kc -> kc.getArtifactId().sameAs(ckc.getArtifactId())));
  }

  static class MockLink implements Link {

    String tgt;

    URI uri;

    public MockLink(String tgt) {
      this.tgt = tgt;
    }

    @Override
    public SemanticIdentifier getHref() {
      return SemanticIdentifier.newId(uuid(tgt), VERSION_ZERO);
    }

    @Override
    public Term getRel() {
      return TermsHelper.mayo("hasChild", "hasChild");
    }
  }
}
