package edu.mayo.kmdp.surrogate;

import edu.mayo.kmdp.series.Versionable;
import edu.mayo.kmdp.terms.VersionableTerm;
import edu.mayo.kmdp.util.URIUtil;
import edu.mayo.ontology.taxonomies.iso639_2_languagecodes.Language;
import edu.mayo.ontology.taxonomies.iso639_2_languagecodes.LanguageSeries;
import edu.mayo.ontology.taxonomies.kao.knowledgeartifactcategory.IKnowledgeArtifactCategory;
import edu.mayo.ontology.taxonomies.kao.knowledgeartifactcategory.KnowledgeArtifactCategorySeries;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetcategory.KnowledgeAssetCategory;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetcategory.KnowledgeAssetCategorySeries;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetrole.KnowledgeAssetRole;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetrole.KnowledgeAssetRoleSeries;
import edu.mayo.ontology.taxonomies.kao.knowledgeassettype.KnowledgeAssetType;
import edu.mayo.ontology.taxonomies.kao.knowledgeassettype.KnowledgeAssetTypeSeries;
import edu.mayo.ontology.taxonomies.kao.knowledgeprocessingtechnique.KnowledgeProcessingTechnique;
import edu.mayo.ontology.taxonomies.kao.knowledgeprocessingtechnique.KnowledgeProcessingTechniqueSeries;
import edu.mayo.ontology.taxonomies.kao.languagerole.KnowledgeRepresentationLanguageRole;
import edu.mayo.ontology.taxonomies.kao.languagerole.KnowledgeRepresentationLanguageRoleSeries;
import edu.mayo.ontology.taxonomies.kao.publicationstatus.PublicationStatus;
import edu.mayo.ontology.taxonomies.kao.publicationstatus.PublicationStatusSeries;
import edu.mayo.ontology.taxonomies.kao.publishingrole.PublishingRole;
import edu.mayo.ontology.taxonomies.kao.publishingrole.PublishingRoleSeries;
import edu.mayo.ontology.taxonomies.kao.rel.citationreltype.BibliographicCitationType;
import edu.mayo.ontology.taxonomies.kao.rel.citationreltype.BibliographicCitationTypeSeries;
import edu.mayo.ontology.taxonomies.kao.rel.dependencyreltype.DependencyType;
import edu.mayo.ontology.taxonomies.kao.rel.dependencyreltype.DependencyTypeSeries;
import edu.mayo.ontology.taxonomies.kao.rel.derivationreltype.DerivationType;
import edu.mayo.ontology.taxonomies.kao.rel.derivationreltype.DerivationTypeSeries;
import edu.mayo.ontology.taxonomies.kao.rel.relatedversiontype.RelatedVersionType;
import edu.mayo.ontology.taxonomies.kao.rel.relatedversiontype.RelatedVersionTypeSeries;
import edu.mayo.ontology.taxonomies.kao.rel.structuralreltype.StructuralPartType;
import edu.mayo.ontology.taxonomies.kao.rel.structuralreltype.StructuralPartTypeSeries;
import edu.mayo.ontology.taxonomies.kao.rel.summaryreltype.SummarizationType;
import edu.mayo.ontology.taxonomies.kao.rel.summaryreltype.SummarizationTypeSeries;
import edu.mayo.ontology.taxonomies.kao.rel.variantreltype.VariantType;
import edu.mayo.ontology.taxonomies.kao.rel.variantreltype.VariantTypeSeries;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormat;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries;
import edu.mayo.ontology.taxonomies.krprofile.KnowledgeRepresentationLanguageProfile;
import edu.mayo.ontology.taxonomies.krprofile.KnowledgeRepresentationLanguageProfileSeries;
import edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerialization;
import edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries;
import edu.mayo.ontology.taxonomies.lexicon.Lexicon;
import edu.mayo.ontology.taxonomies.lexicon.LexiconSeries;
import java.net.URI;
import java.util.Optional;
import org.omg.spec.api4kp._20200801.id.Term;

public class LegacyTermNamespaceMap {

