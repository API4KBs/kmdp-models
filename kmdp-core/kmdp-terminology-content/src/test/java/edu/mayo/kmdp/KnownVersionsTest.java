package edu.mayo.kmdp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import edu.mayo.ontology.taxonomies.kao.ccgentries.ConceptDefinitionType;
import edu.mayo.ontology.taxonomies.kao.ccgentries.ConceptDefinitionTypeSeries;
import edu.mayo.ontology.taxonomies.kao.decisiontype.DecisionType;
import edu.mayo.ontology.taxonomies.kao.decisiontype.DecisionTypeSeries;
import edu.mayo.ontology.taxonomies.kmdo.citationreltype.BibliographicCitationType;
import edu.mayo.ontology.taxonomies.kmdo.citationreltype.BibliographicCitationTypeSeries;
import edu.mayo.ontology.taxonomies.kmdo.publicationeventtype.PublicationEventType;
import edu.mayo.ontology.taxonomies.kmdo.publicationeventtype.PublicationEventTypeSeries;
import edu.mayo.ontology.taxonomies.kmdo.publishingrole.PublishingRole;
import edu.mayo.ontology.taxonomies.kmdo.publishingrole.PublishingRoleSeries;
import edu.mayo.ontology.taxonomies.kmdo.relatedconcept.RelatedConcept;
import edu.mayo.ontology.taxonomies.kmdo.relatedconcept.RelatedConceptSeries;
import edu.mayo.ontology.taxonomies.kmdo.semanticannotationreltype.SemanticAnnotationRelType;
import edu.mayo.ontology.taxonomies.kmdo.semanticannotationreltype.SemanticAnnotationRelTypeSeries;
import edu.mayo.ontology.taxonomies.ws.mimetype.MIMEType;
import edu.mayo.ontology.taxonomies.ws.mimetype.MIMETypeSeries;
import edu.mayo.ontology.taxonomies.ws.responsecodes.ResponseCode;
import edu.mayo.ontology.taxonomies.ws.responsecodes.ResponseCodeSeries;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.taxonomy.dependencyreltype.DependencyType;
import org.omg.spec.api4kp._20200801.taxonomy.dependencyreltype.DependencyTypeSeries;
import org.omg.spec.api4kp._20200801.taxonomy.derivationreltype.DerivationType;
import org.omg.spec.api4kp._20200801.taxonomy.derivationreltype.DerivationTypeSeries;
import org.omg.spec.api4kp._20200801.taxonomy.iso639_2_languagecode.Language;
import org.omg.spec.api4kp._20200801.taxonomy.iso639_2_languagecode.LanguageSeries;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeartifactcategory.KnowledgeArtifactCategory;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeartifactcategory.KnowledgeArtifactCategorySeries;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetcategory.KnowledgeAssetCategory;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetcategory.KnowledgeAssetCategorySeries;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetrole.KnowledgeAssetRole;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetrole.KnowledgeAssetRoleSeries;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetType;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeoperation.KnowledgeProcessingOperation;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeoperation.KnowledgeProcessingOperationSeries;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeprocessingtechnique.KnowledgeProcessingTechnique;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeprocessingtechnique.KnowledgeProcessingTechniqueSeries;
import org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormat;
import org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries;
import org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguage;
import org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries;
import org.omg.spec.api4kp._20200801.taxonomy.krprofile.KnowledgeRepresentationLanguageProfile;
import org.omg.spec.api4kp._20200801.taxonomy.krprofile.KnowledgeRepresentationLanguageProfileSeries;
import org.omg.spec.api4kp._20200801.taxonomy.krserialization.KnowledgeRepresentationLanguageSerialization;
import org.omg.spec.api4kp._20200801.taxonomy.krserialization.KnowledgeRepresentationLanguageSerializationSeries;
import org.omg.spec.api4kp._20200801.taxonomy.languagerole.KnowledgeRepresentationLanguageRole;
import org.omg.spec.api4kp._20200801.taxonomy.languagerole.KnowledgeRepresentationLanguageRoleSeries;
import org.omg.spec.api4kp._20200801.taxonomy.lexicon.Lexicon;
import org.omg.spec.api4kp._20200801.taxonomy.lexicon.LexiconSeries;
import org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevel;
import org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries;
import org.omg.spec.api4kp._20200801.taxonomy.publicationstatus.PublicationStatus;
import org.omg.spec.api4kp._20200801.taxonomy.publicationstatus.PublicationStatusSeries;
import org.omg.spec.api4kp._20200801.taxonomy.relatedversiontype.RelatedVersionType;
import org.omg.spec.api4kp._20200801.taxonomy.relatedversiontype.RelatedVersionTypeSeries;
import org.omg.spec.api4kp._20200801.taxonomy.structuralreltype.StructuralPartType;
import org.omg.spec.api4kp._20200801.taxonomy.structuralreltype.StructuralPartTypeSeries;
import org.omg.spec.api4kp._20200801.taxonomy.summaryreltype.SummarizationType;
import org.omg.spec.api4kp._20200801.taxonomy.summaryreltype.SummarizationTypeSeries;
import org.omg.spec.api4kp._20200801.taxonomy.variantreltype.VariantType;
import org.omg.spec.api4kp._20200801.taxonomy.variantreltype.VariantTypeSeries;

