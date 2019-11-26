package edu.mayo.kmdp;

import static edu.mayo.kmdp.SurrogateBuilder.id;
import static edu.mayo.ontology.taxonomies.kao.knowledgeassettype.KnowledgeAssetTypeSeries.Clinical_Rule;
import static org.junit.jupiter.api.Assertions.assertSame;

import edu.mayo.kmdp.comparator.Contrastor.Comparison;
import edu.mayo.kmdp.metadata.annotations.BasicAnnotation;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.metadata.surrogate.Publication;
import edu.mayo.kmdp.comparator.AbstractDiffer.Mode;
import edu.mayo.kmdp.surrogate.SurrogateDiffer;
import edu.mayo.kmdp.terms.TermsHelper;
import edu.mayo.ontology.taxonomies.kao.publicationstatus.PublicationStatusSeries;
import java.net.URI;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;

public class MetadataDiffTest {


  @Test
  void testBasicDiffer() {
    SurrogateDiffer differ = new SurrogateDiffer(Mode.SYMMETRIC);

    KnowledgeAsset base = new KnowledgeAsset()
        .withAssetId(id(UUID.randomUUID(), "0.0.0"));

    KnowledgeAsset mod = ((KnowledgeAsset) base.clone())
        .withName("Test")
        .withFormalType(Clinical_Rule);

    Comparison delta = differ.contrast(mod,base);
    assertSame(Comparison.BROADER,delta);
  }


  @Test
  void testDiffExclusion() {
    SurrogateDiffer differ = new SurrogateDiffer(Mode.SYMMETRIC);

    KnowledgeAsset base = new KnowledgeAsset()
        .withAssetId(id(UUID.randomUUID(), "0.0.0"))
        .withFormalType(Clinical_Rule)
        .withLifecycle(new Publication()
            .withCreatedOn(new Date())
            .withPublicationStatus(PublicationStatusSeries.Draft));

    KnowledgeAsset mod = ((KnowledgeAsset) base.clone())
        .withLifecycle(new Publication()
            .withCreatedOn(new Date())
            .withPublicationStatus(PublicationStatusSeries.Published));

    Comparison delta = differ.contrast(mod,base);
    assertSame(Comparison.EQUIVALENT,delta);
  }


  @Test
  void testDiffWithConceptIdentifiers() {
    SurrogateDiffer differ = new SurrogateDiffer(Mode.SYMMETRIC);

    ConceptIdentifier trm = TermsHelper.mayo("Foo", "1234");

    KnowledgeAsset base = new KnowledgeAsset()
        .withAssetId(id(UUID.randomUUID(), "0.0.0"))
        .withSubject(new BasicAnnotation()
            .withRel((ConceptIdentifier) trm.clone()));

    KnowledgeAsset mod = new KnowledgeAsset()
        .withAssetId(base.getAssetId())
        .withSubject(new BasicAnnotation()
            .withRel((ConceptIdentifier) trm.clone()));

    Comparison delta = differ.contrast(mod, base);
    assertSame(Comparison.EQUIVALENT, delta);
  }


  @Test
  void testDiffWithConceptIdentifiersLegacyNamespace() {
    SurrogateDiffer differ = new SurrogateDiffer(Mode.SYMMETRIC);

    ConceptIdentifier trm = TermsHelper.mayo("Foo", "1234");

    KnowledgeAsset base = new KnowledgeAsset()
        .withAssetId(id(UUID.randomUUID(), "0.0.0"))
        .withSubject(new BasicAnnotation()
            .withRel((ConceptIdentifier) trm.clone()));

    ConceptIdentifier trm2 = (ConceptIdentifier) trm.clone();
    trm2.getNamespace().setId(URI.create(trm2.getNamespace().getId() + "#something"));

    KnowledgeAsset mod = new KnowledgeAsset()
        .withAssetId(base.getAssetId())
        .withSubject(new BasicAnnotation()
            .withRel(trm2));

    Comparison delta = differ.contrast(mod, base);
    assertSame(Comparison.EQUIVALENT, delta);
  }



}
