package edu.mayo.kmdp;

import static org.omg.spec.api4kp._20200801.taxonomy.publicationstatus.PublicationStatusSeries.Draft;
import static org.omg.spec.api4kp._20200801.taxonomy.publicationstatus.PublicationStatusSeries.Published;
import static org.omg.spec.api4kp._20200801.surrogate.SurrogateBuilder.randomArtifactId;
import static org.omg.spec.api4kp._20200801.surrogate.SurrogateBuilder.randomAssetId;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries.Clinical_Rule;

import edu.mayo.kmdp.comparator.AbstractDiffer.Mode;
import edu.mayo.kmdp.comparator.Contrastor.Comparison;
import org.omg.spec.api4kp._20200801.surrogate.KnowledgeArtifact;
import org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset;
import org.omg.spec.api4kp._20200801.surrogate.Publication;
import org.omg.spec.api4kp._20200801.surrogate.SurrogateDiffer;
import java.net.URI;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.id.ConceptIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.surrogate.Annotation;

public class AssetSurrogateDiffTest {


  @Test
  void testBasicDiffer() {
    SurrogateDiffer differ = new SurrogateDiffer(Mode.SYMMETRIC);

    KnowledgeAsset base = new KnowledgeAsset()
        .withAssetId(randomAssetId());

    KnowledgeAsset mod = ((KnowledgeAsset) base.clone())
        .withName("Test")
        .withFormalType(Clinical_Rule);

    Comparison delta = differ.contrast(mod, base);
    assertSame(Comparison.BROADER, delta);
  }


  @Test
  void testDiffExclusion() {
    SurrogateDiffer differ = new SurrogateDiffer(Mode.SYMMETRIC);

    KnowledgeAsset base = new KnowledgeAsset()
        .withAssetId(randomAssetId())
        .withFormalType(Clinical_Rule)
        .withCarriers(new KnowledgeArtifact()
            .withArtifactId(randomArtifactId())
            .withLifecycle(new Publication()
                .withCreatedOn(new Date())
                .withPublicationStatus(Draft)));

    KnowledgeAsset mod = ((KnowledgeAsset) base.clone());
    mod.getCarriers().get(0)
        .withLifecycle(new Publication()
            .withCreatedOn(new Date())
            .withPublicationStatus(Published));

    Comparison delta = differ.contrast(mod, base);
    assertSame(Comparison.EQUIVALENT, delta);
  }


  @Test
  void testDiffWithConceptIdentifiers() {
    SurrogateDiffer differ = new SurrogateDiffer(Mode.SYMMETRIC);

    ConceptIdentifier trm = Term.mock("Foo", "1234").asConceptIdentifier();

    KnowledgeAsset base = new KnowledgeAsset()
        .withAssetId(randomAssetId())
        .withAnnotation(new Annotation()
            .withRel((ConceptIdentifier) trm.clone()));

    KnowledgeAsset mod = new KnowledgeAsset()
        .withAssetId(base.getAssetId())
        .withAnnotation(new Annotation()
            .withRel((ConceptIdentifier) trm.clone()));

    Comparison delta = differ.contrast(mod, base);
    assertSame(Comparison.EQUIVALENT, delta);
  }


  @Test
  void testDiffWithConceptIdentifiersLegacyNamespace() {
    SurrogateDiffer differ = new SurrogateDiffer(Mode.SYMMETRIC);

    ConceptIdentifier trm = Term.mock("Foo", "1234").asConceptIdentifier();

    KnowledgeAsset base = new KnowledgeAsset()
        .withAssetId(randomAssetId())
        .withAnnotation(new Annotation()
            .withRel((ConceptIdentifier) trm.clone()));

    ConceptIdentifier trm2 = (ConceptIdentifier) trm.clone();
    trm2.setNamespaceUri(URI.create(trm2.getNamespaceUri() + "#something"));

    KnowledgeAsset mod = new KnowledgeAsset()
        .withAssetId(base.getAssetId())
        .withAnnotation(new Annotation()
            .withRel(trm2));

    Comparison delta = differ.contrast(mod, base);
    assertSame(Comparison.EQUIVALENT, delta);
  }


}
