/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.omg.spec.api4kp._20200801.surrogate;

import static edu.mayo.kmdp.registry.Registry.BASE_UUID_URN_URI;
import static edu.mayo.kmdp.registry.Registry.mapAssetToArtifactNamespace;
import static edu.mayo.kmdp.util.Util.ensureUUID;
import static edu.mayo.kmdp.util.Util.uuid;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp._20200801.id.IdentifierConstants.VERSION_LATEST;
import static org.omg.spec.api4kp._20200801.id.IdentifierConstants.VERSION_ZERO;
import static org.omg.spec.api4kp._20200801.id.SemanticIdentifier.newId;
import static org.omg.spec.api4kp._20200801.surrogate.SurrogateHelper.getCanonicalSurrogateId;
import static org.omg.spec.api4kp._20200801.surrogate.SurrogateHelper.getComputableSurrogateMetadata;
import static org.omg.spec.api4kp._20200801.taxonomy.clinicalknowledgeassettype.ClinicalKnowledgeAssetTypeSeries.Patient_Cohort_Definition;
import static org.omg.spec.api4kp._20200801.taxonomy.iso639_2_languagecode.LanguageSeries.English;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetcategory.KnowledgeAssetCategorySeries.Assessment_Predictive_And_Inferential_Models;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetcategory.KnowledgeAssetCategorySeries.Rules_Policies_And_Guidelines;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetcategory.KnowledgeAssetCategorySeries.Terminology_Ontology_And_Assertional_KBs;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetrole.KnowledgeAssetRoleSeries.Operational_Concept_Definition;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries.Computable_Decision_Model;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries.Functional_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries.Grounded_Knowledge;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries.Inquiry_Specification;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries.Service_Specification;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries.Value_Set;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.JSON;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.RDF_1_1;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.TXT;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.XML_1_1;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.YAML_1_2;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_1;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.FHIRPath_STU1;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.FHIR_STU3;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.HL7_CQL_1_3;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.HTML;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate_2_0;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.OWL_2;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.OpenAPI_2_X;
import static org.omg.spec.api4kp._20200801.taxonomy.krprofile.KnowledgeRepresentationLanguageProfileSeries.OWL2_DL;
import static org.omg.spec.api4kp._20200801.taxonomy.languagerole.KnowledgeRepresentationLanguageRoleSeries.Schema_Language;
import static org.omg.spec.api4kp._20200801.taxonomy.lexicon.LexiconSeries.SKOS;
import static org.omg.spec.api4kp._20200801.taxonomy.structuralreltype.StructuralPartTypeSeries.Has_Structuring_Component;

import edu.mayo.ontology.taxonomies.kmdo.semanticannotationreltype.SemanticAnnotationRelTypeSeries;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.UUID;
import org.omg.spec.api4kp._20200801.AbstractCarrier.Encodings;
import org.omg.spec.api4kp._20200801.id.IdentifierConstants;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.id.VersionIdentifier;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.taxonomy.dependencyreltype.DependencyType;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetcategory.KnowledgeAssetCategory;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetType;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeprocessingtechnique.KnowledgeProcessingTechniqueSeries;
import org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormat;
import org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguage;
import org.omg.spec.api4kp._20200801.taxonomy.lexicon.Lexicon;
import org.omg.spec.api4kp._20200801.taxonomy.publicationstatus.PublicationStatus;

public class SurrogateBuilder {

  private final KnowledgeAsset surrogate;

  protected SurrogateBuilder(ResourceIdentifier assetId, boolean root) {
    this.surrogate = newInstance(root)
        .withAssetId(assetId);
  }

  protected KnowledgeAsset newInstance(boolean root) {
    return root
        ? new org.omg.spec.api4kp._20200801.surrogate.resources.KnowledgeAsset()
        : new KnowledgeAsset();
  }

  public static SurrogateBuilder newRandomSurrogate() {
    return newSurrogate(randomAssetId(), true);
  }

