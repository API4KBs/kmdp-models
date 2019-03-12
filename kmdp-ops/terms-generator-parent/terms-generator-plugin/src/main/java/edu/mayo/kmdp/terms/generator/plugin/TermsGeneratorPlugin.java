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
package edu.mayo.kmdp.terms.generator.plugin;

import edu.mayo.kmdp.terms.generator.CatalogGenerator;
import edu.mayo.kmdp.terms.generator.JavaEnumTermsGenerator;
import edu.mayo.kmdp.terms.generator.SkosTerminologyAbstractor;
import edu.mayo.kmdp.terms.generator.XSDEnumTermsGenerator;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationParams;
import edu.mayo.kmdp.terms.generator.util.OntologyLoader;
import edu.mayo.kmdp.util.CatalogBasedURIResolver;
import edu.mayo.kmdp.util.Util;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.protege.xmlcatalog.CatalogUtilities;
import org.protege.xmlcatalog.owlapi.XMLCatalogIRIMapper;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Goal
 *
 * @goal generate-terms
 *
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class TermsGeneratorPlugin extends AbstractMojo {


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
  private String sourceCatalogPath;

  public String getSourceCatalogPath() {
    return sourceCatalogPath;
  }

  public void setSourceCatalogPath(String sourceCatalogPath) {
    this.sourceCatalogPath = sourceCatalogPath;
  }

  /**
   * @parameter
   */
  private String termsProvider;

  public String getTermsProvider() {
    return termsProvider;
  }

  public void setTermsProvider(String termsProvider) {
    this.termsProvider = termsProvider;
  }

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      if (xsdOutputDirectory == null) {
        xsdOutputDirectory = outputDirectory;
      }

      List<String> files = flatten(owlFiles);

      Collection<CatalogGenerator.CatalogEntry> entries = files.stream()
          .flatMap(this::transform)
          .collect(Collectors.toList());

      if (catalogNamespace != null) {
        new CatalogGenerator().generate(catalogNamespace, entries, xsdOutputDirectory, catalogName);
      }

      registerOutputDir();
    } catch (Exception e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
      throw new MojoExecutionException(e.getMessage());
    }

  }

  private Stream<CatalogGenerator.CatalogEntry> transform(String source) {
    OWLOntology ontology = null;
    try {
      ontology = new OntologyLoader().loadOntology(new String[]{source},
          getSourceCatalog().orElse(null));
    } catch (OWLOntologyCreationException e) {
      return Stream.empty();
    }

    SkosTerminologyAbstractor skosTerminologyAbstractor = new SkosTerminologyAbstractor(ontology,
        this.reason);

    SkosTerminologyAbstractor.ConceptGraph graph = skosTerminologyAbstractor.traverse();

    if (!outputDirectory.exists()) {
      outputDirectory.mkdirs();
    }

    EnumGenerationConfig opts = new EnumGenerationConfig()
        .with(EnumGenerationParams.WITH_JSONLD, Boolean.toString(isJsonLD()))
        .with(EnumGenerationParams.WITH_JSON, Boolean.toString(isJson()))
        .with(EnumGenerationParams.WITH_JAXB, Boolean.toString(isJaxb()))
        .with(EnumGenerationParams.TERMS_PROVIDER, termsProvider);
    if (packageName != null) {
      opts.with(EnumGenerationParams.PACKAGE_NAME, packageName);
    }

    new JavaEnumTermsGenerator().generate(graph, opts, outputDirectory);
    if (isJaxb()) {
      new XSDEnumTermsGenerator().generate(graph, opts, xsdOutputDirectory);
    }

    return graph.getConceptSchemes().stream()
        .map(CatalogGenerator.CatalogEntry::new);
  }

  private Optional<OWLOntologyIRIMapper> getSourceCatalog() {
    if (Util.isEmpty(sourceCatalogPath)) {
      return Optional.empty();
    }
    return CatalogBasedURIResolver.resolveFilePathToURL(sourceCatalogPath)
        .flatMap(this::getSourceCatalog);
  }

  private Optional<OWLOntologyIRIMapper> getSourceCatalog(URL cat) {
    try {
      return Optional.of(new XMLCatalogIRIMapper(CatalogUtilities.parseDocument(cat)));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return Optional.empty();
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
    return Arrays.stream(f.listFiles())
        .map(File::getAbsolutePath)
        .collect(Collectors.toList());
  }

}



