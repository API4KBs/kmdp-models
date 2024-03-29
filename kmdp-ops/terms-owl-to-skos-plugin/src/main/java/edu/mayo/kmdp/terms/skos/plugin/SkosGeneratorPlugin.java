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
package edu.mayo.kmdp.terms.skos.plugin;

import static edu.mayo.kmdp.util.CatalogBasedURIResolver.catalogResolver;

import edu.mayo.kmdp.terms.mireot.EntityTypes;
import edu.mayo.kmdp.terms.mireot.MireotConfig;
import edu.mayo.kmdp.terms.mireot.MireotConfig.MireotParameters;
import edu.mayo.kmdp.terms.mireot.MireotExtractor;
import edu.mayo.kmdp.terms.skosifier.Modes;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig.OWLtoSKOSTxParams;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConverter;
import edu.mayo.kmdp.util.CatalogBasedURIResolver;
import edu.mayo.kmdp.util.URIUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.jena.rdf.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.HasOntologyID;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OntologyConfigurator;
import org.semanticweb.owlapi.model.SetOntologyID;
import org.semanticweb.owlapi.util.OWLOntologyImportsClosureSetProvider;
import org.semanticweb.owlapi.util.OWLOntologyMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;


/**
 * Goal
 *
 * @goal owl-to-skos
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class SkosGeneratorPlugin extends AbstractMojo {

  private Logger logger = LoggerFactory.getLogger(SkosGeneratorPlugin.class);

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
   * @parameter
   */
  private String owlSourceURL;

  public String getOwlSourceURL() {
    return owlSourceURL;
  }

  public void setOwlSourceURL(String owlSourceURL) {
    this.owlSourceURL = owlSourceURL;
  }

  /**
   * @parameter
   */
  private List<String> owlSourceURLs;

  public List<String> getOwlSourceURLs() {
    return owlSourceURLs;
  }

  public void setOwlSourceURLs(List<String> owlSourceURLs) {
    this.owlSourceURLs = owlSourceURLs;
  }

  /**
   * @parameter default-value="./target/generated-sources/skos"
   */
  private File outputDirectory;

  public File getOutputDirectory() {
    return outputDirectory;
  }

  public void setOutputDirectory(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  /**
   * @parameter
   */
  private List<String> skosOutputFiles;

  public List<String> getSkosOutputFiles() {
    return skosOutputFiles;
  }

  public void setSkosOutputFiles(List<String> outputFiles) {
    this.skosOutputFiles = outputFiles;
  }

  /**
   * @parameter default-value="http://shapes.kie.org/vocab"
   */
  private String skosNamespace;

  public String getSkosNamespace() {
    return skosNamespace;
  }

  public void setSkosNamespace(String skosNamespace) {
    this.skosNamespace = skosNamespace;
  }

  /**
   * @parameter
   */
  private String targetURI;

  public String getTargetURI() {
    return targetURI;
  }

  public void setTargetURI(String targetURI) {
    this.targetURI = targetURI;
  }

  /**
   * @parameter
   */
  private String owlNamespace;

  public String getOwlNamespace() {
    return owlNamespace;
  }

  public void setOwlNamespace(String owlNamespace) {
    this.owlNamespace = owlNamespace;
  }

  /**
   * @parameter
   */
  private String schemeName;

  public String getSchemeName() {
    return schemeName;
  }

  public void setSchemeName(String schemeName) {
    this.schemeName = schemeName;
  }

  /**
   * @parameter
   */
  private String topConceptName;

  public String getTopConceptName() {
    return topConceptName;
  }

  public void setTopConceptName(String topConceptName) {
    this.topConceptName = topConceptName;
  }

  /**
   * @parameter default-value=false
   */
  private boolean entityOnly;

  public boolean isEntityOnly() {
    return entityOnly;
  }

  public void setEntityOnly(boolean entityOnly) {
    this.entityOnly = entityOnly;
  }

  /**
   * @parameter default-value="SKOS"
   */
  private Modes profile = Modes.SKOS;

  public Modes getProfile() {
    return profile;
  }

  public void setProfile(Modes profile) {
    this.profile = profile;
  }


  /**
   * @parameter default-value="UNKNOWN"
   */
  private EntityTypes entityType = EntityTypes.UNKNOWN;

  public EntityTypes getEntityType() {
    return entityType;
  }

  public void setEntityType(EntityTypes entityType) {
    this.entityType = entityType;
  }

  /**
   * @parameter default-value=0
   */
  private int minDepth = 0;

  public int getMinDepth() {
    return minDepth;
  }

  public void setMinDepth(int minDepth) {
    this.minDepth = minDepth;
  }

  /**
   * @parameter default-value=-1;
   */
  private int maxDepth = -1;

  public int getMaxDepth() {
    return maxDepth;
  }

  public void setMaxDepth(int maxDepth) {
    this.maxDepth = maxDepth;
  }


  /**
   * @parameter
   */
  private List<URL> catalogURLs = null;

  public List<URL> getCatalogURLs() {
    return catalogURLs;
  }

  public void setCatalogURLs(List<URL> catalog) {
    this.catalogURLs = catalog;
  }

  /**
   * @parameter
   */
  private boolean namespaceScoped = false;

  public boolean isNamespaceScoped() {
    return namespaceScoped;
  }

  public void setNamespaceScoped(boolean namespaceScoped) {
    this.namespaceScoped = namespaceScoped;
  }

  public void execute() throws MojoExecutionException {

    if (targetURI == null) {
      getLog().info("No entity selected, exiting...");
      return;
    }

    try {
      List<String> sourceURLs = owlSourceURL != null
          ? Collections.singletonList(owlSourceURL)
          : owlSourceURLs;
      if (sourceURLs == null || sourceURLs.isEmpty()) {
        getLog().info("No ontology file to process, exiting...");
        return;
      }

      int numSources = sourceURLs.size();

      List<URL> catalogs = (catalogURLs == null || catalogURLs.isEmpty())
          ? Collections.nCopies(numSources, null)
          : catalogURLs;
      if (catalogs.size() != numSources) {
        if (catalogs.size() != 1) {
          logger.warn("Mismatch between the number of provided catalogs and the number "
              + "of source URLs");
        }
        catalogs = Collections.nCopies(numSources, catalogs.get(0));
      }

      List<String> outputFiles =
          (skosOutputFiles == null
              || skosOutputFiles.isEmpty() || skosOutputFiles.size() != numSources)
              ? sourceURLs.stream().map(this::generateDefaultOutputFile)
              .collect(Collectors.toList())
              : skosOutputFiles;

      for (int index = 0; index < numSources; index++) {
        generateSKOS(
            sourceURLs.get(index),
            URIUtil.asURI(catalogs.get(index)),
            outputFiles.get(index));
      }
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }

  }

  private void generateSKOS(String owlSourceURL, URI catalogURL, String outputFile)
      throws IOException {
    try (InputStream is = resolve(owlSourceURL, catalogURL)) {

      MireotConfig mfg = new MireotConfig()
          .with(MireotParameters.BASE_URI, owlNamespace)
          .with(MireotParameters.ENTITY_TYPE, entityType)
          .with(MireotParameters.ENTITY_ONLY, entityOnly)
          .with(MireotParameters.MIN_DEPTH, minDepth)
          .with(MireotParameters.MAX_DEPTH, maxDepth)
          .with(MireotParameters.NAMESPACE_SCOPED, namespaceScoped);

      Owl2SkosConfig cfg = new Owl2SkosConfig()
          .with(OWLtoSKOSTxParams.TGT_NAMESPACE, skosNamespace)
          .with(OWLtoSKOSTxParams.ADD_IMPORTS, profile != Modes.OMG)
          .with(OWLtoSKOSTxParams.SCHEME_NAME, schemeName)
          .with(OWLtoSKOSTxParams.TOP_CONCEPT_NAME, topConceptName)
          .with(OWLtoSKOSTxParams.MODE, profile);
      if (profile == Modes.OMG) {
        cfg.with(OWLtoSKOSTxParams.VERSION_POS, 2);
      }

      Optional<Model> mireotedModel = new MireotExtractor()
          .fetch(
              ensureFormat(is,
                  new RDFXMLDocumentFormat(),
                  catalogURL),
              URI.create(targetURI),
              mfg);

      Optional<Model> skosModel = mireotedModel
          .flatMap(ext -> new Owl2SkosConverter().apply(ext, cfg));

      File f = new File(getOutputDirectory()
          + File.separator
          + outputFile);

      if (skosModel.isPresent()) {
        if (!f.getParentFile().exists()) {
          boolean success = f.getParentFile().mkdirs();
          if (!success) {
            throw new IOException("Unable to create output folder " + getOutputDirectory());
          }
        }

        try (FileOutputStream fos = new FileOutputStream(f)) {
          skosModel.get().write(fos);
        }
      } else {
        getLog().error("Unable to generate a SKOS model");
      }

    }

  }

  private InputStream resolve(String owlSourceURL, URI catalogURL) {
    CatalogBasedURIResolver resolver = new CatalogBasedURIResolver(
        catalogResolver(catalogURL));
    try {
      return resolver.resolveEntity(owlSourceURL, owlSourceURL, owlSourceURL, null);
    } catch (Exception tfe) {
      logger.error(tfe.getMessage(), tfe);
      return null;
    }
  }

  private String generateDefaultOutputFile(String owlFile) {
    return owlFile.substring(owlFile.lastIndexOf(File.separator))
        .replaceAll(".owl", "")
        .replaceAll(".rdf", "") + ".skos.rdf";
  }


  public InputStream ensureFormat(InputStream is, OWLDocumentFormat fmt, URI catalogURL) {
    try {
      OWLOntologyManager manager = getManager(catalogURL);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      OWLOntology onto = manager.loadOntologyFromOntologyDocument(is);
      OWLOntologyID originalId = onto.getOntologyID();

      preloadImports(manager, onto, catalogURL);

      OWLOntologyMerger merger = new OWLOntologyMerger(
          new OWLOntologyImportsClosureSetProvider(manager, onto));

      onto = merger.createMergedOntology(
          manager,
          null
      );
      // swap the original ontology with a new ontology that contains the imports closure
      manager.removeOntology(originalId);
      onto.getOWLOntologyManager().applyChange(new SetOntologyID(onto, originalId));
      onto.saveOntology(fmt, baos);

      return new ByteArrayInputStream(baos.toByteArray());

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return is;
    }
  }

  private void preloadImports(final OWLOntologyManager manager, final OWLOntology onto,
      URI catalogURL) {
    onto.directImportsDocuments().forEach(
        ontologyIRI -> {
          if (!isOntologyLoaded(manager, ontologyIRI)) {
            try {
              IRI resolved = applyMappings(ontologyIRI, manager).orElse(ontologyIRI);
              OWLOntology importedOntology = manager.loadOntology(resolved);
              preloadImports(manager, importedOntology, catalogURL);
            } catch (OWLOntologyCreationException e) {
              logger.error(e.getMessage(), e);
            }
          }
        }
    );
  }

  private boolean isOntologyLoaded(OWLOntologyManager manager, IRI ontologyIRI) {
    // different styles in the use of versioned vs series IRIs in import require to check both
    // TODO FIXME This is a workaround the known issue with the DOL ontology URI mismatches
    // return manager.contains(ontologyIRI) || manager.containsVersion(ontologyIRI);
    return manager.ontologies()
        .map(HasOntologyID::getOntologyID)
        .anyMatch(id -> matches(id, ontologyIRI));
  }

  private boolean matches(OWLOntologyID id, IRI ontologyIRI) {
    return id.getOntologyIRI().map(oiri -> matchesIgnoreScheme(oiri, ontologyIRI)).orElse(false)
        || id.getVersionIRI().map(viri -> matchesIgnoreScheme(viri, ontologyIRI)).orElse(false);
  }

  private boolean matchesIgnoreScheme(IRI tgtIri, IRI ontologyIRI) {
    if (tgtIri == null || ontologyIRI == null) {
      return false;
    }
    String s1 = tgtIri.toString().replace("https://", "http://");
    String s2 = ontologyIRI.toString().replace("https://", "http://");
    return s1.equals(s2);
  }

  private static Optional<IRI> applyMappings(IRI ontologyIRI, OWLOntologyManager manager) {
    for (OWLOntologyIRIMapper owlOntologyIRIMapper : manager.getIRIMappers()) {
      IRI mappedIRI = owlOntologyIRIMapper.getDocumentIRI(ontologyIRI);
      if (mappedIRI != null) {
        return Optional.of(mappedIRI);
      }
    }
    return Optional.empty();
  }

  private static OWLOntologyManager getManager(URI catalogURL) {
    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    manager.setOntologyConfigurator(new OntologyConfigurator()
        .setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT));

    if (catalogURL != null) {
      manager.setIRIMappers(Collections.singleton(new CatalogBasedIRIMapper(catalogURL)));
    }
    return manager;
  }

  private static class CatalogBasedIRIMapper implements OWLOntologyIRIMapper {

    transient CatalogBasedURIResolver resolver;

    public CatalogBasedIRIMapper(URI catalogURL) {
      resolver = new CatalogBasedURIResolver(catalogResolver(catalogURL));
    }

    @Nullable
    @Override
    public IRI getDocumentIRI(IRI ontologyIRI) {
      String iriStr = ontologyIRI.getIRIString();
      InputSource resolved = resolver.resolveEntity(iriStr, iriStr);
      return Optional.ofNullable(resolved)
          .map(InputSource::getSystemId)
          .map(IRI::create)
          .orElse(null);
    }
  }
}



