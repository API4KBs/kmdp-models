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
package edu.mayo.kmdp.terms.generator;

import static edu.mayo.kmdp.util.CatalogBasedURIResolver.catalogResolver;
import static edu.mayo.kmdp.util.CodeGenTestUtil.applyJaxb;
import static edu.mayo.kmdp.util.CodeGenTestUtil.deploy;
import static edu.mayo.kmdp.util.CodeGenTestUtil.ensureSuccessCompile;
import static edu.mayo.kmdp.util.CodeGenTestUtil.getNamedClass;
import static edu.mayo.kmdp.util.CodeGenTestUtil.initGenSourceFolder;
import static edu.mayo.kmdp.util.CodeGenTestUtil.initSourceFolder;
import static edu.mayo.kmdp.util.CodeGenTestUtil.initTargetFolder;
import static edu.mayo.kmdp.util.CodeGenTestUtil.showDirContent;
import static edu.mayo.kmdp.util.URIUtil.asURI;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.mayo.kmdp.terms.MockTermsJsonAdapter;
import edu.mayo.kmdp.terms.MockTermsXMLAdapter;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig.EnumGenerationParams;
import edu.mayo.kmdp.terms.generator.config.SkosAbstractionConfig;
import edu.mayo.kmdp.terms.generator.config.SkosAbstractionConfig.SkosAbstractionParameters;
import edu.mayo.kmdp.terms.generator.internal.ConceptGraph;
import edu.mayo.kmdp.util.CatalogBasedURIResolver;
import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.XMLUtil;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.validation.Schema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.omg.spec.api4kp._20200801.id.ObjectFactory;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.terms.ConceptScheme;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.w3c.dom.Document;
import org.w3c.dom.ls.LSResourceResolver;

class JaxbGenerationTest {

  @TempDir
  public Path tmp;


  private static final String parentXSD = "" +
      "<?xml version=\"1.0\" encoding=\"utf-8\"?> " +
      "<xs:schema id=\"Parent\" " +
      "           targetNamespace=\"http://tempuri.org/parent\" " +
      "           elementFormDefault=\"qualified\" " +
      "           xmlns=\"http://tempuri.org/parent\" " +
      "           xmlns:n1=\"http://test/generator/v20180210\" " +
      "           xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"> " +
      "  <xs:import namespace=\"http://test/generator/v20180210\"" +
      "			  schemaLocation=\"test/generator/v20180210/SCH1.xsd\" /> " +
      "  " +
      "  <xs:element name=\"Info\"> " +
      "  <xs:complexType >" +
      "    <xs:sequence> " +
      "      <xs:element name=\"test\" minOccurs=\"0\" maxOccurs=\"unbounded\" type=\"n1:SCH1\"/> "
      +
      "    </xs:sequence> " +
      "  </xs:complexType> " +
      "  </xs:element> " +
      "   " +
      "</xs:schema>";

  private static final String bindings = "" +
      "<jaxb:bindings version=\"2.1\"" +
      "               xmlns:jaxb=\"http://java.sun.com/xml/ns/jaxb\"\n" +
      "               xmlns:xjc=\"http://java.sun.com/xml/ns/jaxb/xjc\"\n" +
      "               xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
      "\n" +
      "  <jaxb:bindings schemaLocation=\"parent.xsd\"\n" +
      "                 node=\"/xs:schema\">\n" +
      "\n" +
      "    <jaxb:bindings node=\"//xs:element[@name='test']\">\n" +
      "      <jaxb:class ref=\"test.generator.v20180210.SCH1\" />\n" +
      "    </jaxb:bindings>\n" +
      "\n" +
      "  </jaxb:bindings>\n" +
      "</jaxb:bindings>";

