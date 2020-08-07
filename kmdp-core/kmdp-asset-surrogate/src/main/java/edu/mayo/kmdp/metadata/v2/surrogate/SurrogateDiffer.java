package edu.mayo.kmdp.metadata.v2.surrogate;

import edu.mayo.kmdp.comparator.AbstractDiffer;
import edu.mayo.kmdp.terms.VersionableTerm;
import edu.mayo.ontology.taxonomies.iso639_2_languagecodes.Language;
import edu.mayo.ontology.taxonomies.kao.knowledgeartifactcategory.IKnowledgeArtifactCategory;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetcategory.KnowledgeAssetCategory;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetrole.KnowledgeAssetRole;
import edu.mayo.ontology.taxonomies.kao.knowledgeassettype.KnowledgeAssetType;
import edu.mayo.ontology.taxonomies.kao.knowledgeprocessingtechnique.KnowledgeProcessingTechnique;
import edu.mayo.ontology.taxonomies.kao.languagerole.KnowledgeRepresentationLanguageRole;
import edu.mayo.ontology.taxonomies.kao.publicationstatus.PublicationStatus;
import edu.mayo.ontology.taxonomies.kao.publishingrole.PublishingRole;
import edu.mayo.ontology.taxonomies.kao.rel.citationreltype.BibliographicCitationType;
import edu.mayo.ontology.taxonomies.kao.rel.dependencyreltype.DependencyType;
import edu.mayo.ontology.taxonomies.kao.rel.derivationreltype.DerivationType;
import edu.mayo.ontology.taxonomies.kao.rel.relatedversiontype.RelatedVersionType;
import edu.mayo.ontology.taxonomies.kao.rel.structuralreltype.StructuralPartType;
import edu.mayo.ontology.taxonomies.kao.rel.summaryreltype.SummarizationType;
import edu.mayo.ontology.taxonomies.kao.rel.variantreltype.VariantType;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormat;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import edu.mayo.ontology.taxonomies.krprofile.KnowledgeRepresentationLanguageProfile;
import edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerialization;
import edu.mayo.ontology.taxonomies.lexicon.Lexicon;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.MappingStyle;
import org.javers.core.metamodel.clazz.EntityDefinition;

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