  public static SurrogateBuilder newSurrogate(UUID id) {
    return newSurrogate(
        assetId(BASE_UUID_URN_URI, id, VERSION_ZERO));
  }

  public static SurrogateBuilder newSurrogate(ResourceIdentifier assetId) {
    return newSurrogate(assetId, true);
  }

  public static SurrogateBuilder newSurrogate(ResourceIdentifier assetId, boolean root) {
    return new SurrogateBuilder(assetId, root)
        .withCanonicalSurrogate();
  }

  public static KnowledgeArtifact addCanonicalSurrogateMetadata(
      KnowledgeAsset original,
      KnowledgeRepresentationLanguage surrogateLanguage,
      SerializationFormat surrogateFormat) {
    var assetId = original.getAssetId();

    var surrogateDescr = new KnowledgeArtifact()
        .withArtifactId(artifactId(
            mapAssetToArtifactNamespace(assetId.getNamespaceUri()),
            defaultSurrogateUUID(assetId, surrogateLanguage),
            VERSION_ZERO
        ))
        .withLocalization(English)
        .withRepresentation(
            rep(surrogateLanguage, surrogateFormat, Charset.defaultCharset(), Encodings.DEFAULT));

    original.withSurrogate(surrogateDescr);

    return surrogateDescr;
  }

  /**
   * Builds a predictable Carrier Artifact UUID for a specific version of an asset. Uses a
   * combination of the Asset version ID and the language used to express the Carrier (consequence:
   * any 'vertical' lifting/lowering of the Carrier does not impact its identity)
   *
   * @param assetId
   * @param lang
   * @return
   */
  public static UUID defaultCarrierUUID(ResourceIdentifier assetId,
      KnowledgeRepresentationLanguage lang) {
    String key = assetId.getVersionId().toString();
    key += IdentifierConstants.CARRIERS + lang.getUuid();
    return UUID.nameUUIDFromBytes(key.getBytes());
  }


  public SurrogateBuilder withName(String name, String descr) {
    surrogate.withName(name)
        .withDescription(descr);
    return this;
  }

  public SurrogateBuilder withValuesetType() {
    get().withFormalCategory(Terminology_Ontology_And_Assertional_KBs)
        .withFormalType(Value_Set);
    return this;
  }

  public SurrogateBuilder withServiceType() {
    get().withFormalCategory(Rules_Policies_And_Guidelines)
        .withFormalType(Service_Specification);
    return this;
  }

  public SurrogateBuilder withCohortDefinitionType() {
    get().withFormalCategory(
            Assessment_Predictive_And_Inferential_Models)
        .withFormalType(Patient_Cohort_Definition);
    return this;
  }

  public SurrogateBuilder withQueryType() {
    get().withFormalCategory(Rules_Policies_And_Guidelines)
        .withFormalType(Inquiry_Specification);
    return this;
  }

  public SurrogateBuilder withContentType() {
    get().withFormalCategory(Terminology_Ontology_And_Assertional_KBs)
        .withFormalType(Grounded_Knowledge);
    return this;
  }

  public SurrogateBuilder withExpressionType() {
    get().withFormalCategory(
            Assessment_Predictive_And_Inferential_Models)
        .withFormalType(Functional_Expression);
    return this;
  }

  public SurrogateBuilder withFormalType(KnowledgeAssetCategory cat, KnowledgeAssetType type) {
    get().withFormalCategory(cat)
        .withFormalType(type);
    return this;
  }

  public SurrogateBuilder withDecisionAidType() {
    get().withFormalCategory(
            Assessment_Predictive_And_Inferential_Models)
        .withFormalType(Computable_Decision_Model);
    return this;
  }


  public SurrogateBuilder aaS() {
    get().withProcessingMethod(KnowledgeProcessingTechniqueSeries.Computational_Technique);
    return this;
  }

