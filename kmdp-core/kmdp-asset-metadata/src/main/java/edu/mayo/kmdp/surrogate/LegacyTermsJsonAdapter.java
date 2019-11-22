package edu.mayo.kmdp.surrogate;

import com.fasterxml.jackson.core.TreeNode;
import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.adapters.json.AbstractTermsJsonAdapter;
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
import java.util.Optional;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;

public class LegacyTermsJsonAdapter {

  protected LegacyTermsJsonAdapter() {
    // don't instantiate
  }

  public static class Serializer
      extends AbstractTermsJsonAdapter.AbstractSerializer<Term> {

  }

  public static class Deserializer
      extends AbstractTermsJsonAdapter.AbstractDeserializer<Term> {

    @Override
    protected Optional<Term> resolveAsKnownTerm(TreeNode t, String tagNode) {
      return resolveWithNamespace(t)
          .flatMap(resTerm -> resolveVersion(resTerm, t));
    }

    private Optional<Term> resolveWithNamespace(TreeNode t) {
      ConceptIdentifier ci = parseAsConceptIdentifier(t);
      if (ci == null) {
        throw new IllegalStateException("Controlled vocabularies should be serialized"
            + " as ConceptIdentifiers in this version");
      }

      Optional<? extends Term> res = Optional.empty();
      switch (ci.getNamespace().getTag()) {
        case DependencyType.schemeID:
          res = DependencyTypeSeries.resolve(ci);
          break;
        case KnowledgeAssetCategory.schemeID:
          res = KnowledgeAssetCategorySeries.resolve(ci);
          break;
        case KnowledgeAssetType.schemeID:
          res = KnowledgeAssetTypeSeries.resolve(ci);
          break;
        case KnowledgeProcessingTechnique.schemeID:
          res = KnowledgeProcessingTechniqueSeries.resolve(ci);
          break;
        case KnowledgeAssetRole.schemeID:
          res = KnowledgeAssetRoleSeries.resolve(ci);
          break;
        case StructuralPartType.schemeID:
          res = StructuralPartTypeSeries.resolve(ci);
          break;
        case DerivationType.schemeID:
          res = DerivationTypeSeries.resolve(ci);
          break;
        case VariantType.schemeID:
          res = VariantTypeSeries.resolve(ci);
          break;
        case RelatedVersionType.schemeID:
          res = RelatedVersionTypeSeries.resolve(ci);
          break;
        case BibliographicCitationType.schemeID:
          res = BibliographicCitationTypeSeries.resolve(ci);
          break;
        case PublicationStatus.schemeID:
          res = PublicationStatusSeries.resolve(ci);
          break;
        case PublishingRole.schemeID:
          res = PublishingRoleSeries.resolve(ci);
          break;
        case Language.schemeID:
          res = LanguageSeries.resolve(ci);
          break;
        case IKnowledgeArtifactCategory.schemeID:
          res = KnowledgeArtifactCategorySeries.resolve(ci);
          break;
        case SummarizationType.schemeID:
          res = SummarizationTypeSeries.resolve(ci);
          break;
        case KnowledgeRepresentationLanguage.schemeID:
          res = KnowledgeRepresentationLanguageSeries.resolve(ci);
          break;
        case KnowledgeRepresentationLanguageProfile.schemeID:
          res = KnowledgeRepresentationLanguageProfileSeries.resolve(ci);
          break;
        case SerializationFormat.schemeID:
          res = SerializationFormatSeries.resolve(ci);
          break;
        case Lexicon.schemeID:
          res = LexiconSeries.resolve(ci);
          break;
        case KnowledgeRepresentationLanguageSerialization.schemeID:
          res = KnowledgeRepresentationLanguageSerializationSeries.resolve(ci);
          break;
        case KnowledgeRepresentationLanguageRole.schemeID:
          res = KnowledgeRepresentationLanguageRoleSeries.resolve(ci);
          break;
        default:

      }

      return Optional.ofNullable(res.orElse(null));
    }

    @Override
    protected Term[] getValues() {
      throw new UnsupportedOperationException();
    }
  }

}
