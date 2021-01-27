package edu.mayo.kmdp;

import static edu.mayo.kmdp.registry.Registry.KNOWLEDGE_ASSET_URI;
import static edu.mayo.kmdp.util.JenaUtil.objA;
import static edu.mayo.kmdp.util.Util.uuid;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.of;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp._20200801.AbstractCompositeCarrier.inferSetStruct;
import static org.omg.spec.api4kp._20200801.AbstractCompositeCarrier.inferStruct;
import static org.omg.spec.api4kp._20200801.AbstractCompositeCarrier.ofMixedAggregate;
import static org.omg.spec.api4kp._20200801.AbstractCompositeCarrier.ofMixedAnonymousComposite;
import static org.omg.spec.api4kp._20200801.AbstractCompositeCarrier.ofUniformAggregate;
import static org.omg.spec.api4kp._20200801.AbstractCompositeCarrier.ofUniformAnonymousComposite;
import static org.omg.spec.api4kp._20200801.AbstractCompositeCarrier.ofUniformNamedComposite;
import static org.omg.spec.api4kp._20200801.id.IdentifierConstants.VERSION_LATEST;
import static org.omg.spec.api4kp._20200801.id.IdentifierConstants.VERSION_ZERO;
import static org.omg.spec.api4kp._20200801.id.SemanticIdentifier.hashIdentifiers;
import static org.omg.spec.api4kp._20200801.id.SemanticIdentifier.newId;
import static org.omg.spec.api4kp._20200801.id.SemanticIdentifier.randomId;
import static org.omg.spec.api4kp._20200801.services.CompositeStructType.GRAPH;
import static org.omg.spec.api4kp._20200801.services.CompositeStructType.SET;
import static org.omg.spec.api4kp._20200801.services.CompositeStructType.TREE;
import static org.omg.spec.api4kp._20200801.taxonomy.dependencyreltype.DependencyTypeSeries.Depends_On;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.TXT;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.XML_1_1;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_1;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.HTML;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Serialized_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.structuralreltype.StructuralPartTypeSeries.Has_Structural_Component;
import static org.omg.spec.api4kp._20200801.taxonomy.structuralreltype.StructuralPartTypeSeries.Has_Structuring_Component;

import edu.mayo.kmdp.terms.TermsHelper;
import edu.mayo.kmdp.util.StreamUtil;
import java.io.ByteArrayInputStream;
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
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.AbstractCarrier;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.id.Link;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.services.CompositeKnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.CompositeStructType;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;

class CompositeTest {

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
    ResourceIdentifier aid1 = newId(uuid("A"), VERSION_ZERO);
    ResourceIdentifier aid2 = newId(uuid("B"), VERSION_ZERO);
    ResourceIdentifier aid3 = newId(uuid("C"), VERSION_ZERO);
    ResourceIdentifier aidSet = newId(uuid(CompositeStructType.TREE), VERSION_LATEST);
    ResourceIdentifier compositeAssetId =
        hashIdentifiers(hashIdentifiers(hashIdentifiers(aid2, aid1), aid3), aidSet);

    List<String> artifacts = Arrays.asList("A", "B", "C");

    CompositeKnowledgeCarrier ckc =
            ofUniformNamedComposite(
                compositeAssetId,
                null,
                null,
                "mock name",
                SET,
                inferSetStruct(
                    compositeAssetId,
                    randomId(),
                    s -> newId(uuid(s), VERSION_ZERO),
                    artifacts),
                artifacts,
                rep(HTML,TXT),
                s -> newId(uuid(s), VERSION_ZERO),
                s -> newId(uuid(s.toLowerCase()), VERSION_ZERO),
                s -> null);

    assertEquals(3, ckc.getComponent().size());
    assertNotNull(ckc.getStruct());

    assertEquals(compositeAssetId, ckc.getAssetId());
    assertNotEquals(ckc.getAssetId(), ckc.getStruct().getAssetId());
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

