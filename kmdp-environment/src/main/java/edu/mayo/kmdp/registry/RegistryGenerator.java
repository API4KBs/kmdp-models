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
package edu.mayo.kmdp.registry;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBeanBuilder;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.DC_11;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class RegistryGenerator {

  private static final Logger logger = LogManager.getLogger(RegistryGenerator.class);

  private static final Resource constructedLanguage = ResourceFactory
      .createResource("https://www.omg.org/spec/API4KP/api4kp/ConstructedLanguage");
  private static final Resource baseLanguage = ResourceFactory
      .createResource("https://www.omg.org/spec/API4KP/api4kp/BaseConstructedLanguage");
  private static final Resource lexicon = ResourceFactory
      .createResource("https://www.omg.org/spec/API4KP/api4kp/Lexicon");
  private static final Resource metaFormat = ResourceFactory
      .createResource("https://www.omg.org/spec/API4KP/api4kp/MetaFormat");

  private static final Property dependsOn = ResourceFactory
      .createProperty("https://www.omg.org/spec/API4KP/api4kp/depends-on");
  private static final Property governedBy = ResourceFactory
      .createProperty("https://www.omg.org/spec/API4KP/api4kp/governed-by");
  private static final Property usesLexicon = ResourceFactory
      .createProperty("https://www.omg.org/spec/API4KP/api4kp/uses-lexicon");


  private static final Resource OMSLanguage = ResourceFactory
      .createResource("http://www.omg.org/spec/DOL/DOL-terms/OMSLanguage");
  private static final Resource dolProfile = ResourceFactory
      .createResource("http://www.omg.org/spec/DOL/DOL-terms/Profile");
  private static final Resource omsSerialization = ResourceFactory
      .createResource("http://www.omg.org/spec/DOL/DOL-terms/Serialization");

  private static final Property isProfileOf = ResourceFactory
      .createProperty("http://www.omg.org/spec/DOL/DOL-terms/isProfileOf");
  private static final Property supportSerialization = ResourceFactory
      .createProperty("http://www.omg.org/spec/DOL/DOL-terms/supportsSerialization");

  private static final String REGISTRY_DB = "/home/davide/Projects/API4KB/api4kbs/src/main/resources/API4KP-Registry.xlsx";
  private static final String REGISTRY_ONTO = "/home/davide/Projects/API4KB/api4kbs/ontologies/API4KP/informative/api4kp-registry.rdf";

  public static void main(String... args) {
    try (
        InputStream in = new FileInputStream(REGISTRY_DB);
        FileOutputStream fos = new FileOutputStream(REGISTRY_ONTO)) {
      new RegistryGenerator().generate(in, fos);
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }

  public Model generate(InputStream in, OutputStream out) {
    Optional<byte[]> inBytes = readBytes(in);
    if (!inBytes.isPresent()) {
      logger.error("Unable to access Registry KB");
      return null;
    }

    Context ctx = new Context();
    ctx.prefixes = parseBeans(new ByteArrayInputStream(inBytes.get()),
        5, PrefixRes.class).stream().collect(
        Collectors.toMap(PrefixRes::getPrefix, PrefixRes::getNamespace));
    OntModel registry = initOntology(ctx.prefixes);

    ctx.langs = parseBeans(new ByteArrayInputStream(inBytes.get()),
        0, LanguageRes.class);
    ctx.profiles = parseBeans(new ByteArrayInputStream(inBytes.get()),
        1, ProfileRes.class);
    ctx.serializations = parseBeans(new ByteArrayInputStream(inBytes.get()),
        2, SerializationRes.class);
    ctx.lexicons = parseBeans(new ByteArrayInputStream(inBytes.get()),
        3, LexiconRes.class);
    ctx.formats = parseBeans(new ByteArrayInputStream(inBytes.get()),
        4, FormatRes.class);

    ctx.langs.forEach(l -> toLanguage(l, ctx, registry));
    ctx.profiles.forEach(p -> toProfile(p, ctx, registry));
    ctx.lexicons.forEach(l -> toLexicon(l, registry));
    ctx.formats.forEach(f -> toFormat(f, registry));
    ctx.serializations.forEach(s -> toSerialization(s, ctx, registry));

    try {
      registry.write(out);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }

    return registry;
  }


  private void toSerialization(SerializationRes serial, Context ctx,
      OntModel registry) {
    Resource subj = describe(serial,registry);
    registry.add(objA(subj, RDF.type, omsSerialization));

    if (isNotBlank(serial.getLanguage())) {

      Optional<String> obj = joinSerialization(serial.getLanguage(), ctx);
      if (!obj.isPresent()) {
        throw new IllegalStateException(
            "Unable to link serialization " + serial.getName() + " to language " + serial
                .getLanguage());
      }

      registry.add(objA(ResourceFactory.createResource(obj.get()), supportSerialization, subj));
    }

    if (isNotBlank(serial.getFormat())) {
      String fmt = serial.getFormat();
      Optional<FormatRes> sup = ctx.formats.stream()
          .filter(f -> f.getIdentifier().equals(fmt))
          .findAny();
      if (!sup.isPresent()) {
        throw new IllegalStateException(
            "Serialization " + serial.getSerialization() + " of no format " + serial.getFormat());
      }
      String obj = asSubject(sup.get());
      registry.add(objA(subj, dependsOn, ResourceFactory.createResource(obj)));
    }
  }

  private Optional<String> joinSerialization(String lang, Context ctx) {
    Optional<? extends OntoRes> sup = ctx.langs.stream().filter(l -> joins(l, lang)).findAny();
    if (!sup.isPresent()) {
      sup = ctx.profiles.stream().filter(l -> joins(l, lang)).findAny();
    }
    return sup.map(this::asSubject);
  }

  private boolean joins(OntoRes l, String lang) {
    String vTag = "";
    String lTag;
    if (lang.contains("-")) {
      vTag = lang.substring(lang.indexOf('-') + 1);
      lTag = lang.substring(0, lang.indexOf('-'));
    } else {
      lTag = lang;
    }
    if (!lTag.equals(l.getIdentifier())) {
      return false;
    }
    return StringUtils.isBlank(vTag) || vTag.equals(l.getVersionTag());
  }

  private void toFormat(FormatRes fmt, OntModel registry) {
    Resource subj = describe(fmt, registry);
    registry.add(objA(subj, RDF.type, metaFormat));
  }

  private void toLexicon(LexiconRes lex, OntModel registry) {
    Resource subj = describe(lex, registry);
    registry.add(objA(subj, RDF.type, lexicon));
  }

  private void toProfile(ProfileRes profile, Context ctx, OntModel registry) {
    Resource subj = describe(profile, registry);
    registry.add(objA(subj, RDF.type, dolProfile));
    if (isNotBlank(profile.getLanguage())) {
      String lang = profile.getLanguage();
      Optional<LanguageRes> sup = ctx.langs.stream().filter(l -> l.getIdentifier().equals(lang))
          .findAny();
      if (!sup.isPresent()) {
        throw new IllegalStateException(
            "Profile " + profile.getProfile() + " of no language " + profile.getLanguage());
      }
      String obj = asSubject(sup.get());
      registry.add(objA(subj, isProfileOf, ResourceFactory.createResource(obj)));
    }
  }


  private void toLanguage(LanguageRes language, Context ctx, OntModel registry) {
    Resource subj = describe(language, registry);
    registry.add(objA(subj, RDF.type, baseLanguage));

    if (language.isOms()) {
      registry.add(objA(subj, RDF.type, OMSLanguage));
    }

    Arrays.stream(language.getLexica().split(",")).forEach(lex -> {
      if (isNotBlank(lex)) {
        Optional<LexiconRes> lexiconTerm = ctx.lexicons.stream()
            .filter(l -> lex.equals(l.getIdentifier())).findFirst();
        if (!lexiconTerm.isPresent()) {
          throw new IllegalStateException(
              "Unmatched lexicon " + lex + " for language " + language.getLanguage());
        }
        String obj = asSubject(lexiconTerm.get());
        registry.add(objA(subj, usesLexicon, ResourceFactory.createResource(obj)));
      }
    });

  }

  private Resource describe(OntoRes res, OntModel registry) {
    String subject = asSubject(res);

    Resource subj = ResourceFactory.createResource(subject);

    if (isNotBlank(res.getLabel())) {
      registry.add(datA(subj, RDFS.label, res.getLabel()));
    }
    if (isNotBlank(res.getPrefLabel())) {
      registry.add(datA(subj, SKOS.prefLabel, res.getPrefLabel()));
    }
    if (isNotBlank(res.getIdentifier())) {
      registry.add(datA(subj, DC_11.identifier, res.getIdentifier()));
    }
    if (isNotBlank(res.getComment())) {
      registry.add(datA(subj, RDFS.comment, res.getComment()));
    }
    if (isNotBlank(res.getSource())) {
      registry.add(datA(subj, DC_11.source, res.getSource()));
    }

    if (isNotBlank(res.getVersionTag())) {
      registry.add(datA(subj, OWL2.versionInfo, res.getVersionTag()));
    }
    return subj;
  }


  private String asSubject(OntoRes res) {
    if (isBlank(res.getUri())) {
      throw new IllegalStateException("Unable to find URI for " + res.getLabel());
    }
    return res.getUri();
  }


  private Optional<byte[]> readBytes(InputStream in) {
    try {
      int numBytes = in.available();
      byte[] b = new byte[numBytes];
      int actualBytes = in.read(b);
      if (numBytes != actualBytes) {
        logger.error("Unable to read Registry source, read {} out of {} bytes",actualBytes,numBytes);
        return Optional.empty();
      }
      return Optional.of(b);
    } catch (IOException e) {
      logger.error(e.getMessage(),e);
      return Optional.empty();
    }
  }

  private OntModel initOntology(Map<String, String> prefixes) {
    OntModel registry = ModelFactory.createOntologyModel();

    registry.setNsPrefixes(prefixes);
    Ontology onto = registry.createOntology("http://ontology.mayo.edu/ontologies/kmdp-registry/");
    registry.add(
        objA(onto, OWL2.versionIRI,
            ResourceFactory.createResource("http://ontology.mayo.edu/ontologies/20190801/kmdp-registry/")));

    registry.add(objA(constructedLanguage, RDF.type, OWL2.Class));
    registry.add(objA(baseLanguage, RDF.type, OWL2.Class));
    registry.add(objA(lexicon, RDF.type, OWL2.Class));
    registry.add(objA(metaFormat, RDF.type, OWL2.Class));

    registry.add(objA(dolProfile, RDF.type, OWL2.Class));
    registry.add(objA(dolProfile, RDFS.subClassOf, OMSLanguage));
    registry.add(objA(dolProfile, RDFS.subClassOf, constructedLanguage));
    registry.add(objA(baseLanguage, RDFS.subClassOf, constructedLanguage));

    registry.add(objA(OMSLanguage, RDF.type, OWL2.Class));
    registry.add(objA(omsSerialization, RDF.type, OWL2.Class));

    registry.add(objA(isProfileOf, RDF.type, OWL2.ObjectProperty));
    registry.add(objA(supportSerialization, RDF.type, OWL2.ObjectProperty));
    registry.add(objA(governedBy, RDF.type, OWL2.ObjectProperty));
    registry.add(objA(dependsOn, RDF.type, OWL2.ObjectProperty));
    registry.add(objA(usesLexicon, RDF.type, OWL2.ObjectProperty));

    return registry;
  }

  private Statement objA(Resource res, Property prop, Resource obj) {
    return ResourceFactory.createStatement(res, prop, obj);
  }

  private Statement datA(Resource subj, Property prop, String val) {
    return ResourceFactory.createStatement(subj, prop, ResourceFactory.createStringLiteral(val));
  }

  private <T> List<T> parseBeans(InputStream source, int sheet, Class<T> klass) {
    try {
      byte[] csv = convertToCSV(new XSSFWorkbook(source), sheet);
      return new ArrayList<>(
          new CsvToBeanBuilder<T>(new InputStreamReader(new ByteArrayInputStream(csv)))
              .withType(klass)
              .withSeparator(';')
              .build()
              .parse());
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      return new ArrayList<>();
    }
  }

  private byte[] convertToCSV(XSSFWorkbook workbook, int index) {
    final Sheet sheet = workbook.getSheetAt(index);
    int length = sheet.getRow(0).getPhysicalNumberOfCells();
    return saveCSVFile( IntStream.range( 0, sheet.getLastRowNum() + 1 )
            .mapToObj( j -> rowToCSV( sheet.getRow(j), length) )
            .collect( Collectors.toList() ));
  }

  private byte[] saveCSVFile( final List<List<String>> csvData) {
    return csvData.stream()
        .map( line -> String.join(";", line))
        .collect( Collectors.joining( System.lineSeparator() ) )
        .getBytes();
  }


  private List<String> rowToCSV(Row row, int length) {
    if ( row == null ) {
      return Collections.emptyList();
    }
    return IntStream.range( 0, length )
        .mapToObj( row::getCell )
        .map( xell -> xell != null ? xell.getStringCellValue() : "" )
        .collect( Collectors.toList() );
  }

  public static class Context {

    Map<String, String> prefixes;

    List<LanguageRes> langs;
    List<ProfileRes> profiles;
    List<SerializationRes> serializations;
    List<LexiconRes> lexicons;
    List<FormatRes> formats;
  }

  public abstract static class OntoRes {

    @CsvBindByName(column = "URI")
    String uri;

    @CsvBindByName(column = "Label")
    String label;

    @CsvBindByName(column = "PrefLabel")
    String prefLabel;

    @CsvBindByName(column = "Version")
    String version;

    @CsvBindByName(column = "Identifier")
    String identifier;

    @CsvBindByName(column = "Comment")
    String comment;

    @CsvBindByName(column = "Source")
    String source;

    @CsvBindByName(column = "Scope")
    String scope;

    @CsvBindByName(column = "Version Tag")
    String versionTag;

    abstract String getName();

    public String getUri() {
      return uri;
    }

    public void setUri(String uri) {
      this.uri = uri;
    }

    public String getLabel() {
      return label;
    }

    public void setLabel(String label) {
      this.label = label;
    }

    public String getPrefLabel() {
      return prefLabel;
    }

    public void setPrefLabel(String prefLabel) {
      this.prefLabel = prefLabel;
    }

    public String getVersion() {
      return version;
    }

    public void setVersion(String version) {
      this.version = version;
    }

    public String getIdentifier() {
      return identifier;
    }

    public void setIdentifier(String identifier) {
      this.identifier = identifier;
    }

    public String getComment() {
      return comment;
    }

    public void setComment(String comment) {
      this.comment = comment;
    }

    public String getSource() {
      return source;
    }

    public void setSource(String source) {
      this.source = source;
    }

    public String getScope() {
      return scope;
    }

    public void setScope(String scope) {
      this.scope = scope;
    }

    public String getVersionTag() {
      return versionTag;
    }

    public void setVersionTag(String versionTag) {
      this.versionTag = versionTag;
    }
  }


  public static class LanguageRes extends OntoRes {

    @CsvBindByName(column = "Language")
    String language;

    @CsvBindByName(column = "OMS")
    boolean oms;

    @CsvBindByName(column = "Lexicon")
    String lexica;

    protected String getName() {
      return getLanguage();
    }

    public String getLanguage() {
      return language;
    }

    public void setLanguage(String language) {
      this.language = language;
    }

    public boolean isOms() {
      return oms;
    }

    public void setOms(boolean oms) {
      this.oms = oms;
    }

    public String getLexica() {
      return lexica;
    }

    public void setLexica(String lexica) {
      this.lexica = lexica;
    }
  }

  public static class ProfileRes extends OntoRes {

    @CsvBindByName(column = "Profile")
    String profile;

    @CsvBindByName(column = "Language")
    String language;

    String getName() {
      return getProfile();
    }

    public String getProfile() {
      return profile;
    }

    public void setProfile(String profile) {
      this.profile = profile;
    }

    public String getLanguage() {
      return language;
    }

    public void setLanguage(String language) {
      this.language = language;
    }
  }


  public static class SerializationRes extends OntoRes {

    @CsvBindByName(column = "Serialization")
    String serialization;

    @CsvBindByName(column = "Language")
    String language;

    @CsvBindByName(column = "Format")
    String format;

    protected String getName() {
      return getSerialization();
    }

    public String getSerialization() {
      return serialization;
    }

    public void setSerialization(String serialization) {
      this.serialization = serialization;
    }

    public String getLanguage() {
      return language;
    }

    public void setLanguage(String language) {
      this.language = language;
    }

    public String getFormat() {
      return format;
    }

    public void setFormat(String format) {
      this.format = format;
    }
  }

  public static class LexiconRes extends OntoRes {

    @CsvBindByName(column = "Lexicon")
    String lexicon;

    protected String getName() {
      return getLexicon();
    }

    public String getLexicon() {
      return lexicon;
    }

    public void setLexicon(String lexicon) {
      this.lexicon = lexicon;
    }
  }


  public static class FormatRes extends OntoRes {

    @CsvBindByName(column = "Format")
    String format;

    protected String getName() {
      return getFormat();
    }

    public String getFormat() {
      return format;
    }

    public void setFormat(String format) {
      this.format = format;
    }
  }

  public static class PrefixRes {

    @CsvBindByName(column = "Prefix")
    String prefix;

    @CsvBindByName(column = "Namespace")
    String namespace;

    public String getPrefix() {
      return prefix;
    }

    public void setPrefix(String prefix) {
      this.prefix = prefix;
    }

    public String getNamespace() {
      return namespace;
    }

    public void setNamespace(String namespace) {
      this.namespace = namespace;
    }
  }


}
