/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.mayo.kmdp.terms.generator;

import static edu.mayo.kmdp.util.NameUtils.namespaceURIStringToPackage;
import static edu.mayo.kmdp.util.NameUtils.removeTrailingPart;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import edu.mayo.kmdp.terms.adapters.json.AbstractTermsJsonAdapter;
import edu.mayo.kmdp.terms.adapters.xml.TermsXMLAdapter;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig.EnumGenerationParams;
import edu.mayo.kmdp.terms.generator.internal.AnonymousConceptScheme;
import edu.mayo.kmdp.terms.generator.internal.ConceptGraph;
import edu.mayo.kmdp.terms.generator.internal.ConceptTermImpl;
import edu.mayo.kmdp.terms.generator.internal.ConceptTermSeries;
import edu.mayo.kmdp.util.DateTimeUtil;
import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.NameUtils;
import edu.mayo.kmdp.util.PropertiesUtil;
import edu.mayo.kmdp.util.StreamUtil;
import edu.mayo.kmdp.util.URIUtil;
import edu.mayo.kmdp.util.Util;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import org.omg.spec.api4kp._20200801.id.ConceptIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.terms.ConceptScheme;

public abstract class BaseEnumGenerator {

  protected static Map<String, Template> registry = new HashMap<>();

  Map<ConceptScheme, Map<String, Object>> contextCache = new HashMap<>();

  static {
    prepareTemplates();
  }

  protected BaseEnumGenerator() {
  }

  protected BaseEnumGenerator(BaseEnumGenerator other) {
    this.contextCache = new HashMap<>(other.contextCache);
  }

  private static void prepareTemplates() {
    registry.put("concepts-java",
        getResource("java/concepts-java.mustache"));
    registry.put("concepts-java-interface",
        getResource("java/concepts-java-interface.mustache"));
    registry.put("concepts-java-series",
        getResource("java/concepts-java-series.mustache"));
    registry.put("concepts-xsd",
        getResource("concepts-xsd.mustache"));
    registry.put("concepts-xsd-series",
        getResource("concepts-xsd-series.mustache"));
    registry.put("concepts-xjb",
        getResource("concepts-xjb.mustache"));
    registry.put("concepts-xjb-series",
        getResource("concepts-xjb-series.mustache"));
    registry.put("catalog",
        getResource("catalog.mustache"));
  }

  private static Template getResource(String templ) {
    String s = FileUtil.read(BaseEnumGenerator.class.getResourceAsStream("/templates/" + templ))
        .orElse("Mustache Template " + templ + " not found");
    return Mustache.compiler().compile(s);
  }