  @Test
  void testConstructSetComposite_Tree() throws IOException {
    ResourceIdentifier compositeAssetId = randomId();

    List<String> artifacts = Arrays.asList("A", "B", "C");
    Map<SemanticIdentifier, String> artifactsMap = new HashMap<>();
    artifacts.forEach(artifact ->
        artifactsMap.put(newId(uuid(artifact), VERSION_ZERO), artifact));
    MockLink mockLinkB = new MockLink("B");
    MockLink mockLinkC = new MockLink("C");

    ResourceIdentifier rootId = newId(uuid("A"), VERSION_ZERO);
    ResourceIdentifier structId = randomId();

    CompositeKnowledgeCarrier ckc =
        ofUniformNamedComposite(
            compositeAssetId,
            null,
            rootId,
            "mock name",
            CompositeStructType.TREE,
            inferStruct(
                compositeAssetId,
                rootId,
                structId,
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
                s -> newId(uuid(s), VERSION_ZERO),
                artifacts),
            artifacts,
            rep(HTML,TXT),
            s -> newId(uuid(s), VERSION_ZERO),
            s -> newId(uuid(s.toLowerCase()), VERSION_ZERO),
            s -> null);

    assertEquals(3, ckc.getComponent().size());

    List<Statement> hasStructuralStatements =
        artifacts.stream()
            .flatMap(
                artifact -> Stream.of(
                    objA(ckc.getAssetId().getVersionId().toString(),
                        Has_Structural_Component.getReferentId().toString(),
                        newId(uuid(artifact), VERSION_ZERO).getVersionId().toString()),
                    objA(ckc.getAssetId().getVersionId().toString(),
                        Has_Structuring_Component.getReferentId().toString(),
                        structId.getVersionId().toString()),
                    objA(ckc.getAssetId().getVersionId().toString(),
                        RDF.type.getURI(),
                        KNOWLEDGE_ASSET_URI),
                    objA(newId(uuid(artifact), VERSION_ZERO).getVersionId().toString(),
                        RDF.type.getURI(),
                        KNOWLEDGE_ASSET_URI)))
            .collect(Collectors.toList());
    hasStructuralStatements.add(
        objA(
            rootId.getVersionId().toString(),
            mockLinkB.getRel().getResourceId().toString(),
            mockLinkB.getHrefVersionURI().toString()));
    hasStructuralStatements.add(
        objA(
            rootId.getVersionId().toString(),
            mockLinkC.getRel().getResourceId().toString(),
            mockLinkC.getHrefVersionURI().toString()));

    String actualStruct = ckc.getStruct().getExpression().toString();
    Model actualModel =
        ModelFactory.createDefaultModel()
            .read(IOUtils.toInputStream(actualStruct, "UTF-8"), null, "TURTLE");

    Model expectedModel = ModelFactory.createDefaultModel().add(hasStructuralStatements);
    assertTrue(actualModel.isIsomorphicWith(expectedModel));

    assertEquals(compositeAssetId, ckc.getAssetId());
    assertNotEquals(ckc.getAssetId(), ckc.getStruct().getAssetId());
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

  @Test
  void testUniformAggregate() {
    KnowledgeCarrier kc1 = mockCarrier("AAA");
    KnowledgeCarrier kc2 = mockCarrier("BBB");
    KnowledgeCarrier kc3 = mockCarrier("CCC");

    CompositeKnowledgeCarrier ckc = ofUniformAggregate(Arrays.asList(kc1,kc2,kc3));

    assertNotNull(ckc.getAssetId());
    assertNull(ckc.getRootId());
    assertNull(ckc.getArtifactId());

    assertTrue(HTML.sameAs(ckc.getRepresentation().getLanguage()));
    assertTrue(Serialized_Knowledge_Expression.sameAs(ckc.getLevel()));

    assertSame(CompositeStructType.NONE,ckc.getStructType());
    assertNull(ckc.getStruct());

    assertTrue(ckc.tryMainComponent().isEmpty());
    assertEquals(3, ckc.getComponent().size());
  }

  @Test
  void testMixedAggregate() {
    KnowledgeCarrier kc1 = mockCarrier("AAA");
    KnowledgeCarrier kc2 = mockFormalCarrier("XXX");

    CompositeKnowledgeCarrier ckc = ofMixedAggregate(Arrays.asList(kc1,kc2));

    assertNotNull(ckc.getAssetId());
    assertNull(ckc.getRootId());
    assertNull(ckc.getArtifactId());

    assertNull(ckc.getRepresentation());
    assertNull(ckc.getLevel());

    assertSame(CompositeStructType.NONE,ckc.getStructType());
    assertNull(ckc.getStruct());

    assertTrue(ckc.tryMainComponent().isEmpty());
    assertEquals(2, ckc.getComponent().size());
  }

  @Test
  void testDefensiveOnAggregate() {
    KnowledgeCarrier kc1 = mockCarrier("AAA");
    KnowledgeCarrier kc2 = mockFormalCarrier("XXX");

    assertThrows(IllegalArgumentException.class,
        () -> ofUniformAggregate(Arrays.asList(kc1,kc2)));
  }

  @Test
  void testUniformAnonymous() {
    KnowledgeCarrier kc1 = mockCarrier("AAA");
    KnowledgeCarrier kc2 = mockCarrier("BBB");
    KnowledgeCarrier kc3 = mockCarrier("CCC");

    CompositeKnowledgeCarrier ckc = ofUniformAnonymousComposite(
        kc1.getAssetId(),
        Arrays.asList(kc1,kc2,kc3));

    assertNotNull(ckc.getAssetId());
    assertNotNull(ckc.getRootId());
    assertNull(ckc.getArtifactId());

    assertTrue(HTML.sameAs(ckc.getRepresentation().getLanguage()));
    assertTrue(Serialized_Knowledge_Expression.sameAs(ckc.getLevel()));
  }


  @Test
  void testAnonymousGraph() {
    KnowledgeCarrier kc1 = mockCarrier("AAA");
    KnowledgeCarrier kc2 = mockCarrier("BBB");
    KnowledgeCarrier kc3 = mockCarrier("CCC");

    CompositeKnowledgeCarrier ckc = ofUniformAnonymousComposite(
        kc1.getAssetId(),
        Arrays.asList(kc1,kc2,kc3));

    assertNotNull(ckc.getAssetId());
    assertEquals(kc1.getAssetId().asKey(), ckc.getRootId().asKey());

    assertSame(GRAPH, ckc.getStructType());
    assertNotNull(ckc.getStruct());

    Model struct = structAsModel(ckc);
    assertTrue(struct.contains(objA(
        ckc.getAssetId().getVersionId(),
        Has_Structural_Component.getReferentId(),
        kc1.getAssetId().getVersionId()
    )));
    assertTrue(struct.contains(objA(
        ckc.getAssetId().getVersionId(),
        Has_Structural_Component.getReferentId(),
        kc2.getAssetId().getVersionId()
    )));
    assertTrue(struct.contains(objA(
        ckc.getAssetId().getVersionId(),
        Has_Structural_Component.getReferentId(),
        kc3.getAssetId().getVersionId()
    )));
    assertTrue(struct.contains(objA(
        ckc.getAssetId().getVersionId(),
        Has_Structuring_Component.getReferentId(),
        ckc.getStruct().getAssetId().getVersionId()
    )));

    assertEquals(3, ckc.getComponent().size());
    assertTrue(ckc.tryMainComponent().isPresent());
    assertEquals(kc1.getAssetId().asKey(), ckc.mainComponent().getAssetId().asKey());
  }

  @Test
  void testAnonymousSet() {
    KnowledgeCarrier kc1 = mockCarrier("AAA");
    KnowledgeCarrier kc2 = mockCarrier("BBB");
    KnowledgeCarrier kc3 = mockCarrier("CCC");

    CompositeKnowledgeCarrier ckc = ofUniformAnonymousComposite(
        Arrays.asList(kc1, kc2, kc3));

    assertNotNull(ckc.getAssetId());
    assertNull(ckc.getRootId());

    assertSame(SET, ckc.getStructType());
    assertNotNull(ckc.getStruct());

    Model struct = structAsModel(ckc);
    assertTrue(struct.contains(objA(
        ckc.getAssetId().getVersionId(),
        Has_Structural_Component.getReferentId(),
        kc1.getAssetId().getVersionId()
    )));

  }


  @Test
  void testOfMixedAnonymousWithUserProvidedStruct() {
    KnowledgeCarrier kc1 = mockCarrier("AAA");
    KnowledgeCarrier kc2 = mockCarrier("BBB");
    KnowledgeCarrier kc3 = mockCarrier("CCC");

    CompositeKnowledgeCarrier ckc = ofMixedAnonymousComposite(
        kc1.getAssetId(),
        x -> {
          if (kc1.getAssetId().asKey().equals(x.getAssetId().asKey())) {
            return Arrays.asList(
                new MockLink(kc2.getAssetId().getVersionId()),
                new MockLink(kc3.getAssetId().getVersionId()));
          } else {
            return Collections.emptyList();
          }
        },
        TREE,
        Arrays.asList(kc1, kc2, kc3));

    assertNotNull(ckc.getAssetId());
    assertNotNull(ckc.getRootId());
    assertEquals(kc1.getAssetId().asKey(), ckc.getRootId().asKey());

    assertSame(TREE, ckc.getStructType());
    assertNotNull(ckc.getStruct());

    Model struct = structAsModel(ckc);
    assertTrue(struct.contains(objA(
        ckc.getAssetId().getVersionId(),
        Has_Structural_Component.getReferentId(),
        kc1.getAssetId().getVersionId()
    )));

  }

  @Test
  void testOfUniformAnonymousWithUserProvidedStruct() {
    KnowledgeCarrier kc1 = mockCarrier("AAA");
    KnowledgeCarrier kc2 = mockCarrier("BBB");
    KnowledgeCarrier kc3 = mockCarrier("CCC");

    CompositeKnowledgeCarrier ckc = ofUniformAnonymousComposite(
        kc1.getAssetId(),
        x -> {
          if (kc1.getAssetId().asKey().equals(x.getAssetId().asKey())) {
            return Arrays.asList(
                new MockLink("BBB"),
                new MockLink("CCC"));
          } else {
            return Collections.emptyList();
          }
        },
        TREE,
        Arrays.asList(kc1, kc2, kc3));

    assertNotNull(ckc.getAssetId());
    assertNotNull(ckc.getRootId());
    assertEquals(kc1.getAssetId().asKey(), ckc.getRootId().asKey());

    assertSame(TREE, ckc.getStructType());
    assertNotNull(ckc.getStruct());

    Model struct = structAsModel(ckc);
    assertTrue(struct.contains(objA(
        kc1.getAssetId().getVersionId(),
        TermsHelper.mayo("hasChild", "hasChild").getReferentId(),
        kc2.getAssetId().getVersionId()
    )));

    assertNotNull(ckc.getRepresentation());
    assertTrue(HTML.sameAs(ckc.getRepresentation().getLanguage()));
  }


  @Test
  void testOfUniformNamed() {
    ResourceIdentifier compositeAssetId = randomId();
    KnowledgeCarrier kc1 = mockCarrier("AAA");
    KnowledgeCarrier kc2 = mockCarrier("BBB");
    KnowledgeCarrier kc3 = mockCarrier("CCC");

    CompositeKnowledgeCarrier ckc = ofUniformNamedComposite(
        compositeAssetId,
        null,
        kc1.getAssetId(),
        "Mock",
        TREE,
        x -> {
          if (kc1.getAssetId().asKey().equals(x.getAssetId().asKey())) {
            return Arrays.asList(
                new MockLink("BBB"),
                new MockLink("CCC"));
          } else {
            return Collections.emptyList();
          }
        },
        Arrays.asList(kc1, kc2, kc3));

    assertEquals(compositeAssetId.asKey(), ckc.getAssetId().asKey());
    assertNotNull(ckc.getRootId());
    assertEquals("Mock", ckc.getLabel());
    assertEquals(kc1.getAssetId().asKey(), ckc.getRootId().asKey());

    assertSame(TREE, ckc.getStructType());
    assertNotNull(ckc.getStruct());

    Model struct = structAsModel(ckc);
    assertTrue(struct.contains(objA(
        kc1.getAssetId().getVersionId(),
        TermsHelper.mayo("hasChild", "hasChild").getReferentId(),
        kc2.getAssetId().getVersionId()
    )));

    assertNotNull(ckc.getRepresentation());
    assertTrue(HTML.sameAs(ckc.getRepresentation().getLanguage()));
  }


  private Model structAsModel(CompositeKnowledgeCarrier ckc) {
    Model m = ModelFactory.createDefaultModel();
    return ckc.getStruct().asString()
        .map(s -> m.read(new ByteArrayInputStream(s.getBytes()), null, "TTL"))
        .orElseGet(Assertions::fail);
  }


  private KnowledgeCarrier mockFormalCarrier(String content) {
    return of("<dmn>" + content + "<dmn>")
        .withRepresentation(rep(DMN_1_1,XML_1_1, Charset.defaultCharset()))
        .withLabel(content.substring(0,1))
        .withAssetId(newId(uuid(content),VERSION_ZERO))
        .withArtifactId(newId(uuid(content + "X"),VERSION_ZERO));
  }

  private KnowledgeCarrier mockCarrier(String content) {
    return of("<p>" + content + "<p>")
        .withRepresentation(rep(HTML,TXT, Charset.defaultCharset()))
        .withLabel(content.substring(0,1))
        .withAssetId(newId(uuid(content),VERSION_ZERO))
        .withArtifactId(newId(uuid(content + "X"),VERSION_ZERO));
  }


  static class MockLink implements Link {

    String tgt;

    public MockLink(URI uri) {
      this.tgt = uri.toString();
    }

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
