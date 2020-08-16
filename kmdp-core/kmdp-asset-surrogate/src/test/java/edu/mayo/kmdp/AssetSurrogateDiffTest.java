package edu.mayo.kmdp;

import static edu.mayo.kmdp.metadata.v2.surrogate.SurrogateBuilder.randomArtifactId;
import static edu.mayo.kmdp.metadata.v2.surrogate.SurrogateBuilder.randomAssetId;
import static edu.mayo.ontology.taxonomies.kao.knowledgeassettype.KnowledgeAssetTypeSeries.Clinical_Rule;
import static org.junit.jupiter.api.Assertions.assertSame;

import edu.mayo.kmdp.comparator.AbstractDiffer.Mode;
import edu.mayo.kmdp.comparator.Contrastor.Comparison;
import edu.mayo.kmdp.metadata.v2.surrogate.ComputableKnowledgeArtifact;
import edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.metadata.v2.surrogate.Publication;
import edu.mayo.kmdp.metadata.v2.surrogate.SurrogateDiffer;
import edu.mayo.kmdp.metadata.v2.surrogate.annotations.Annotation;
import edu.mayo.ontology.taxonomies.kao.publicationstatus.PublicationStatusSeries;
import java.net.URI;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.id.ConceptIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;

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
        .withCarriers(new ComputableKnowledgeArtifact()
            .withArtifactId(randomArtifactId())
            .withLifecycle(new Publication()
                .withCreatedOn(new Date())
                .withPublicationStatus(PublicationStatusSeries.Draft)));

    KnowledgeAsset mod = ((KnowledgeAsset) base.clone());
    mod.getCarriers().get(0)
        .withLifecycle(new Publication()
            .withCreatedOn(new Date())
            .withPublicationStatus(PublicationStatusSeries.Published));

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
