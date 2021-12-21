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
package edu.mayo.kmdp.terms.generator.plugin;

import static edu.mayo.kmdp.util.CatalogBasedURIResolver.catalogResolver;

import edu.mayo.kmdp.terms.generator.CatalogGenerator;
import edu.mayo.kmdp.terms.generator.CatalogGenerator.CatalogEntry;
import edu.mayo.kmdp.terms.generator.JavaEnumTermsGenerator;
import edu.mayo.kmdp.terms.generator.SkosTerminologyAbstractor;
import edu.mayo.kmdp.terms.generator.XSDEnumTermsGenerator;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig.EnumGenerationParams;
import edu.mayo.kmdp.terms.generator.config.SkosAbstractionConfig;
import edu.mayo.kmdp.terms.generator.config.SkosAbstractionConfig.CLOSURE_MODE;
import edu.mayo.kmdp.terms.generator.config.SkosAbstractionConfig.SkosAbstractionParameters;
import edu.mayo.kmdp.terms.generator.internal.ConceptGraph;
import edu.mayo.kmdp.terms.generator.internal.VersionedConceptGraph;
import edu.mayo.kmdp.terms.generator.util.OntologyLoader;
import edu.mayo.kmdp.util.CatalogBasedURIResolver;
import edu.mayo.kmdp.util.URIUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.kmdp.util.XMLUtil;
import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.xml.catalog.CatalogResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Goal
 *
 * @goal generate-terms
 *
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class TermsGeneratorPlugin extends AbstractMojo {

  private Logger logger = LoggerFactory.getLogger(TermsGeneratorPlugin.class);

  /**
   * @parameter default-value="false"
   */
  private boolean reason = false;

  public boolean isReason() {
    return reason;
  }

  public void setReason(boolean reason) {
    this.reason = reason;
  }

  /**
   * @parameter default-value="false"
   */
  private boolean enforceClosure = false;

  public boolean isEnforceClosure() {
    return enforceClosure;
  }

  public void setEnforceClosure(boolean enforceClosure) {
    this.enforceClosure = enforceClosure;
  }

  /**
   * @parameter default-value="false"
   */
  private boolean enforceVersion = false;

  public boolean isEnforceVersion() {
    return enforceVersion;
  }

  public void setEnforceVersion(boolean enforceVersion) {
    this.enforceVersion = enforceVersion;
  }

  /**
   * @parameter default-value=".*\/(.*)\/$"
   */
  private String versionPattern;

  public String getVersionPattern() {
    return versionPattern;
  }

  public void setVersionPattern(String versionPattern) {
    this.versionPattern = versionPattern;
  }

  /**
   * @parameter default-value="IMPORTS"
   */
  private CLOSURE_MODE closureMode;

  public CLOSURE_MODE getClosureMode() {
    return closureMode;
  }

  public void setClosureMode(
      CLOSURE_MODE closureMode) {
    this.closureMode = closureMode;
  }

  /**
   * @parameter default-value="false"
   */
  private boolean jaxb = false;

  public boolean isJaxb() {
    return jaxb;
  }

  public void setJaxb(boolean jaxb) {
    this.jaxb = jaxb;
  }

  /**
   * @parameter default-value="false"
   */
  private boolean jsonld = false;

  public boolean isJsonLD() {
    return jsonld;
  }

  public void setJsonLD(boolean jsonLD) {
    jsonld = jsonLD;
  }

  /**
   * @parameter default-value="false"
   */
  private boolean json = false;

  public boolean isJson() {
    return json;
  }

  public void setJson(boolean json) {
    this.json = json;
  }

  /**
   * @parameter default-value="true"
   */
  private boolean java = true;

  public boolean isJava() {
    return java;
  }

  public void setJava(boolean java) {
    this.java = java;
  }

  /**
   * @parameter default-value="true"
   */
  private boolean xsd = true;

  public boolean isXSD() {
    return xsd;
  }

  public void setXSD(boolean xsd) {
    this.xsd = xsd;
  }

  /**
   * @parameter
   */
  private List<String> owlFiles;

  public List<String> getOwlFiles() {
    return owlFiles;
  }

  public void setOwlFiles(List<String> owlFiles) {
    this.owlFiles = owlFiles;
  }

  /**
   * @parameter
   */
  private List<String> exclusions;

  public List<String> getExclusions() {
    return exclusions;
  }

  public void setExclusions(List<String> exclusions) {
    this.exclusions = exclusions;
  }

  /**
   * @parameter
   */
  private String packageName;

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  /**
   * @parameter
   */
  private String catalogNamespace;

  public String getCatalogNamespace() {
    return catalogNamespace;
  }

  public void setCatalogNamespace(String namespace) {
    this.catalogNamespace = namespace;
  }

  /**
   * @parameter
   */
  private String catalogName;

  public String getCatalogName() {
    return catalogName;
  }

  public void setCatalogName(String catalogName) {
    this.catalogName = catalogName;
  }

  /**
   * @parameter
   */
  private List<String> ignoredIRIs;

  public List<String> getIgnoredIRIs() {
    return ignoredIRIs;
  }

  public void setIgnoredIRIs(List<String> ignoredIRIs) {
    this.ignoredIRIs = ignoredIRIs;
  }

  /**
   * @parameter default-value="./target/generated-sources"
   */
  private File outputDirectory;

  public File getOutputDirectory() {
    return outputDirectory;
  }

  public void setOutputDirectory(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  /**
   * @parameter default-value="./target/generated-sources/xsd"
   */
  private File xsdOutputDirectory;

  public File getXsdOutputDirectory() {
    return xsdOutputDirectory;
  }

  public void setXsdOutputDirectory(File xsdOutputDirectory) {
    this.xsdOutputDirectory = xsdOutputDirectory;
  }

  /**
   * @parameter
   */
  private List<String> sourceCatalogPaths;

  public List<String> getSourceCatalogPaths() {
    return sourceCatalogPaths;
  }

  public void setSourceCatalogPaths(List<String> sourceCatalogPaths) {
    this.sourceCatalogPaths = sourceCatalogPaths;
  }

  /**
   * @parameter
   */
  private String tagFormat;

  public String getTagFormat() {
    return tagFormat;
  }

  public void setTagFormat(String tagFormat) {
    this.tagFormat = tagFormat;
  }


  /**
   * @parameter
   */
  private List<String> interfaceOverrides;

  public List<String> getInterfaceOverrides() {
    return interfaceOverrides;
  }

  public void setInterfaceOverrides(List<String> interfaceOverrides) {
    this.interfaceOverrides = interfaceOverrides;
  }

  /**
   * @parameter
   */
  private String xmlAdapter;

  public String getXmlAdapter() {
    return xmlAdapter;
  }

  public void setXmlAdapter(String xmlAdapter) {
    this.xmlAdapter = xmlAdapter;
  }

  /**
   * @parameter
   */
  private String jsonAdapter;

  public String getJsonAdapter() {
    return jsonAdapter;
  }

  public void setJsonAdapter(String jsonAdapter) {
    this.jsonAdapter = jsonAdapter;
  }

  /**
   * @parameter default-value=20200801
   */
  private String api4kpRelease;

  public String getApi4kpRelease() {
    return api4kpRelease;
  }

  public void setApi4kpRelease(String api4kpRelease) {
    this.api4kpRelease = api4kpRelease;
  }


  /**
   * @parameter default-value=prefLabel
   */
  private String labelProperty;

  public String getLabelProperty() {
    return labelProperty;
  }

  public void setLabelProperty(String labelProperty) {
    this.labelProperty = labelProperty;
  }


  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  public void execute() {
    if (xsdOutputDirectory == null) {
      xsdOutputDirectory = outputDirectory;
    }

    List<String> files = flatten(owlFiles);
    if (exclusions != null) {
      files.removeAll(exclusions);
    }

    if (files.isEmpty()) {
      logger.warn("No files to process, exit...");
      return;
    }

    final List<String> catalogs = alignCatalogs(sourceCatalogPaths, files);

    Collection<CatalogGenerator.CatalogEntry> entries = this.transform(files, catalogs);

    if (catalogNamespace != null) {
      new CatalogGenerator().generate(catalogNamespace, entries, xsdOutputDirectory, catalogName);
    }

    registerOutputDir();
  }

  private List<String> alignCatalogs(List<String> sourceCatalogPaths, List<String> files) {
    List<String> catalogs = (sourceCatalogPaths == null || sourceCatalogPaths.isEmpty())
        ? Collections.nCopies(files.size(), null)
        : sourceCatalogPaths;
    if (catalogs.size() != files.size()) {
      if (catalogs.size() != 1) {
        logger.warn("Mismatch between the number of provided catalogs and the number "
            + "of source URLs");
      }
      catalogs = Collections.nCopies(files.size(), catalogs.get(0));
    }
    return catalogs;
  }

  private List<CatalogEntry> transform(List<String> sources, List<String> sourceCatalogPaths) {
    if (sources.size() != sourceCatalogPaths.size()) {
      logger.error("Each OWL file to be processed should have a corresponding catalog");
    }

    VersionedConceptGraph graph = null;
    for (int j = 0; j < sources.size(); j++) {
      try {
        OWLOntology ontology = readSource(sources.get(j), sourceCatalogPaths.get(j));
        ConceptGraph g = analyze(ontology);
        if (graph == null) {
          graph = new VersionedConceptGraph(g);
        } else {
          graph.merge(g);
        }
      } catch (OWLOntologyCreationException e) {
        logger.error(e.getMessage(), e);
        return Collections.emptyList();
      }
    }

    if (!outputDirectory.exists()) {
      outputDirectory.mkdirs();
    }

    EnumGenerationConfig opts = new EnumGenerationConfig()
        .with(EnumGenerationParams.WITH_JSONLD, Boolean.toString(isJsonLD()))
        .with(EnumGenerationParams.WITH_JSON, Boolean.toString(isJson()))
        .with(EnumGenerationParams.WITH_JAXB, Boolean.toString(isJaxb()))
        .with(EnumGenerationParams.XML_ADAPTER, xmlAdapter)
        .with(EnumGenerationParams.JSON_ADAPTER, jsonAdapter)
        .with(EnumGenerationParams.API4KP_RELEASE, api4kpRelease);
    if (packageName != null) {
      opts.with(EnumGenerationParams.PACKAGE_NAME, packageName);
    }

    opts.with(EnumGenerationParams.INTERFACE_OVERRIDES,
        interfaceOverrides != null
            ? String.join(",", interfaceOverrides)
            : "");

    JavaEnumTermsGenerator bjg =
        new JavaEnumTermsGenerator();

    if (isJava()) {
      bjg.generate(graph, opts, outputDirectory);
    }
    if (isJaxb() || isXSD()) {
      new XSDEnumTermsGenerator(bjg).generate(graph, opts, xsdOutputDirectory);
    }

    return graph != null
        ? graph.getConceptSchemes().stream()
        .map(CatalogGenerator.CatalogEntry::new)
        .collect(Collectors.toList())
        : Collections.emptyList();
  }

  private ConceptGraph analyze(OWLOntology ontology) {
    SkosAbstractionConfig cfg = new SkosAbstractionConfig()
        .with(SkosAbstractionParameters.REASON, this.reason)
        .with(SkosAbstractionParameters.ENFORCE_CLOSURE, enforceClosure)
        .with(SkosAbstractionParameters.CLOSURE_MODE, closureMode)
        .with(SkosAbstractionParameters.ENFORCE_VERSION, enforceVersion)
        .with(SkosAbstractionParameters.VERSION_PATTERN, versionPattern)
        .with(SkosAbstractionParameters.TAG_TYPE, tagFormat)
        .with(SkosAbstractionParameters.VERSION_POS, detectPosition(versionPattern))
        .with(SkosAbstractionParameters.LABEL_PROPERTY, labelProperty);
    return new SkosTerminologyAbstractor()
        .traverse(ontology, cfg);
  }

  // E.g. .*/(.*)/.*/.* -> -2
  private int detectPosition(String versionPattern) {
    if (versionPattern == null) {
      return Integer.MAX_VALUE - 1;
    }
    String s = versionPattern.substring(versionPattern.lastIndexOf(')'));
    int n = 0;
    for (int j = 0; j < s.length(); j++) {
      if (s.charAt(j) == '/') {
        n--;
      }
    }
    return n < 0 ? n : Integer.MAX_VALUE - 1;
  }

  private OWLOntology readSource(String source, String sourceCatalogPath)
      throws OWLOntologyCreationException {

    IRI[] ignoreds = getIgnoredIRIs() == null
        ? new IRI[0]
        : getIgnoredIRIs().stream()
            .map(IRI::create)
            .collect(Collectors.toList())
            .toArray(new IRI[getIgnoredIRIs().size()]);
    return new OntologyLoader().loadOntology(
        new String[]{source},
        getSourceCatalog(sourceCatalogPath).orElse(null),
        ignoreds);
  }

  private Optional<OWLOntologyIRIMapper> getSourceCatalog(String sourceCatalogPath) {
    if (Util.isEmpty(sourceCatalogPath)) {
      return Optional.empty();
    }
    return CatalogBasedURIResolver.resolveFilePathToURL(sourceCatalogPath)
        .map(URIUtil::asURI)
        .filter(Objects::nonNull)
        .flatMap(this::getSourceCatalog);
  }

  private Optional<OWLOntologyIRIMapper> getSourceCatalog(URI cat) {
    CatalogResolver catalog = catalogResolver(cat);
    return Optional.of(new OWLOntologyIRIMapper() {
      @Nullable
      @Override
      public IRI getDocumentIRI(IRI ontologyIRI) {
        String iriStr = ontologyIRI.getIRIString();
        String resolved = catalog.resolveEntity(iriStr, iriStr).getSystemId();
        return resolved != null ? IRI.create(resolved) : null;
      }
    });
  }

  private void registerOutputDir() {
    if (this.getPluginContext() != null && this.getPluginContext().containsKey("project")) {
      project = (MavenProject) this.getPluginContext().get("project");
      project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
    }
  }

  private List<String> flatten(List<String> owlFiles) {
    if (owlFiles.isEmpty()) {
      return Collections.emptyList();
    }
    return owlFiles.stream()
        .map(File::new)
        .filter(File::exists)
        .map(this::flatten)
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  private List<String> flatten(File f) {
    return f.isDirectory()
        ? flatten(listFiles(f))
        : Collections.singletonList(f.getAbsolutePath());
  }

  private List<String> listFiles(File f) {
    return Arrays.stream(Util.ensureArray(f.listFiles(), File.class))
        .map(File::getAbsolutePath)
        .collect(Collectors.toList());
  }

}