  public static Optional<? extends Term> resolveKnownConceptByNamespace(URI conceptId, URI conceptNamespaceId, String versionTag) {
    Optional<? extends Term> res = Optional.empty();
    String coreUri = URIUtil.normalizeURIString(conceptNamespaceId);
    switch (coreUri) {
      case DependencyType.seriesIdentifier:
        res = DependencyTypeSeries.resolveId(conceptId)
            .map(x -> pickVersion(x,versionTag));
        break;
      case KnowledgeAssetCategory.seriesIdentifier:
        res = KnowledgeAssetCategorySeries.resolveId(conceptId)
            .map(x -> pickVersion(x,versionTag));
        break;
      case KnowledgeAssetType.seriesIdentifier:
        res = KnowledgeAssetTypeSeries.resolveId(conceptId)
            .map(x -> pickVersion(x,versionTag));
        break;
      case KnowledgeProcessingTechnique.seriesIdentifier:
        res = KnowledgeProcessingTechniqueSeries.resolveId(conceptId)
            .map(x -> pickVersion(x,versionTag));
        break;
      case KnowledgeAssetRole.seriesIdentifier:
        res = KnowledgeAssetRoleSeries.resolveId(conceptId)
            .map(x -> pickVersion(x,versionTag));
        break;
      case StructuralPartType.seriesIdentifier:
        res = StructuralPartTypeSeries.resolveId(conceptId)
            .map(x -> pickVersion(x,versionTag));
        break;
      case DerivationType.seriesIdentifier:
        res = DerivationTypeSeries.resolveId(conceptId)
            .map(x -> pickVersion(x,versionTag));
        break;
      case VariantType.seriesIdentifier:
        res = VariantTypeSeries.resolveId(conceptId)
            .map(x -> pickVersion(x,versionTag));
        break;
      case RelatedVersionType.seriesIdentifier:
        res = RelatedVersionTypeSeries.resolveId(conceptId)
            .map(x -> pickVersion(x,versionTag));
        break;
      case BibliographicCitationType.seriesIdentifier:
        res = BibliographicCitationTypeSeries.resolveId(conceptId)
            .map(x -> pickVersion(x,versionTag));
        break;
      case PublicationStatus.seriesIdentifier:
        res = PublicationStatusSeries.resolveId(conceptId)
            .map(x -> pickVersion(x,versionTag));
        break;
      case PublishingRole.seriesIdentifier:
        res = PublishingRoleSeries.resolveId(conceptId)
            .map(x -> pickVersion(x,versionTag));
        break;
      case Language.seriesIdentifier:
        res = LanguageSeries.resolveId(conceptId)
            .map(x -> pickVersion(x,versionTag));
        break;
      case IKnowledgeArtifactCategory.seriesIdentifier:
        res = KnowledgeArtifactCategorySeries.resolveId(conceptId)
            .map(x -> pickVersion(x,versionTag));
        break;
      case SummarizationType.seriesIdentifier:
        res = SummarizationTypeSeries.resolveId(conceptId)
            .map(x -> pickVersion(x,versionTag));
        break;
      case KnowledgeRepresentationLanguage.seriesIdentifier:
        res = KnowledgeRepresentationLanguageSeries.resolveId(conceptId)
            .map(x -> pickVersion(x,versionTag));
        break;
      case KnowledgeRepresentationLanguageProfile.seriesIdentifier:
        res = KnowledgeRepresentationLanguageProfileSeries.resolveId(conceptId)
            .map(x -> pickVersion(x,versionTag));
        break;
      case SerializationFormat.seriesIdentifier:
        res = SerializationFormatSeries.resolveId(conceptId)
            .map(x -> pickVersion(x,versionTag));
        break;
      case Lexicon.seriesIdentifier:
        res = LexiconSeries.resolveId(conceptId)
            .map(x -> pickVersion(x,versionTag));
        break;
      case KnowledgeRepresentationLanguageSerialization.seriesIdentifier:
        res = KnowledgeRepresentationLanguageSerializationSeries.resolveId(conceptId)
            .map(x -> pickVersion(x,versionTag));
        break;
      case KnowledgeRepresentationLanguageRole.seriesIdentifier:
        res = KnowledgeRepresentationLanguageRoleSeries.resolveId(conceptId)
            .map(x -> pickVersion(x,versionTag));
        break;
      default:

    }
    return res;
  }

  private static Term pickVersion(VersionableTerm x, String versionTag) {
    if (versionTag == null) {
      return (Term) x.asSeries().getLatest();
    }
    return (Term) x.asSeries().getVersions().stream()
        .filter(ver -> versionTag.equals(((Versionable) ver).getVersionIdentifier().getVersionTag()))
        .findFirst()
        .orElse(x);
  }
}