  protected Map<String, Object> getContext(ConceptScheme<Term> conceptScheme,
      EnumGenerationConfig options,
      ConceptGraph graph) {

    if (contextCache.containsKey(conceptScheme)) {
      return contextCache.get(conceptScheme);
    }

    Boolean jaxb = options.getTyped(EnumGenerationParams.WITH_JAXB);
    Boolean jsonld = options.getTyped(EnumGenerationParams.WITH_JSONLD);
    Boolean json = options.getTyped(EnumGenerationParams.WITH_JSON);

    String defaultPackage = options.getTyped(EnumGenerationParams.PACKAGE_NAME);
    Properties overrides = PropertiesUtil
        .parseProperties(options.getTyped(EnumGenerationParams.PACKAGE_OVERRIDES));

    String api4kpRelease = options.getTyped(EnumGenerationParams.API4KP_RELEASE);
    String className = conceptScheme.getPublicName();
    String seriesName = conceptScheme.getPublicName() + "Series";
    String interfaceName = conceptScheme.getPublicName();
    String innerPackageName = getPackageName(conceptScheme.getVersionId(), defaultPackage,
        overrides);
    String outerPackageName = getPackageName(conceptScheme.getId(), defaultPackage, overrides);

    if (interfaceName.equals(className) && outerPackageName.equals(innerPackageName)) {
      interfaceName = "I" + interfaceName;
    }

    List<SeriesHolder> holder = toSeries(graph.getConceptSeries(conceptScheme.getId()), defaultPackage, overrides);

    Map<String, Object> context = new HashMap<>();
    context.put("api4kpRelease", api4kpRelease);
    context.put("conceptScheme", conceptScheme);
    context.put("conceptSchemeTag", NameUtils.getTrailingPart(conceptScheme.getId().toString()));
    context.put("concepts", graph.getConceptList(conceptScheme));
    context.put("schemeVersions", graph.getSchemeSeries(conceptScheme.getId()));
    context.put("schemeVersionIdentifiers",
        graph.getSchemeSeriesURI(conceptScheme.getId()));

    context.put("conceptSeries", holder);
    context.put("publicationDate", DateTimeUtil.serializeAsDate(conceptScheme.getEstablishedOn()));
    context.put("publicationDates", graph.getSchemeReleases(conceptScheme.getId()));

    context.put("typeName", className);
    context.put("seriesName", seriesName);

    Optional<String> overrideOpt = getOverrideInterface(conceptScheme, options);
    if (overrideOpt.isPresent()) {
      String override = overrideOpt.get();
      String overridePackage = getPackageName(URI.create(override +"/"),defaultPackage,overrides);
      String overrideName = NameUtils.getTrailingPart(override);
      context.put("intfName", overrideName);
      context.put("intfPackageName", overridePackage);
    } else {
      context.put("intfName", interfaceName);
      context.put("intfPackageName", outerPackageName);
    }
    context.put("outerPackageName", outerPackageName);

    context.put("allSeries",
        getImplementingSeries(
            conceptScheme,
            graph,
            options,
            cs -> cs.getPublicName() + "Series",
            cs -> getPackageName(cs.getId(), defaultPackage, overrides)));

    context.put("seriesNamespace",
        edu.mayo.kmdp.util.NameUtils.removeFragment(conceptScheme.getId()));
    context.put("namespace",
        edu.mayo.kmdp.util.NameUtils.removeFragment(conceptScheme.getVersionId()));
    context.put("packageName", innerPackageName);
    context.put("overridePk", overridePk(defaultPackage, overrides));
    context.put("baseJsonAdapter", options.get(EnumGenerationParams.JSON_ADAPTER)
        .orElse(AbstractTermsJsonAdapter.class.getName()));
    context.put("baseXmlAdapter", options.get(EnumGenerationParams.XML_ADAPTER)
        .orElse(TermsXMLAdapter.class.getName()));
    context.put("implClassName",
        AnonymousConceptScheme.class.getName());
    context.put("typeIntf", Term.class);

    context.put("jaxb", jaxb);
    context.put("jsonld", jsonld);
    context.put("json", json);

    contextCache.put(conceptScheme, context);

    return context;
  }

  private List<String> getImplementingSeries(
      ConceptScheme<Term> cs,
      ConceptGraph graph,
      EnumGenerationConfig options,
      Function<ConceptScheme<Term>,String> seriesNameMapper,
      Function<ConceptScheme<Term>,String> seriesPackageMapper
      ) {

    List<String> allSeries = new ArrayList<>();
    allSeries.add(seriesPackageMapper.apply(cs) + "." + seriesNameMapper.apply(cs));

    Optional<String> overridesOpt = options.get(EnumGenerationParams.INTERFACE_OVERRIDES);
    if (overridesOpt.isPresent() && ! Util.isEmpty(overridesOpt.get())) {
      String overrides = overridesOpt.get();
      Arrays.stream(overrides.split(","))
          .forEach(override -> {
            URI overridden = URI.create(override.substring(0, override.indexOf('=')));
            URI overriding = URI.create(override.substring(override.indexOf('=') + 1));
            if (overriding.equals(URIUtil.normalizeURI(cs.getId()))) {
              ConceptScheme<Term> overriddenCS = graph.getConceptSchemes().stream()
                  .filter(x -> URIUtil.normalizeURI(x.getId()).equals(overridden))
                  .findFirst()
                  .orElseThrow(() -> new IllegalStateException("Unable to find a scheme for " + overridden));
              allSeries.add(seriesPackageMapper.apply(overriddenCS)
                  + "." + seriesNameMapper.apply(overriddenCS));
            }
          });
    }
    return allSeries;
  }

