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
package edu.mayo.kmdp;


import static edu.mayo.kmdp.id.helper.DatatypeHelper.getVersionSeparator;

import edu.mayo.kmdp.id.helper.DatatypeHelper;
import edu.mayo.kmdp.metadata.annotations.Annotation;
import edu.mayo.kmdp.metadata.annotations.BasicAnnotation;
import edu.mayo.kmdp.metadata.annotations.ComplexAnnotation;
import edu.mayo.kmdp.metadata.annotations.DatatypeAnnotation;
import edu.mayo.kmdp.metadata.annotations.MultiwordAnnotation;
import edu.mayo.kmdp.metadata.annotations.SimpleAnnotation;
import edu.mayo.kmdp.metadata.surrogate.Association;
import edu.mayo.kmdp.metadata.surrogate.ComputableKnowledgeArtifact;
import edu.mayo.kmdp.metadata.surrogate.Dependency;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeArtifact;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeResource;
import edu.mayo.kmdp.metadata.surrogate.Representation;
import edu.mayo.kmdp.registry.Registry;
import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.StreamUtil;
import edu.mayo.kmdp.util.URIUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.kmdp.util.XMLUtil;
import edu.mayo.ontology.taxonomies.kao.languagerole.KnowledgeRepresentationLanguageRole;
import edu.mayo.ontology.taxonomies.kao.rel.dependencyreltype.DependencyTypeSeries;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.validation.Schema;
import org.omg.spec.api4kp._1_0.id.ResourceIdentifier;
import org.omg.spec.api4kp._1_0.id.SemanticIdentifier;
import org.omg.spec.api4kp._1_0.id.Term;
import org.omg.spec.api4kp._1_0.id.VersionIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;
import org.w3c.dom.Node;

public class SurrogateHelper {

  protected SurrogateHelper() {

  }

  public static Optional<Schema> getSchema() {
    return XMLUtil.getSchemas(
        SurrogateHelper.class.getResource("/xsd/metadata/surrogate/surrogate.xsd"),
        XMLUtil.catalogResolver("/xsd/km-metadata-catalog.xml", "/xsd/terms-catalog.xml"));
  }


  public static Annotation unmarshallAnnotation(Node node) {
    Class<? extends Annotation> annoType;
    switch (node.getLocalName()) {
      case "complexAnnotation":
        annoType = ComplexAnnotation.class;
        break;
      case "multiwordAnnotation":
        annoType = MultiwordAnnotation.class;
        break;
      case "simpleAnnotation":
        annoType = SimpleAnnotation.class;
        break;
      case "basicAnnotation":
        annoType = BasicAnnotation.class;
        break;
      case "datatypeAnnotation":
        annoType = DatatypeAnnotation.class;
        break;
      default:
        throw new IllegalStateException("Unrecognized Annotation " + node.getLocalName());
    }
    return JaxbUtil.unmarshall(Annotation.class,
        annoType,
        node)
        .orElseThrow(IllegalStateException::new);
  }

  public static Annotation rootToFragment(Annotation anno) {
    Class<? extends Annotation> annoType = anno.getClass();
    if (annoType.getPackage()
        .equals(edu.mayo.kmdp.metadata.annotations.resources.ObjectFactory.class.getPackage())) {
      if (annoType.equals(edu.mayo.kmdp.metadata.annotations.resources.SimpleAnnotation.class)) {
        return (Annotation) anno.copyTo(new SimpleAnnotation());
      } else if (annoType
          .equals(edu.mayo.kmdp.metadata.annotations.resources.MultiwordAnnotation.class)) {
        return (Annotation) anno.copyTo(new MultiwordAnnotation());
      } else if (annoType
          .equals(edu.mayo.kmdp.metadata.annotations.resources.ComplexAnnotation.class)) {
        return (Annotation) anno.copyTo(new ComplexAnnotation());
      } else if (annoType
          .equals(edu.mayo.kmdp.metadata.annotations.resources.BasicAnnotation.class)) {
        return (Annotation) anno.copyTo(new BasicAnnotation());
      } else if (annoType
          .equals(edu.mayo.kmdp.metadata.annotations.resources.DatatypeAnnotation.class)) {
        return (Annotation) anno.copyTo(new DatatypeAnnotation());
      }
    }
    return anno;
  }

