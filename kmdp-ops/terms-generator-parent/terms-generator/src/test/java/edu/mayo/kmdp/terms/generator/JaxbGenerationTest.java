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

import edu.mayo.kmdp.terms.example.MockTermsDirectory;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationParams;
import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.XMLUtil;
import org.apache.tools.ant.filters.StringInputStream;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;
import org.junit.rules.TemporaryFolder;
import org.omg.spec.api4kp._1_0.identifiers.ObjectFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import javax.xml.validation.Schema;
import java.io.File;
import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static edu.mayo.kmdp.util.CodeGenTestBase.*;
import static edu.mayo.kmdp.util.XMLUtil.catalogResolver;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

@EnableRuleMigrationSupport
public class JaxbGenerationTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();


  private String parentXSD = "" +
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

  private String bindings = "" +
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
  public void testJaxbGeneration() {
    File tgt = compile();

    Class<?> info = getNamedClass("org.tempuri.parent.Info", tgt);
    Class<?> scheme = getNamedClass("test.generator.v20180210.SCH1", tgt);
    assertFalse(info.isEnum());
    assertNotNull(scheme);

    try {
      Object i = info.newInstance();
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

      ((List) m.invoke(i)).add(Array.get(values, 0));

      String x = JaxbUtil.marshallToString(Collections.singleton(ObjectFactory.class),
          i,
          JaxbUtil.defaultProperties());
      System.out.println(x);

      Optional<Schema> schema = XMLUtil
          .getSchemas(new File(tgt.getParent() + "/test/parent.xsd").toURI().toURL(),
              catalogResolver(JaxbGenerationTest.class.getResource("/xsd/api4kp-catalog.xml")));
      assertTrue(schema.isPresent());
      XMLUtil.validate(x, schema.get());


    } catch (InstantiationException | IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException | MalformedURLException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }


  private File compile() {
    File src = initSourceFolder(folder);
    File gen = initGenSourceFolder(folder);
    File tgt = initTargetFolder(folder);
    EnumGenerationConfig opts = config()
        .with(EnumGenerationParams.TERMS_PROVIDER, MockTermsDirectory.provider);

    deploy(new StringInputStream(parentXSD), src, "/parent.xsd");
    deploy(new StringInputStream(bindings), src, "/bindings.xjb");
    deploy(src, "/xsd/API4KP/api4kp/identifiers/identifiers.xsd");
    deploy(src, "/xsd/api4kp-catalog.xml");

    SkosTerminologyAbstractor.ConceptGraph graph = doAbstract();
    doGenerate(graph, opts, src);

//		printSourceFile( new File( src.getAbsolutePath() + "/test/generator/v20180210/SCH1.xsd"), System.out );
//		printSourceFile( new File( src.getAbsolutePath() + "/test/generator/v20180210/SCH1.java"), System.out );

    showDirContent(folder);

    applyJaxb(singletonList(new File(src.getAbsolutePath() + "/parent.xsd")),
        singletonList(new File(src.getAbsolutePath())),
        gen,
        new File(src.getAbsolutePath() + "/xsd/api4kp-catalog.xml"));

    purge(gen);

    showDirContent(folder);

//		printSourceFile( new File( src.getAbsolutePath() + "/org/tempuri/parent/Info.java"), System.out );
//		printSourceFile( new File( src.getAbsolutePath() + "/org/tempuri/parent/ObjectFactory.java"), System.out );

    ensureSuccessCompile(src, gen, tgt);

    showDirContent(folder);

    return tgt;
  }

  private void purge(File gen) {
    // In real scenarios, one would pass the episode file and the dependency to the already compiled datatypes.
    // For testing purposes, we let JaxB regenerate the files, then delete them before compilation
    File omg = new File(gen.getAbsolutePath() + "/org/omg");
    assertTrue(omg.isDirectory());
    FileUtil.delete(omg);
  }


  private void doGenerate(SkosTerminologyAbstractor.ConceptGraph graph, EnumGenerationConfig opts,
      File tgt) {
    new JavaEnumTermsGenerator().generate(graph, opts, tgt);
    new XSDEnumTermsGenerator().generate(graph, opts, tgt);
  }

  private SkosTerminologyAbstractor.ConceptGraph doAbstract() {
    try {
      OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();
      OWLOntology o = owlOntologyManager.loadOntologyFromOntologyDocument(
          JaxbGenerationTest.class.getResourceAsStream("/test.owl"));

      SkosTerminologyAbstractor generator = new SkosTerminologyAbstractor(o, true);

      return generator.traverse();
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
