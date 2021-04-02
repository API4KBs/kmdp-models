package edu.mayo.kmdp;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._20200801.surrogate.SurrogateBuilder.randomArtifactId;
import static org.omg.spec.api4kp._20200801.surrogate.SurrogateBuilder.randomAssetId;
import static org.omg.spec.api4kp._20200801.taxonomy.dependencyreltype.DependencyTypeSeries.Depends_On;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries.Clinical_Rule;
import static org.omg.spec.api4kp._20200801.taxonomy.publicationstatus.PublicationStatusSeries.Draft;
import static org.omg.spec.api4kp._20200801.taxonomy.publicationstatus.PublicationStatusSeries.Published;

import edu.mayo.kmdp.comparator.AbstractDiffer.Mode;
import edu.mayo.kmdp.comparator.Contrastor.Comparison;
import java.net.URI;
import java.util.Date;
import org.javers.core.diff.Diff;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.id.ConceptIdentifier;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.surrogate.Annotation;
import org.omg.spec.api4kp._20200801.surrogate.Dependency;
import org.omg.spec.api4kp._20200801.surrogate.KnowledgeArtifact;
import org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset;
import org.omg.spec.api4kp._20200801.surrogate.Publication;
import org.omg.spec.api4kp._20200801.surrogate.SurrogateBuilder;
import org.omg.spec.api4kp._20200801.surrogate.SurrogateDiffer;
import org.omg.spec.api4kp._20200801.surrogate.SurrogateHelper;
import org.omg.spec.api4kp._20200801.surrogate.SurrogateHelper.VersionIncrement;
import org.omg.spec.api4kp._20200801.taxonomy.dependencyreltype.snapshot.DependencyType;

class AssetSurrogateDiffTest {


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
    ResourceIdentifier tgtRef = randomAssetId();

    KnowledgeAsset base = new KnowledgeAsset()
        .withAssetId(randomAssetId())
        .withFormalType(Clinical_Rule)
        .withCarriers(new KnowledgeArtifact()
            .withArtifactId(randomArtifactId()));

    KnowledgeAsset mod = ((KnowledgeAsset) base.clone());

    mod.getCarriers().get(0)
        .withLifecycle(new Publication()
            .withCreatedOn(new Date())
            .withPublicationStatus(Published));
    base.getCarriers().get(0)
        .withLifecycle(new Publication()
            .withCreatedOn(new Date())
            .withPublicationStatus(Draft));

    base.withLinks(new Dependency()
        .withRel(Depends_On)
        .withHref(tgtRef));
    mod.withLinks(new Dependency()
        .withRel(DependencyType.Depends_On)
        .withHref(tgtRef));

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


  @Test
  void testDiffeWithRootClass() {
    SurrogateDiffer differ = new SurrogateDiffer(Mode.SYMMETRIC);

    ResourceIdentifier aid = randomAssetId();

    KnowledgeAsset base1 = new KnowledgeAsset()
        .withAssetId(aid);

    KnowledgeAsset base2 = new org.omg.spec.api4kp._20200801.surrogate.resources.KnowledgeAsset()
        .withAssetId((ResourceIdentifier) aid.clone());

    Comparison delta = differ.contrast(base1,base2);
    assertSame(Comparison.EQUIVALENT, delta);
  }



  @Test
  void testDifferWithSecondaryId() {
    SurrogateDiffer differ = new SurrogateDiffer(Mode.SYMMETRIC);

    ResourceIdentifier aid = randomAssetId();
    ResourceIdentifier sid1 = randomAssetId();
    ResourceIdentifier sid2 = (ResourceIdentifier) sid1.clone();
    sid2.setEstablishedOn(new Date(100));

    KnowledgeAsset base1 = new KnowledgeAsset()
        .withAssetId(aid)
        .withSecondaryId(sid1);

    KnowledgeAsset base2 = new KnowledgeAsset()
        .withAssetId(aid)
        .withSecondaryId(sid2);

    Comparison delta = differ.contrast(base1,base2);
    assertSame(Comparison.EQUIVALENT, delta);
  }


  @Test
  void testControlledIncrementDiff() {
    ResourceIdentifier aid = randomAssetId();
    ResourceIdentifier sid1 = randomAssetId();
    SurrogateDiffer differ = new SurrogateDiffer(Mode.SYMMETRIC);

    KnowledgeAsset base1 = SurrogateBuilder
        .newSurrogate(aid)
        .get()
        .withSecondaryId(sid1);
    KnowledgeAsset base2 = SurrogateBuilder
        .newSurrogate(aid)
        .get()
        .withSecondaryId(sid1);

    base2.setDescription("Foo");
    SurrogateHelper.incrementVersion(base2, VersionIncrement.MINOR);

    Diff delta = differ.diff(base1, base2);
    assertTrue(delta.hasChanges());
  }

}
