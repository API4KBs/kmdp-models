package org.omg.spec.api4kp._20200801.surrogate;

import edu.mayo.kmdp.comparator.AbstractDiffer;
import edu.mayo.kmdp.terms.VersionableTerm;
import edu.mayo.ontology.taxonomies.kmdo.citationreltype.BibliographicCitationType;
import edu.mayo.ontology.taxonomies.kmdo.publicationstatus.PublicationStatus;
import edu.mayo.ontology.taxonomies.kmdo.publishingrole.PublishingRole;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.MappingStyle;
import org.javers.core.metamodel.clazz.EntityDefinition;
import org.omg.spec.api4kp.taxonomy.dependencyreltype.DependencyType;
import org.omg.spec.api4kp.taxonomy.derivationreltype.DerivationType;
import org.omg.spec.api4kp.taxonomy.iso639_2_languagecode.Language;
import org.omg.spec.api4kp.taxonomy.knowledgeartifactcategory.IKnowledgeArtifactCategory;
import org.omg.spec.api4kp.taxonomy.knowledgeassetcategory.KnowledgeAssetCategory;
import org.omg.spec.api4kp.taxonomy.knowledgeassetrole.KnowledgeAssetRole;
import org.omg.spec.api4kp.taxonomy.knowledgeassettype.KnowledgeAssetType;
import org.omg.spec.api4kp.taxonomy.knowledgeprocessingtechnique.KnowledgeProcessingTechnique;
import org.omg.spec.api4kp.taxonomy.krformat.SerializationFormat;
import org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguage;
import org.omg.spec.api4kp.taxonomy.krprofile.KnowledgeRepresentationLanguageProfile;
import org.omg.spec.api4kp.taxonomy.krserialization.KnowledgeRepresentationLanguageSerialization;
import org.omg.spec.api4kp.taxonomy.languagerole.KnowledgeRepresentationLanguageRole;
import org.omg.spec.api4kp.taxonomy.lexicon.Lexicon;
import org.omg.spec.api4kp.taxonomy.relatedversiontype.RelatedVersionType;
import org.omg.spec.api4kp.taxonomy.structuralreltype.StructuralPartType;
import org.omg.spec.api4kp.taxonomy.summaryreltype.SummarizationType;
import org.omg.spec.api4kp.taxonomy.variantreltype.VariantType;

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
            new EntityDefinition(KnowledgeAsset.class, "assetId",
                Arrays.asList("carriers", "annotation")))
        .registerEntity(
            new EntityDefinition(KnowledgeArtifact.class, "artifactId")
        );

    configureTerminology(builder);

    return builder.build();
  }

  public static boolean isEquivalent(KnowledgeAsset a1, KnowledgeAsset a2) {
    Comparison c = new SurrogateDiffer().contrast(a1, a2);
    return c == Comparison.EQUAL || c == Comparison.EQUIVALENT || c == Comparison.IDENTICAL;
  }


  public static void configureTerminology(JaversBuilder builder) {
    List<Class<? extends VersionableTerm<?, ?>>> valuesetKlasses =
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
            IKnowledgeArtifactCategory.class,
            SummarizationType.class,
            KnowledgeRepresentationLanguage.class,
            KnowledgeRepresentationLanguageProfile.class,
            SerializationFormat.class,
            Lexicon.class,
            KnowledgeRepresentationLanguageSerialization.class,
            KnowledgeRepresentationLanguageRole.class
        );

    valuesetKlasses.forEach(klass ->
        builder.registerValue(klass, VersionableTerm::isSameEntity, Objects::toString));

  }
}