class KnownVersionsTest {

  @Test
  void testKRLanguageVersions() {
    // build only '_20210401' until the list and the URIs can be standardized
    assertEquals(1, KnowledgeRepresentationLanguageSeries.schemeVersions.size());
    KnowledgeRepresentationLanguage lang
        = org.omg.spec.api4kp._20200801.taxonomy.krlanguage._20210401.KnowledgeRepresentationLanguage.BPMN_2_0;
    assertNotNull(lang);
  }

  @Test
  void testKRProfileVersions() {
    // build only '_20210401' until the list and the URIs can be standardized
    assertEquals(1, KnowledgeRepresentationLanguageProfileSeries.schemeVersions.size());
    KnowledgeRepresentationLanguageProfile prof
        = org.omg.spec.api4kp._20200801.taxonomy.krprofile._20210401.KnowledgeRepresentationLanguageProfile.OWL2_EL;
    assertNotNull(prof);
  }

  @Test
  void testKRMetaFormats() {
    // build only '_20210401' until the list and the URIs can be standardized
    assertEquals(1, SerializationFormatSeries.schemeVersions.size());
    SerializationFormat fmt
        = org.omg.spec.api4kp._20200801.taxonomy.krformat._20210401.SerializationFormat.XML_1_1;
    assertNotNull(fmt);
  }

  @Test
  void testKRSerializations() {
    // build only '_20210401' until the list and the URIs can be standardized
    assertEquals(1, KnowledgeRepresentationLanguageSerializationSeries.schemeVersions.size());
    KnowledgeRepresentationLanguageSerialization serial
        = org.omg.spec.api4kp._20200801.taxonomy.krserialization._20210401.KnowledgeRepresentationLanguageSerialization.DMN_1_1_XML_Syntax;
    assertNotNull(serial);
  }

  @Test
  void testLexica() {
    // build only '_20210401' until the list and the URIs can be standardized
    assertEquals(1, LexiconSeries.schemeVersions.size());
    Lexicon lex
        = org.omg.spec.api4kp._20200801.taxonomy.lexicon._20210401.Lexicon.RxNORM;
    assertNotNull(lex);
  }


  @Test
  void testLang2Codes() {
    // version 20190201
    assertEquals(1, LanguageSeries.schemeVersions.size());
    Language lang
        = org.omg.spec.api4kp._20200801.taxonomy.iso639_2_languagecode._20190201.Language.Italian;
    assertNotNull(lang);
  }

  @Test
  void testLang1Codes() {
    // version 20190201
    assertEquals(1, org.omg.spec.api4kp._20200801.taxonomy.iso639_1_languagecode.LanguageSeries.schemeVersions.size());
    org.omg.spec.api4kp._20200801.taxonomy.iso639_1_languagecode.Language lang
        = org.omg.spec.api4kp._20200801.taxonomy.iso639_1_languagecode._20190201.Language.Italian;
    assertNotNull(lang);
  }

