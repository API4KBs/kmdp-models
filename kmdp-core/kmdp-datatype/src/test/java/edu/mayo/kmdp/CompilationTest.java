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
package edu.mayo.kmdp;

import static edu.mayo.kmdp.util.CatalogBasedURIResolver.catalogResolver;
import static edu.mayo.kmdp.util.CodeGenTestUtil.applyJaxb;
import static edu.mayo.kmdp.util.CodeGenTestUtil.deploy;
import static edu.mayo.kmdp.util.CodeGenTestUtil.ensureSuccessCompile;
import static edu.mayo.kmdp.util.CodeGenTestUtil.getNamedClass;
import static edu.mayo.kmdp.util.CodeGenTestUtil.initGenSourceFolder;
import static edu.mayo.kmdp.util.CodeGenTestUtil.initSourceFolder;
import static edu.mayo.kmdp.util.CodeGenTestUtil.initTargetFolder;
import static edu.mayo.kmdp.util.CodeGenTestUtil.showDirContent;
import static edu.mayo.kmdp.util.XMLUtil.getSchemas;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.XMLUtil;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import javax.xml.validation.Schema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.omg.spec.api4kp._20200801.id.Pointer;

public class CompilationTest {

  @TempDir
  public Path tmp;

  @Test
  public void testJaxbGeneration() {
    File tgt = compile();

    Class<?> ptrClass = getNamedClass("org.omg.spec.api4kp._20200801.id.resources.Pointer", tgt);
    assertNotNull(ptrClass);
    assertTrue(Pointer.class.isAssignableFrom(ptrClass));

    try {
      Object ptr1 = ptrClass.getConstructor().newInstance();
      URI foo = URI.create("http://mock");
      ptrClass.getMethod("setName", String.class).invoke(ptr1, "Test");
      ptrClass.getMethod("setType", URI.class).invoke(ptr1, foo);
      ptrClass.getMethod("setHref", URI.class).invoke(ptr1, foo);
      ptrClass.getMethod("setTag", String.class).invoke(ptr1, "foo");
      ptrClass.getMethod("setUuid", UUID.class).invoke(ptr1, UUID.randomUUID());
      ptrClass.getMethod("setResourceId", URI.class).invoke(ptr1, foo);

      String xml = JaxbUtil.marshallToString(Collections.singleton(Pointer.class),
          ptr1,
          JaxbUtil.defaultProperties());
      //System.out.println(xml);

      Optional<Schema> schema = getSchemas(
          DatatypeTest.class.getResource("/xsd/API4KP/api4kp/id/id.openapi.xsd"),
          catalogResolver("/xsd/api4kp-catalog.xml"));
      assertTrue(schema.isPresent());
      assertTrue(XMLUtil.validate(xml, schema.get()));

      Optional<Pointer> asPtr = JaxbUtil
          .unmarshall(ptrClass, Pointer.class, xml);
      assertTrue(asPtr.isPresent());
      Pointer p2 = asPtr.get();

      assertEquals("Test", p2.getName());
      assertNotNull(p2.getHref());
      assertNotNull(p2.getType());

      Pointer p3 = new Pointer();
      p2.copyTo(p3);

      assertEquals(p2.getType(), p3.getType());

      Object x = ptrClass.getConstructor().newInstance();

      p3.copyTo(x);
      assertEquals(((Pointer) x).getType(), ((Pointer) ptr1).getType());


    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }


  private File compile() {
    File folder = tmp.toFile();
    assertTrue(folder.exists());

    File src = initSourceFolder(folder);
    File gen = initGenSourceFolder(folder);
    File tgt = initTargetFolder(folder);

    deploy(src, "/xsd/API4KP/api4kp/id/id.openapi.xsd", CompilationTest.class);
    deploy(src, "/xsd/API4KP/api4kp/id/id.xsd", CompilationTest.class);
    deploy(src, "/xsd/API4KP/api4kp/datatypes/datatypes.xsd", CompilationTest.class);

    showDirContent(folder);

    applyJaxb(Collections.singletonList(src), Collections.emptyList(), gen, true);

   // printSourceFile(new File(gen.getAbsolutePath() + "/edu/mayo/kmdp/common/model/Pointer.java"),
     //   System.out);

    ensureSuccessCompile(src, gen, tgt);

    return tgt;
  }


}