  private static final Set<DependencyTypeSeries> TRAVERSE_DEPS = Util
      .newEnumSet(Arrays.asList(
          DependencyTypeSeries.Imports,
          DependencyTypeSeries.Includes,
          DependencyTypeSeries.Depends_On),
          DependencyTypeSeries.class);


  public static Set<KnowledgeAsset> closure(KnowledgeAsset resource, boolean includeSelf) {
    return closure(resource, includeSelf,
        (assetId, vTag) -> Optional.empty());
  }

  public static Set<KnowledgeAsset> closure(KnowledgeAsset resource,
      BiFunction<UUID, String, Optional<KnowledgeAsset>> resolver) {
    return closure(resource, true, resolver);
  }

  public static Set<KnowledgeAsset> closure(KnowledgeAsset resource, boolean includeSelf,
      BiFunction<UUID, String, Optional<KnowledgeAsset>> resolver) {
    HashSet<KnowledgeAsset> closure = new HashSet<>();
    closure(resource, closure, resolver);
    if (includeSelf) {
      closure.add(resource);
    }
    return closure;
  }

  private static void closure(KnowledgeAsset resource, HashSet<KnowledgeAsset> collector,
      BiFunction<UUID, String, Optional<KnowledgeAsset>> resolver) {
    Set<KnowledgeAsset> deps = dependencies(resource, resolver);
    for (KnowledgeAsset dep : deps) {
      if (!collector.contains(dep)) {
        collector.add(dep);
        closure(dep, collector, resolver);
      }
    }
  }

  private static Set<KnowledgeAsset> dependencies(KnowledgeResource resource,
      BiFunction<UUID, String, Optional<KnowledgeAsset>> resolver) {
    return resource.getRelated().stream()
        .filter(Dependency.class::isInstance)
        .map(Dependency.class::cast)
        .filter(dependency -> TRAVERSE_DEPS.contains(dependency.getRel().asEnum()))
        .map(Association::getTgt)
        .map(edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset.class::cast)
        .map(x -> {
          VersionIdentifier vid = DatatypeHelper.toVersionIdentifier(x.getAssetId().getVersionId());
          return resolver.apply(UUID.fromString(vid.getTag()), vid.getVersionTag())
              .orElse(x);
        }).collect(Collectors.toSet());
  }

  public static Optional<ConceptIdentifier> getSimpleAnnotationValue(KnowledgeAsset asset,
      ConceptIdentifier rel) {
    return asset.getSubject().stream()
        .flatMap(StreamUtil.filterAs(SimpleAnnotation.class))
        .filter(ann -> rel == null || isSame(rel,ann.getRel()))
        .map(SimpleAnnotation::getExpr)
        .findAny();
  }


  public static Optional<URIIdentifier> getIdentifier(KnowledgeResource knowledgeResource) {
    if (knowledgeResource instanceof KnowledgeAsset) {
      return Optional.of(((KnowledgeAsset) knowledgeResource).getAssetId());
    } else if (knowledgeResource instanceof KnowledgeArtifact) {
      return Optional.of(((KnowledgeArtifact) knowledgeResource).getArtifactId());
    } else {
      return Optional.empty();
    }
  }

  public static Representation canonicalRepresentationOf(KnowledgeAsset asset) {
    if (asset == null
        || asset.getCarriers().isEmpty()
        || !(asset.getCarriers().get(0) instanceof ComputableKnowledgeArtifact)) {
      return null;
    }
    ComputableKnowledgeArtifact artifact = (ComputableKnowledgeArtifact) asset.getCarriers().get(0);
    return artifact.getRepresentation();
  }

  public static Stream<Representation> expandRepresentation(
      Representation rep,
      KnowledgeRepresentationLanguageRole role) {
    if (rep == null) {
      return Stream.empty();
    }
    return Stream.concat(
        role == null ? Stream.of(rep) : Stream.empty(),
        rep.getWith() != null
            ? rep.getWith().stream()
            .filter(sub -> role == null || sub.getRole().isSame(role)
                || sub.getRole().hasAncestor(role))
            .flatMap(sub -> expandRepresentation(sub.getSubLanguage(), role))
            : Stream.empty());
  }