  @Test
  void testDerivTypes() {
    // version _20210401
    assertEquals(1, DerivationTypeSeries.schemeVersions.size());
    DerivationType d2
        = org.omg.spec.api4kp._20200801.taxonomy.derivationreltype._20210401.DerivationType.Is_Derived_From;
    assertNotNull(d2);
  }

  @Test
  void testVariantType() {
    // version _20210401
    assertEquals(1, VariantTypeSeries.schemeVersions.size());
     VariantType v2
        = org.omg.spec.api4kp._20200801.taxonomy.variantreltype._20210401.VariantType.Is_Translation_Of;
    assertNotNull(v2);
  }

  @Test
  void testSummaryType() {
    // version _20210401
    assertEquals(1, SummarizationTypeSeries.schemeVersions.size());
    SummarizationType s2
        = org.omg.spec.api4kp._20200801.taxonomy.summaryreltype._20210401.SummarizationType.Abbreviates;
    assertNotNull(s2);
  }

  @Test
  void testDependencyType() {
    // version _20210401
    assertEquals(1, DependencyTypeSeries.schemeVersions.size());
    DependencyType d2
        = org.omg.spec.api4kp._20200801.taxonomy.dependencyreltype._20210401.DependencyType.Depends_On;
    assertNotNull(d2);
  }

  @Test
  void testSeriesType() {
    // version _20210401
    assertEquals(1, RelatedVersionTypeSeries.schemeVersions.size());
    RelatedVersionType v2
        = org.omg.spec.api4kp._20200801.taxonomy.relatedversiontype._20210401.RelatedVersionType.Has_Next_Version;
    assertNotNull(v2);
  }

  @Test
  void testStructType() {
    // version _20210401
    assertEquals(1, StructuralPartTypeSeries.schemeVersions.size());
    StructuralPartType p2
        = org.omg.spec.api4kp._20200801.taxonomy.structuralreltype._20210401.StructuralPartType.Has_Proper_Part;
    assertNotNull(p2);
  }

  @Test
  void testCito() {
    // version 2018-02-16
    assertEquals(1, BibliographicCitationTypeSeries.schemeVersions.size());
    BibliographicCitationType bib1
        = edu.mayo.ontology.taxonomies.kmdo.citationreltype._2018_02_16.BibliographicCitationType.Cites;
    assertNotNull(bib1);
  }

  @Test
  void testPublishRole() {
    // version 2017-09-04
    assertEquals(1, PublishingRoleSeries.schemeVersions.size());
    PublishingRole pubrol
        = edu.mayo.ontology.taxonomies.kmdo.publishingrole._2017_09_04.PublishingRole.Author;
    assertNotNull(pubrol);
  }

  @Test
  void testPublishStatus() {
    // version 2014-02-01
    assertEquals(1, PublicationStatusSeries.schemeVersions.size());
    PublicationStatus stat
        = org.omg.spec.api4kp._20200801.taxonomy.publicationstatus._2014_02_01.PublicationStatus.Archived;
    assertNotNull(stat);
  }

  @Test
  void testPublishEvent() {
    // version _20210401 only until standardized
    assertEquals(1, PublicationEventTypeSeries.schemeVersions.size());
    PublicationEventType evn
        = edu.mayo.ontology.taxonomies.kmdo.publicationeventtype._20210401.PublicationEventType.Creation;
    assertNotNull(evn);
  }

  @Test
  void testSemAnnotation() {
    // version _20210401 only until standardized
    assertEquals(1, SemanticAnnotationRelTypeSeries.schemeVersions.size());
    SemanticAnnotationRelType anno
        = edu.mayo.ontology.taxonomies.kmdo.semanticannotationreltype._20210401.SemanticAnnotationRelType.Is_About;
    assertNotNull(anno);
  }

  @Test
  void testMIMETypes() {
    // 1.0.1
    assertEquals(1, MIMETypeSeries.schemeVersions.size());
    MIMEType mime
        = edu.mayo.ontology.taxonomies.ws.mimetype._1_0_1.MIMEType.Application_Atomxml;
    assertNotNull(mime);
  }

  @Test
  void testAssetCategory() {
    // legacy 2019
    assertEquals(1, KnowledgeAssetCategorySeries.schemeVersions.size());
    KnowledgeAssetCategory kac
        = org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetcategory._20210401.KnowledgeAssetCategory.Rules_Policies_And_Guidelines;
    assertNotNull(kac);
  }

