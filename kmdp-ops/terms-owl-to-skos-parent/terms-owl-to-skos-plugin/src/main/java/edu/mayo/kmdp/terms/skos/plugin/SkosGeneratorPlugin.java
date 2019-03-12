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
import edu.mayo.kmdp.terms.mireot.MireotExtractor;
import edu.mayo.kmdp.terms.skosifier.Modes;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConverter;
import edu.mayo.kmdp.util.CatalogBasedURIResolver;
import org.apache.jena.rdf.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.protege.xmlcatalog.CatalogUtilities;
import org.protege.xmlcatalog.XMLCatalog;
import org.protege.xmlcatalog.owlapi.XMLCatalogIRIMapper;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OntologyConfigurator;
import org.semanticweb.owlapi.model.SetOntologyID;
import org.semanticweb.owlapi.util.OWLOntologyImportsClosureSetProvider;
import org.semanticweb.owlapi.util.OWLOntologyMerger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Optional;


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
   * @parameter default-value=null
   */
  private URL catalogURL = null;

  public URL getCatalogURL() {
    return catalogURL;
  }

  public void setCatalogURL(URL catalog) {
    this.catalogURL = catalog;
  }

  public void execute() throws MojoExecutionException {
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

      InputStream is = resolve(owlFile);

      MireotExtractor extractor = new MireotExtractor(ensureFormat(is,
          new RDFXMLDocumentFormat(),
          catalogURL),
          targetNamespace);

      Owl2SkosConverter converter = new Owl2SkosConverter(skosNamespace,
          profile);

      Optional<Model> skosModel = extractor.fetch(targetURI,
          entityType,
          entityOnly,
          minDepth,
          maxDepth).flatMap((ext) -> converter.run(ext, reason, false));

      if (skosModel.isPresent()) {
        if (!getOutputDirectory().exists()) {
          getOutputDirectory().mkdirs();
        }
        File f = new File(getOutputDirectory()
            + File.separator
            + getSkosOutputFile());
        FileOutputStream fos = new FileOutputStream(f);
        skosModel.get().write(fos);
      } else {
        getLog().error("Unable to generate a SKOS model");
      }

    } catch (Exception e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
      throw new MojoExecutionException(e.getMessage(), e);
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


  public static InputStream ensureFormat(InputStream is, OWLDocumentFormat fmt, URL catalogURL) {
    try {
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      manager.setOntologyConfigurator(new OntologyConfigurator()
          .setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT));

      if (catalogURL != null) {
        XMLCatalog catalog = CatalogUtilities.parseDocument(catalogURL);
        manager.setIRIMappers(Collections.singleton(new XMLCatalogIRIMapper(catalog)));
      }

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      OWLOntology onto = manager.loadOntologyFromOntologyDocument(is);
      OWLOntologyID originalId = onto.getOntologyID();

      OWLOntologyMerger merger = new OWLOntologyMerger(
          new OWLOntologyImportsClosureSetProvider(manager, onto));
      onto = merger.createMergedOntology(OWLManager.createOWLOntologyManager(),
          onto.getOntologyID().getOntologyIRI().get());
      onto.getOWLOntologyManager().applyChange(new SetOntologyID(onto, originalId));
      onto.saveOntology(fmt, baos);

      onto.saveOntology(System.out);
      return new ByteArrayInputStream(baos.toByteArray());

    } catch (Exception e) {
      e.printStackTrace();
      return is;
    }
  }

}