  public static Set<KnowledgeRepresentationLanguage> getSublanguages(Representation rep,
      KnowledgeRepresentationLanguageRole role) {
    return expandRepresentation(rep, role)
        .map(Representation::getLanguage)
        .collect(Collectors.toSet());
  }

  /**
   * Helper method that looks for any ComputableKnowledgeArtifact among the Artifacts
   * referenced in a Knowledge Asset Surrogate
   * @param surr  The Surrogate of the Asset for which a Computable Manifestation is requested
   * @return  Any Computable Variant of the asset, if present
   */
  public static Optional<ComputableKnowledgeArtifact> getComputableCarrier(
      KnowledgeAsset surr) {
    return surr.getCarriers().stream()
        .flatMap(StreamUtil.filterAs(ComputableKnowledgeArtifact.class))
        .findAny();
  }


  public static org.omg.spec.api4kp._1_0.id.ConceptIdentifier fromLegacyConceptIdentifier(
      ConceptIdentifier v) {
    if (v == null) {
      return null;
    }
    return new org.omg.spec.api4kp._1_0.id.ConceptIdentifier()
        .withUuid(v.getConceptUUID())
        .withResourceId(v.getConceptId())
        .withTag(v.getTag())
        .withNamespaceUri(v.getNamespace().getId())
        .withName(v.getLabel())
        .withReferentId(v.getRef());
  }

  public static ConceptIdentifier toLegacyConceptIdentifier(Term term) {
    return toLegacyConceptIdentifier(term.asConceptIdentifier());
  }

  public static ConceptIdentifier toLegacyConceptIdentifier(
      org.omg.spec.api4kp._1_0.id.ConceptIdentifier cId) {

    ResourceIdentifier nsId = SemanticIdentifier.newNamespaceId(cId.getNamespaceUri());

    return new ConceptIdentifier()
        .withConceptId(cId.getResourceId())
        .withTag(cId.getTag())
        .withLabel(cId.getName())
        .withRef(cId.getReferentId())
        .withConceptUUID(cId.getUuid())
        .withNamespace(
            new NamespaceIdentifier()
                .withId(nsId.getResourceId())
                .withLabel(nsId.getName())
                .withTag(nsId.getTag())
                .withVersion(cId.getVersionTag())
                .withEstablishedOn(nsId.getEstablishedOn())
        );
  }

  public static URIIdentifier uri(final String id, final String versionTag) {
    return new URIIdentifier()
        .withUri(URI.create(id))
        .withVersionId(
            Util.isEmpty(versionTag) ? null : URI.create(id + getVersionSeparator(id) + versionTag));
  }

  public static String getTag(URIIdentifier id) {
    return URIUtil.detectLocalName(id.getUri());
  }

  public static String getVersionTag(URIIdentifier id) {
    return DatatypeHelper.toVersionIdentifier(id.getVersionId()).getVersionTag();
  }

  private static boolean isSame(ConceptIdentifier c1, ConceptIdentifier c2) {
    return c1 != null && c2 != null
        && c1.getConceptUUID().equals(c2.getConceptUUID());
  }

  public static URIIdentifier toURIIdentifier(SemanticIdentifier semId) {
    return new URIIdentifier()
        .withUri(semId.getResourceId())
        .withVersionId(semId.getVersionId());
  }

  public static ResourceIdentifier fromURIIdentifier(URIIdentifier assetId) {
    URI uri = assetId.getUri();
    if ("urn".equals(uri.getScheme())) {
      return SemanticIdentifier.newId(
          Registry.BASE_UUID_URN_URI, URIUtil.detectLocalName(uri), getVersionTag(assetId));
    } else {
      String localTag = URIUtil.detectLocalName(uri);
      URI ns = URIUtil.detectNamespace(uri);
      return SemanticIdentifier.newId(
          ns, localTag, getVersionTag(assetId));
    }

  }
}