  @Test
  @SuppressWarnings("unchecked")
  void testJaxbGeneration() {
    File tgt = compile();

    Class<?> info = getNamedClass("org.tempuri.parent.Info", tgt);
    Class<?> scheme = getNamedClass("test.generator.v20180210.SCH1", tgt);
    assertFalse(info.isEnum());
    assertNotNull(scheme);

    try {
      Object i = info.getConstructor().newInstance();
      Field fld1 = info.getDeclaredField("test");

      assertNotNull(fld1);
      assertEquals("java.util.List", fld1.getType().getName());

      Method m = info.getMethod("getTest");

      // Due to clashes beetween the CL used for compilation, and the thread's CL,
      // the needed classes are accessed using reflection
      ParameterizedType t = ((ParameterizedType) m.getGenericReturnType());
      Class<?> arg = (Class<?>) t.getActualTypeArguments()[0];
      assertEquals("test.generator.v20180210.SCH1", arg.getName());
      assertTrue(arg.isEnum());

      Object values = arg.getMethod("values").invoke(null);
      assertTrue(values.getClass().isArray());

      ((List<Object>) m.invoke(i)).add(Array.get(values, 0));

      String x = JaxbUtil.marshallToString(Collections.singleton(ObjectFactory.class),
          i,
          JaxbUtil.defaultProperties());
      //System.out.println(x);

      Optional<Schema> schema = XMLUtil
          .getSchemas(
              new File(tgt.getParent() + "/test/parent.xsd").toURI().toURL(),
              getResolver(tgt.getParentFile().toURI().toURL()));
      assertTrue(schema.isPresent());
      XMLUtil.validate(x, schema.get());


    } catch (InstantiationException | IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException | MalformedURLException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  private LSResourceResolver getResolver(URL baseLoc) {
    return new CatalogBasedURIResolver(
        baseLoc,
        catalogResolver(
            asURI(JaxbGenerationTest.class.getResource("/xsd/api4kp-catalog.xml"))));
  }


  @Test
  void testXSDConfig() {
    XSDEnumTermsGenerator xsdGen = new XSDEnumTermsGenerator();
    ConceptGraph conceptGraph = doAbstract();

    assertNotNull(conceptGraph);
    assertNotNull(conceptGraph.getConceptSchemes());
    assertFalse(conceptGraph.getConceptSchemes().isEmpty());

    ConceptScheme<Term> scheme = conceptGraph.getConceptSchemes().iterator().next();
    Map<String, Object> context = xsdGen.getContext(scheme, new EnumGenerationConfig(),
        conceptGraph);
    String xsd = xsdGen.fromTemplate("concepts-xsd", context);

    Optional<Document> odox = XMLUtil.loadXMLDocument(xsd.getBytes());
    assertTrue(odox.isPresent());

    Pattern p = Pattern.compile("xs:enumeration", Pattern.LITERAL);
    Matcher m = p.matcher(xsd);
    int j = 0;
    while (m.find()) {
      j = ++j;
    }
    assertEquals(3, j);
  }


  @Test
  void testJaxbConfig() {
    XSDEnumTermsGenerator xsdGen = new XSDEnumTermsGenerator();
    ConceptGraph conceptGraph = doAbstract();

    assertNotNull(conceptGraph);
    assertNotNull(conceptGraph.getConceptSchemes());
    assertFalse(conceptGraph.getConceptSchemes().isEmpty());

    ConceptScheme<Term> scheme = conceptGraph.getConceptSchemes().iterator().next();
    Map<String, Object> context = xsdGen.getContext(scheme, new EnumGenerationConfig(),
        conceptGraph);
    String jxb = xsdGen.fromTemplate("concepts-xjb", context);

    Optional<Document> jdox = XMLUtil.loadXMLDocument(jxb.getBytes());
    assertTrue(jdox.isPresent());
  }


  @Test
  void testJava() {
    XSDEnumTermsGenerator xsdGen = new XSDEnumTermsGenerator();
    ConceptGraph conceptGraph = doAbstract();

    assertNotNull(conceptGraph);
    assertNotNull(conceptGraph.getConceptSchemes());
    assertFalse(conceptGraph.getConceptSchemes().isEmpty());

    ConceptScheme<Term> scheme = conceptGraph.getConceptSchemes().iterator().next();
    Map<String, Object> context = xsdGen.getContext(scheme,
        new EnumGenerationConfig()
            .with(EnumGenerationParams.WITH_JAXB, true)
            .with(EnumGenerationParams.WITH_JSON, true)
            .with(EnumGenerationParams.WITH_JSONLD, true),
        conceptGraph);
    String java = xsdGen.fromTemplate("concepts-java", context);

    assertTrue(java.contains("package test.generator.v20180210;"));
  }


  @SuppressWarnings("deprecation")
  private File compile() {
    File folder = tmp.toFile();

    File src = initSourceFolder(folder);
    File gen = initGenSourceFolder(folder);
    File tgt = initTargetFolder(folder);
    EnumGenerationConfig opts = config()
        .with(EnumGenerationParams.JSON_ADAPTER, MockTermsJsonAdapter.class.getName())
        .with(EnumGenerationParams.XML_ADAPTER, MockTermsXMLAdapter.class.getName());

    deploy(new ByteArrayInputStream(parentXSD.getBytes()), src, "/parent.xsd");
    deploy(new ByteArrayInputStream(bindings.getBytes()), src, "/bindings.xjb");
    deploy(src, "/xsd/API4KP/api4kp/id/id.xsd");
    deploy(src, "/xsd/API4KP/api4kp/datatypes/datatypes.xsd");
    deploy(src, "/xsd/api4kp-catalog.xml");

    ConceptGraph graph = doAbstract();
    doGenerate(graph, opts, src);

//		printSourceFile( new File( src.getAbsolutePath() + "/test/generator/v20180210/SCH1.xsd"), System.out );
//		printSourceFile( new File( src.getAbsolutePath() + "/test/generator/v20180210/SCH1.java"), System.out );

    showDirContent(folder, true);

    applyJaxb(singletonList(
            new File(src.getAbsolutePath() + "/parent.xsd")),
        Arrays.asList(
            new File(src.getAbsolutePath() + "/test/generator/v20180210/SCH1.xjb"),
            new File(src.getAbsolutePath() + "/bindings.xjb")
        ),
        gen,
        null,
        singletonList(new File(src.getAbsolutePath() + "/xsd/api4kp-catalog.xml")),
        src,
        false, false);

    showDirContent(folder, true);

    purge(gen);

    showDirContent(folder, true);

//		printSourceFile( new File( src.getAbsolutePath() + "/org/tempuri/parent/Info.java"), System.out );
//		printSourceFile( new File( src.getAbsolutePath() + "/org/tempuri/parent/ObjectFactory.java"), System.out );

    ensureSuccessCompile(src, gen, tgt);

    showDirContent(folder, true);

    return tgt;
  }

  private void purge(File gen) {
    // In real scenarios, one would pass the episode file and the dependency to the already compiled datatypes.
    // For testing purposes, we let JaxB regenerate the files, then delete them before compilation
    File omg = new File(gen.getAbsolutePath() + "/org/omg");
    if (omg.exists()) {
      assertTrue(omg.isDirectory());
      FileUtil.delete(omg);
    }
  }


  private void doGenerate(ConceptGraph graph, EnumGenerationConfig opts,
      File tgt) {
    new JavaEnumTermsGenerator().generate(graph, opts, tgt);
    new XSDEnumTermsGenerator().generate(graph, opts, tgt);
  }

  private ConceptGraph doAbstract() {
    try {
      OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();
      OWLOntology o = owlOntologyManager.loadOntologyFromOntologyDocument(
          JaxbGenerationTest.class.getResourceAsStream("/test.owl"));

      return new SkosTerminologyAbstractor()
          .traverse(o, new SkosAbstractionConfig()
              .with(SkosAbstractionParameters.VERSION_PATTERN, ".*/(.*)")
              .with(SkosAbstractionParameters.REASON, true));
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    return null;
  }


  private EnumGenerationConfig config() {
    return new EnumGenerationConfig().with(EnumGenerationParams.WITH_JAXB, "true");
  }

}
