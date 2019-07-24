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
package edu.mayo.kmdp.terms.skos.plugin;

import edu.mayo.kmdp.terms.mireot.EntityTypes;
import edu.mayo.kmdp.terms.mireot.MireotConfig;
import edu.mayo.kmdp.terms.mireot.MireotConfig.MireotParameters;
import edu.mayo.kmdp.terms.mireot.MireotExtractor;
import edu.mayo.kmdp.terms.skosifier.Modes;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig.OWLtoSKOSTxParams;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConverter;
import edu.mayo.kmdp.util.CatalogBasedURIResolver;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Optional;
import org.apache.jena.rdf.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.protege.xmlcatalog.CatalogUtilities;
import org.protege.xmlcatalog.XMLCatalog;
import org.protege.xmlcatalog.owlapi.XMLCatalogIRIMapper;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.io.StreamDocumentSource;
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


/**
 * Goal
 *
 * @goal owl-to-skos
 *
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class SkosGeneratorPlugin extends AbstractMojo {


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
  private String owlFile;

  public String getOwlFile() {
    return owlFile;
  }

  public void setOwlFile(String owlFile) {
    this.owlFile = owlFile;
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
  private String skosOutputFile;

  public String getSkosOutputFile() {
    return skosOutputFile;
  }

  public void setSkosOutputFile(String outputFile) {
    this.skosOutputFile = outputFile;
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
  private String targetNamespace;

  public String getTargetNamespace() {
    return targetNamespace;
  }

  public void setTargetNamespace(String targetNamespace) {
    this.targetNamespace = targetNamespace;
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
  private URL catalogURL = null;

  public URL getCatalogURL() {
    return catalogURL;
  }

  public void setCatalogURL(URL catalog) {
    this.catalogURL = catalog;
  }

  public void execute() throws MojoExecutionException {
    InputStream is = null;
    try {
      if (owlFile == null) {
        getLog().info("No ontology file to process, exiting...");
        return;
      }

      if (targetURI == null) {
        getLog().info("No entity selected, exiting...");
        return;
      }
      if (skosOutputFile == null) {
        skosOutputFile = generateDefaultOutputFile(owlFile);
      }

      is = resolve(owlFile);

      MireotConfig mfg = new MireotConfig()
          .with(MireotParameters.BASE_URI,targetNamespace)
          .with(MireotParameters.ENTITY_TYPE,entityType)
          .with(MireotParameters.ENTITY_ONLY,entityOnly)
          .with(MireotParameters.MIN_DEPTH,minDepth)
          .with(MireotParameters.MAX_DEPTH,maxDepth);

      Owl2SkosConfig cfg = new Owl2SkosConfig()
          .with(OWLtoSKOSTxParams.TGT_NAMESPACE,skosNamespace)
          .with(OWLtoSKOSTxParams.ADD_IMPORTS,true)
          .with(OWLtoSKOSTxParams.SCHEME_NAME,schemeName)
          .with(OWLtoSKOSTxParams.TOP_CONCEPT_NAME,topConceptName)
          .with(OWLtoSKOSTxParams.MODE, profile);

      Optional<Model> mireotedModel = new MireotExtractor()
          .fetch(
              ensureFormat(is,
                  new RDFXMLDocumentFormat(),
                  catalogURL),
              URI.create(targetURI),
              mfg);

      Optional<Model> skosModel = mireotedModel
          .flatMap((ext) -> new Owl2SkosConverter().apply(ext, cfg));

      if (skosModel.isPresent()) {
        if (!getOutputDirectory().exists()) {
          getOutputDirectory().mkdirs();
        }
        File f = new File(getOutputDirectory()
            + File.separator
            + getSkosOutputFile());
        FileOutputStream fos = new FileOutputStream(f);
        try {
          skosModel.get().write(fos);
        } finally {
          fos.close();
        }
      } else {
        getLog().error("Unable to generate a SKOS model");
      }

    } catch (Exception e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
      throw new MojoExecutionException(e.getMessage(), e);
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

  }

  private String generateDefaultOutputFile(String owlFile) {
    return owlFile.substring(owlFile.lastIndexOf(File.separator))
        .replaceAll(".owl", "")
        .replaceAll(".rdf", "") + ".skos.rdf";
  }

  private InputStream resolve(String file) {
    return CatalogBasedURIResolver.resolveFilePath(file, catalogURL)
        .orElse(SkosGeneratorPlugin.class.getResourceAsStream(owlFile));
  }


  public InputStream ensureFormat(InputStream is, OWLDocumentFormat fmt, URL catalogURL) {
    try {
      OWLOntologyManager manager = getManager(catalogURL);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      OWLOntology onto = manager.loadOntologyFromOntologyDocument(is);
      OWLOntologyID originalId = onto.getOntologyID();

      preloadImports(manager,onto);

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

      //onto.saveOntology(System.out);
      return new ByteArrayInputStream(baos.toByteArray());

    } catch (Exception e) {
      e.printStackTrace();
      return is;
    }
  }

  private void preloadImports(final OWLOntologyManager manager, final OWLOntology onto) {
    onto.directImportsDocuments().forEach(
        ontologyIRI -> {
          if (manager.getOntology(ontologyIRI) == null) {
            try {
              if (applyMappings(ontologyIRI, manager).isPresent()) {
                OWLOntology importedOntology = manager.loadOntologyFromOntologyDocument(new StreamDocumentSource(
                    resolve(ontologyIRI.toString()),
                    ontologyIRI));
                preloadImports(manager,importedOntology);
              } else {
                OWLOntology importedOntology = manager.loadOntology(ontologyIRI);
                preloadImports(manager,importedOntology);
              }
            } catch (OWLOntologyCreationException e) {
              e.printStackTrace();
            }
          }
        }
    );
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

  private static OWLOntologyManager getManager(URL catalogURL) throws IOException {
    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    manager.setOntologyConfigurator(new OntologyConfigurator()
        .setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT));

    if (catalogURL != null) {
      XMLCatalog catalog = CatalogUtilities.parseDocument(catalogURL);
      manager.setIRIMappers(Collections.singleton(new XMLCatalogIRIMapper(catalog)));
    }
    return manager;
  }

}



