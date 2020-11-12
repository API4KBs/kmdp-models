package org.omg.spec.api4kp._20200801.surrogate;

import static java.util.Collections.singletonList;

import edu.mayo.kmdp.comparator.AbstractDiffer;
import edu.mayo.ontology.taxonomies.kmdo.citationreltype.BibliographicCitationType;
import edu.mayo.ontology.taxonomies.kmdo.publishingrole.PublishingRole;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.MappingStyle;
import org.javers.core.metamodel.clazz.EntityDefinition;
import org.omg.spec.api4kp._20200801.id.ConceptIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.taxonomy.dependencyreltype.DependencyType;
import org.omg.spec.api4kp._20200801.taxonomy.derivationreltype.DerivationType;
import org.omg.spec.api4kp._20200801.taxonomy.iso639_2_languagecode.Language;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeartifactcategory.KnowledgeArtifactCategory;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetcategory.KnowledgeAssetCategory;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetrole.KnowledgeAssetRole;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetType;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeprocessingtechnique.KnowledgeProcessingTechnique;
import org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormat;
import org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguage;
import org.omg.spec.api4kp._20200801.taxonomy.krprofile.KnowledgeRepresentationLanguageProfile;
import org.omg.spec.api4kp._20200801.taxonomy.krserialization.KnowledgeRepresentationLanguageSerialization;
import org.omg.spec.api4kp._20200801.taxonomy.languagerole.KnowledgeRepresentationLanguageRole;
import org.omg.spec.api4kp._20200801.taxonomy.lexicon.Lexicon;
import org.omg.spec.api4kp._20200801.taxonomy.publicationstatus.PublicationStatus;
import org.omg.spec.api4kp._20200801.taxonomy.relatedversiontype.RelatedVersionType;
import org.omg.spec.api4kp._20200801.taxonomy.structuralreltype.StructuralPartType;
import org.omg.spec.api4kp._20200801.taxonomy.summaryreltype.SummarizationType;
import org.omg.spec.api4kp._20200801.taxonomy.variantreltype.VariantType;
import org.omg.spec.api4kp._20200801.terms.ConceptTerm;
import org.omg.spec.api4kp._20200801.terms.TypedTerm;
import org.omg.spec.api4kp._20200801.terms.VersionableTerm;

public class SurrogateDiffer extends AbstractDiffer<KnowledgeAsset> {

  public SurrogateDiffer() {
  }

  public SurrogateDiffer(Mode mode) {
    super(mode);
  }

  @Override
  protected Javers init() {
    JaversBuilder builder = JaversBuilder.javers()
        .withMappingStyle(MappingStyle.BEAN)

        .registerEntity(
            new EntityDefinition(KnowledgeAsset.class,
                "assetId",
                singletonList("lifecycle")))
        .registerEntity(
            new EntityDefinition(KnowledgeArtifact.class,
                "artifactId",
                singletonList("lifecycle")));

    configureTerminology(builder);

    return builder.build();
  }

  public static boolean isEquivalent(KnowledgeAsset a1, KnowledgeAsset a2) {
    Comparison c = new SurrogateDiffer().contrast(a1, a2);
    return c == Comparison.EQUAL || c == Comparison.EQUIVALENT || c == Comparison.IDENTICAL;
  }


  public static void configureTerminology(JaversBuilder builder) {
    List<Class<? extends ConceptTerm>> valuesetKlasses =
        Arrays.asList(
            DependencyType.class,
            KnowledgeAssetCategory.class,
            KnowledgeAssetType.class,
            KnowledgeProcessingTechnique.class,
            KnowledgeAssetRole.class,
            StructuralPartType.class,
            DerivationType.class,
            VariantType.class,
            RelatedVersionType.class,
            BibliographicCitationType.class,
            PublicationStatus.class,
            PublishingRole.class,
            Language.class,
            KnowledgeArtifactCategory.class,
            SummarizationType.class,
            KnowledgeRepresentationLanguage.class,
            KnowledgeRepresentationLanguageProfile.class,
            SerializationFormat.class,
            Lexicon.class,
            KnowledgeRepresentationLanguageSerialization.class,
            KnowledgeRepresentationLanguageRole.class
        );

    valuesetKlasses.forEach(klass ->
        builder.registerValue(klass, ConceptTerm::sameTermAs, Objects::toString));

    builder.registerValue(ConceptIdentifier.class,
        SemanticIdentifier::sameAs, c -> c.getResourceId() + " | " + c.getLabel());

  }
}