  protected Optional<String> getOverrideInterface(ConceptScheme<Term> cs,
      EnumGenerationConfig options) {
    if (options.get(EnumGenerationParams.INTERFACE_OVERRIDES).isEmpty()) {
      return Optional.empty();
    }
    String overrides = options.getTyped(EnumGenerationParams.INTERFACE_OVERRIDES);
    if (Util.isEmpty(overrides)) {
      return Optional.empty();
    }
    return Arrays.stream(overrides.split(","))
        .filter(s -> s.substring(0, s.indexOf('=')).equals(URIUtil.normalizeURIString(cs.getId())))
        .map(s -> s.substring(s.indexOf('=') + 1))
        .findFirst();
  }

  private List<SeriesHolder> toSeries(
      Collection<ConceptTermSeries> conceptSeries,
      String defaultPackage, Properties overrides) {

    return conceptSeries.stream()
        .map(s -> new SeriesHolder(
            s.getLabel(),
            s.getVersions(),
            p -> getPackageName(p, defaultPackage, overrides)))
        .collect(Collectors.toList());
  }

  private Mustache.Lambda overridePk(String defaultPackage, Properties overrides) {
    return (frag, out) -> {
      String key = frag.execute();
      out.write(getPackageName(key,defaultPackage,overrides));
    };
  }

  protected String getPackageName(URI baseUri, String defaultPackage, Properties packageNameOverrides) {
    String packageName = namespaceURIStringToPackage(removeTrailingPart(baseUri.toString()));
    return getPackageName(packageName,defaultPackage,packageNameOverrides);
  }

  protected String getPackageName(String nativePackage, String defaultPackage, Properties packageNameOverrides) {
    if (!Util.isEmpty(defaultPackage)) {
      return defaultPackage;
    }
    return packageNameOverrides.getOrDefault(nativePackage,nativePackage).toString();
  }


  protected String fromTemplate(String templateId, Map<String, Object> context) {
    StringWriter sw = new StringWriter();
    Template m = registry.get(templateId);
    if (m == null) {
      return "Mustache Template " + templateId + " not found";
    }
    m.execute(context, sw);
    return sw.toString();
  }



  protected File getFile(File outputDir, Map<String, Object> context, String ext) {
    String packageName = (String) context.get("packageName");
    String fileName = (String) context.get("typeName");
    return getFile(outputDir,packageName,fileName,ext);
  }

  protected File getFile(File outputDir, String packageName, String fileName, String ext) {
    File packageDir = new File(outputDir, packageName.replace('.', File.separatorChar));
    if (!packageDir.exists()) {
      packageDir.mkdirs();
    }

    return new File(packageDir, fileName + ext);
  }

  protected void writeToFile(
      String content,
      File outputFile) {

    try(FileWriter writer = new FileWriter(outputFile)) {
      writer.write(content);
      writer.flush();
    } catch (IOException e) {
      throw new EnumGenerationException(e);
    }
  }

  protected class EnumGenerationException extends RuntimeException {
    public EnumGenerationException(Exception e) {
      super(e);
    }
  }

  public static class SeriesHolder {
    private String term;
    private List<String> versions;

    public SeriesHolder(String label, List<Term> versions, UnaryOperator<String> packageNameMapper ) {
      this.term = label;
      this.versions = versions.stream()
          .flatMap(StreamUtil.filterAs(ConceptTermImpl.class))
          // Ensure latest version first
          .sorted(Comparator.comparing(ConceptIdentifier::getEstablishedOn).reversed())
          //
          .map(v -> packageNameMapper.apply(v.getTermConceptPackage())
              + "."
              + v.getScheme().getPublicName()
              + "."
              + v.getTermConceptName())
          .collect(Collectors.toList());
    }

    public String getTerm() {
      return term;
    }
    public List<String> getVersions() {
      return versions;
    }
  }
}