  @Test
  void testAssetRole() {
    // legacy 2019
    assertEquals(1, KnowledgeAssetRoleSeries.schemeVersions.size());
    KnowledgeAssetRole rol
        = org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetrole._20210401.KnowledgeAssetRole.Operational_Concept_Definition;
    assertNotNull(rol);
  }

  @Test
  void testAssetType() {
    // legacy 2019
    assertEquals(1, KnowledgeAssetTypeSeries.schemeVersions.size());
    KnowledgeAssetType type
        = org.omg.spec.api4kp._20200801.taxonomy.clinicalknowledgeassettype._20210401.ClinicalKnowledgeAssetType.Care_Process_Model;
    assertNotNull(type);
  }

  @Test
  void testArtifactCategory() {
    // 2020-01-20
    assertEquals(1, KnowledgeArtifactCategorySeries.schemeVersions.size());
    KnowledgeArtifactCategory type
        = org.omg.spec.api4kp._20200801.taxonomy.knowledgeartifactcategory._2020_01_20.KnowledgeArtifactCategory.Software;
    assertNotNull(type);
  }

  @Test
  void testProcessingTechnique() {
    // version _20210401
    assertEquals(1, KnowledgeProcessingTechniqueSeries.schemeVersions.size());
    KnowledgeProcessingTechnique t2
        = org.omg.spec.api4kp._20200801.taxonomy.knowledgeprocessingtechnique._20210401.KnowledgeProcessingTechnique.Qualitative_Technique;
    assertNotNull(t2);
  }

  @Test
  void testRelatedConcept() {
    // SKOS - not in a series
    assertEquals(1, RelatedConceptSeries.schemeVersions.size());
    RelatedConcept rel
        = edu.mayo.ontology.taxonomies.kmdo.relatedconcept.RelatedConcept.Has_Broader;
    assertNotNull(rel);
  }

  @Test
  void testLanguageRole() {
    // version _20210401
    assertEquals(1, KnowledgeRepresentationLanguageRoleSeries.schemeVersions.size());
    KnowledgeRepresentationLanguageRole r2
        = org.omg.spec.api4kp._20200801.taxonomy.languagerole._20210401.KnowledgeRepresentationLanguageRole.Annotation_Language;
    assertNotNull(r2);
  }

  @Test
  void testOperations() {
    // version _20210401
    assertEquals(1, KnowledgeProcessingOperationSeries.schemeVersions.size());
    KnowledgeProcessingOperation op2
        = org.omg.spec.api4kp._20200801.taxonomy.knowledgeoperation._20210401.KnowledgeProcessingOperation.Cherry_Picking_Task;
    assertNotNull(op2);
  }

  @Test
  void testParsingLevel() {
    // version _20210401
    assertEquals(1, ParsingLevelSeries.schemeVersions.size());
    ParsingLevel pl2
        = org.omg.spec.api4kp._20200801.taxonomy.parsinglevel._20210401.ParsingLevel.Concrete_Knowledge_Expression;
    assertNotNull(pl2);
  }

  @Test
  void testResponseCode() {
    // version 2011
    assertEquals(1, ResponseCodeSeries.schemeVersions.size());
    ResponseCode rc
        = edu.mayo.ontology.taxonomies.ws.responsecodes._2011.ResponseCode.OK;
    assertNotNull(rc);
  }

  @Test
  void testCCG() {
    // _20210401 until published
    assertEquals(1, ConceptDefinitionTypeSeries.schemeVersions.size());
    ConceptDefinitionType ct
        = edu.mayo.ontology.taxonomies.kao.ccgentries._20210401.ConceptDefinitionType.Human_Resolution_Concept_Definition;
    assertNotNull(ct);
  }

  @Test
  void testDMN() {
    // _20210401 until published
    assertEquals(1, DecisionTypeSeries.schemeVersions.size());
    DecisionType dt
        = edu.mayo.ontology.taxonomies.kao.decisiontype._20210401.DecisionType.Assessment_Decision;
    assertNotNull(dt);
  }


}