  public SurrogateBuilder withApplicability(Term t) {
    if (t == null) {
      return this;
    }
    get().withApplicableIn(new Applicability()
        .withSituation(t.asConceptIdentifier()));
    return this;
  }


  public SurrogateBuilder asOperationalDefinition(Term subject, Term proposition,
      Term... inputs) {
    get().withRole(Operational_Concept_Definition);

    if (proposition != null) {
      this.withAnnotation(SemanticAnnotationRelTypeSeries.Defines.asConceptIdentifier(),
          proposition.asConceptIdentifier());
    }

    if (subject != null) {
      this.withAnnotation(SemanticAnnotationRelTypeSeries.Has_Primary_Subject.asConceptIdentifier(),
          subject.asConceptIdentifier());
    }

    Arrays.stream(inputs).forEach(input ->
        this.withAnnotation(SemanticAnnotationRelTypeSeries.In_Terms_Of.asConceptIdentifier(),
            input.asConceptIdentifier())
    );

    return this;
  }


  public SurrogateBuilder withDMNExpression(KnowledgeRepresentationLanguage schema) {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(
          new KnowledgeArtifact()
              .withRepresentation(new SyntacticRepresentation()
                  .withLanguage(DMN_1_1)
                  .withFormat(XML_1_1)
                  .withSubLanguage(new SyntacticRepresentation()
                      .withRole(Schema_Language)
                      .withLanguage(schema))));
    }
    return this;
  }


  public SurrogateBuilder withOpenAPIExpression() {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(
          new KnowledgeArtifact()
              .withRepresentation(new SyntacticRepresentation()
                  .withLanguage(OpenAPI_2_X)
                  .withFormat(YAML_1_2)));
    }
    return this;
  }

  public SurrogateBuilder withHTMLExpression() {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(new KnowledgeArtifact()
          .withLocalization(English)
          .withRepresentation(new SyntacticRepresentation()
              .withLanguage(HTML)
              .withFormat(TXT)));
    }
    return this;
  }

  public SurrogateBuilder withSKOSExpression() {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(new KnowledgeArtifact()
          .withRepresentation(new SyntacticRepresentation()
              .withLanguage(OWL_2)
              .withFormat(RDF_1_1)
              .withProfile(OWL2_DL)
              .withLexicon(SKOS)));
    }
    return this;
  }


  public SurrogateBuilder withCQLExpression(Lexicon lex) {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(new KnowledgeArtifact()
          .withRepresentation(new SyntacticRepresentation()
              .withLanguage(HL7_CQL_1_3)
              .withFormat(TXT)
              .withLexicon(lex)));
    }
    return this;
  }

  public SurrogateBuilder withInlinedFhirPath(String expr) {
    return withInlinedFhirPath(expr, FHIR_STU3);
  }

  public static ResourceIdentifier defaultSurrogateId(
      URI baseArtifactNamespace,
      ResourceIdentifier assetId,
      KnowledgeRepresentationLanguage lang) {
    return artifactId(baseArtifactNamespace, defaultSurrogateUUID(assetId, lang), VERSION_ZERO);
  }


  public SurrogateBuilder withRepresentation(KnowledgeRepresentationLanguage lang,
      SerializationFormat format) {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(new KnowledgeArtifact()
          .withRepresentation(new SyntacticRepresentation()
              .withLanguage(lang)
              .withFormat(format)));
    }
    return this;
  }


  public SurrogateBuilder withCarriers(ResourceIdentifier id, URI loc) {
    if (get().getCarriers().isEmpty()) {
      KnowledgeArtifact carrier = new KnowledgeArtifact()
          .withArtifactId(id)
          .withLocator(loc);
      get().withCarriers(carrier);
    } else {
      get().getCarriers().get(0)
          .withArtifactId(id)
          .withLocator(loc);
    }

    return this;
  }

  public SurrogateBuilder withAnnotation(Term rel, Term obj) {
    get().withAnnotation(new Annotation()
        .withRel(rel.asConceptIdentifier())
        .withRef(obj.asConceptIdentifier()));
    return this;
  }

  public SurrogateBuilder withDependency(DependencyType rel, ResourceIdentifier relatedAsset) {
    get().withLinks(
        new Dependency()
            .withRel(rel)
            .withHref(relatedAsset));
    return this;
  }

  public SurrogateBuilder withPublicationStatus(PublicationStatus status) {
    get().withLifecycle(
        new Publication()
            .withPublicationStatus(status));
    return this;
  }


  public KnowledgeAsset get() {
    return surrogate;
  }

  /**
   * Builds a predictable Surrogate UUID for a specific version of an asset.
   * Uses a combination of the Asset version ID and the language used to express the Surrogate
   * (consequence: any 'vertical' lifting/lowering of the Surrogate does not impact its identity)
   * @param assetId
   * @param lang
   * @return
   */
  public static UUID defaultSurrogateUUID(ResourceIdentifier assetId,
      KnowledgeRepresentationLanguage lang) {
    String key = assetId.getVersionId().toString();
    key += IdentifierConstants.SURROGATES + lang.getUuid();
    return UUID.nameUUIDFromBytes(key.getBytes());
  }

  /**
   * Builds a predictable Structure UUID for a specific version of a (Composite) asset.
   * Uses a combination of the Asset version ID and the language used to express the Surrogate
   * @param assetId
   * @return
   */
  public static UUID defaultStructureAssetUUID(ResourceIdentifier assetId) {
    // TODO define an asset type for 'Structs'
    return defaultComponentAssetUUID(assetId, Has_Structuring_Component);
  }

  /**
   * Builds a predictable Structure UUID for a specific Component of a Composite Asset
   * Uses a combination of the Asset version ID and the Type of the Component
   * @param assetId
   * @return
   */
  public static UUID defaultComponentAssetUUID(ResourceIdentifier assetId, Term type) {
    String key = assetId.getVersionId().toString();
    key += type.getUuid();
    return UUID.nameUUIDFromBytes(key.getBytes());
  }

  /**
   * Updates the version of 'self' as a Surrogate, incrementing the MINOR version number
   * <p>
   * Retrieves the KnowledgeAsset.surrogates['self'], and increments the artifactId version number
   * <p>
   * Used in conjunction with incremental changes to the Surrogate object itself.
   *
   * @param surr The surrogate to update
   */
  public static ResourceIdentifier updateSurrogateVersion(KnowledgeAsset surr) {
    KnowledgeArtifact self = getCanonicalSurrogateId(surr)
        .flatMap(sid -> getComputableSurrogateMetadata(sid.getUuid(), sid.getVersionTag(), surr))
        .orElseThrow();

    ResourceIdentifier oldSurrogateId = self.getArtifactId();
    ResourceIdentifier newSurrogateId = newId(
        oldSurrogateId.getNamespaceUri(),
        oldSurrogateId.getUuid(),
        oldSurrogateId.getSemanticVersionTag().incrementMinorVersion().toString());
    self.setArtifactId(newSurrogateId);

    return newSurrogateId;
  }

  public static ResourceIdentifier assetId(URI baseNamespace, UUID assetId) {
    return SemanticIdentifier.newId(baseNamespace, assetId);
  }

  public static ResourceIdentifier assetId(URI baseNamespace, String uuid, String versionTag) {
    return assetId(baseNamespace, ensureUUID(uuid).orElseThrow(), versionTag);
  }

  public static ResourceIdentifier assetId(URI baseNamespace, UUID uuid, String versionTag) {
    return SemanticIdentifier.newId(
        baseNamespace,
        uuid,
        VersionIdentifier.toSemVer(versionTag));
  }

  public static ResourceIdentifier randomAssetId() {
    return assetId(BASE_UUID_URN_URI, UUID.randomUUID(), VERSION_ZERO);
  }

  public static ResourceIdentifier randomAssetId(URI baseNamespace) {
    return SemanticIdentifier.newId(baseNamespace, UUID.randomUUID(), VERSION_ZERO);
  }

  public static ResourceIdentifier defaultArtifactId(
      ResourceIdentifier assetId,
      KnowledgeRepresentationLanguage lang) {
    return defaultArtifactId(
        mapAssetToArtifactNamespace(assetId.getNamespaceUri()),
        assetId,
        lang);
  }

  public static ResourceIdentifier defaultArtifactId(
      URI baseArtifactNamespace,
      ResourceIdentifier assetId,
      KnowledgeRepresentationLanguage lang) {
    return defaultArtifactId(baseArtifactNamespace, assetId, lang, VERSION_ZERO);
  }

  public static ResourceIdentifier defaultArtifactId(
      ResourceIdentifier assetId,
      KnowledgeRepresentationLanguage lang,
      String versionTag) {
    return defaultArtifactId(
        mapAssetToArtifactNamespace(assetId.getNamespaceUri()),
        assetId,
        lang,
        versionTag);
  }

  public static ResourceIdentifier defaultArtifactId(
      URI baseArtifactNamespace,
      ResourceIdentifier assetId,
      KnowledgeRepresentationLanguage lang,
      String versionTag) {
    return artifactId(baseArtifactNamespace, defaultCarrierUUID(assetId, lang), versionTag);
  }

  private SurrogateBuilder withCanonicalSurrogate() {
    addCanonicalSurrogateMetadata(get(), Knowledge_Asset_Surrogate_2_0, JSON);
    return this;
  }

  public static ResourceIdentifier defaultSurrogateId(
      ResourceIdentifier assetId,
      KnowledgeRepresentationLanguage lang) {
    return defaultSurrogateId(
        mapAssetToArtifactNamespace(assetId.getNamespaceUri()),
        assetId,
        lang);
  }

  public static ResourceIdentifier defaultSurrogateId(
      ResourceIdentifier assetId,
      KnowledgeRepresentationLanguage lang,
      String versionTag) {
    return defaultSurrogateId(
        mapAssetToArtifactNamespace(assetId.getNamespaceUri()),
        assetId,
        lang,
        versionTag);
  }

  public static ResourceIdentifier defaultSurrogateId(
      URI baseArtifactNamespace,
      ResourceIdentifier assetId,
      KnowledgeRepresentationLanguage lang,
      String versionTag) {
    return artifactId(baseArtifactNamespace, defaultCarrierUUID(assetId, lang), versionTag);
  }

  public static ResourceIdentifier artifactId(URI baseNamespace, String uuid, String versionTag) {
    return artifactId(baseNamespace, ensureUUID(uuid).orElseThrow(), versionTag);
  }

  public static ResourceIdentifier artifactId(URI baseNamespace, UUID uuid, String versionTag) {
    return SemanticIdentifier.newId(
        baseNamespace,
        uuid,
        VersionIdentifier.toSemVer(versionTag));
  }

  public static ResourceIdentifier randomArtifactId() {
    return artifactId(BASE_UUID_URN_URI, UUID.randomUUID(), VERSION_ZERO);
  }

  public static ResourceIdentifier randomArtifactId(URI baseNamespace) {
    return SemanticIdentifier.newId(baseNamespace, UUID.randomUUID(), VERSION_ZERO);
  }

  public SurrogateBuilder withInlinedFhirPath(String expr,
      KnowledgeRepresentationLanguage schemaLanguage) {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(new KnowledgeArtifact()
          .withArtifactId(
              artifactId(
                  mapAssetToArtifactNamespace(get().getAssetId().getNamespaceUri()),
                  expr != null ? uuid(expr) : UUID.randomUUID(),
                  VERSION_LATEST))
          .withRepresentation(
              rep(FHIRPath_STU1, TXT, Charset.defaultCharset())
                  .withSubLanguage(rep(schemaLanguage).withRole(Schema_Language)))
          .withInlinedExpression(expr));
    }
    return this;
  }
}
