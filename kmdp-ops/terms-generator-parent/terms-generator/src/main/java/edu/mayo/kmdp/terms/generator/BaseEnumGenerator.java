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

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.ConceptScheme;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig.EnumGenerationParams;
import edu.mayo.kmdp.terms.impl.model.InternalTerm;
import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.NameUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class BaseEnumGenerator {

  protected static Map<String,Mustache> registry = new HashMap<>();

  static {
    prepareTemplates();
  }

  public BaseEnumGenerator() {
  }

  private static void prepareTemplates() {
    registry.put("concepts-java",
        getResource("concepts-java.mustache"));
    registry.put("concepts-xsd",
        getResource("concepts-xsd.mustache"));
    registry.put("concepts-xjb",
        getResource("concepts-xjb.mustache"));
    registry.put("catalog",
        getResource("catalog.mustache"));
  }

  private static Mustache getResource(String templ) {
    String s = FileUtil.read(BaseEnumGenerator.class.getResourceAsStream("/templates/" + templ))
        .orElse("Mustache Template " + templ + " not found");
    MustacheFactory mf = new DefaultMustacheFactory();
    return mf.compile(new StringReader(s),templ);
  }


  protected Map<String, Object> getContext(ConceptScheme<Term> conceptScheme,
      EnumGenerationConfig options,
      SkosTerminologyAbstractor.ConceptGraph graph) {

    String packageName = options.getTyped(EnumGenerationParams.PACKAGE_NAME);
    Boolean jaxb = options.getTyped(EnumGenerationParams.WITH_JAXB);
    Boolean jsonld = options.getTyped(EnumGenerationParams.WITH_JSONLD);
    Boolean json = options.getTyped(EnumGenerationParams.WITH_JSON);

    String className = conceptScheme.getPublicName();
    String innerPackageName = getPackageName(conceptScheme, packageName);

    Map<String, Object> context = new HashMap<>();
    context.put("conceptScheme", conceptScheme);
    context.put("concepts", graph.getConceptList(conceptScheme.getId()));
    context.put("typeName", className);
    context.put("namespace", edu.mayo.kmdp.util.NameUtils.removeFragment(conceptScheme.getVersionId()));
    context.put("packageName", innerPackageName);
    context.put("termsProvider", options.get(EnumGenerationParams.TERMS_PROVIDER).get());
    context.put("implClassName",
        edu.mayo.kmdp.terms.impl.model.AnonymousConceptScheme.class.getName());
    context.put("typeIntf", Term.class);

    context.put("jaxb", jaxb);
    context.put("jsonld", jsonld);
    context.put("json", json);

    return context;
  }


  protected String getPackageName(ConceptScheme<Term> conceptScheme, String packageName) {
    return packageName != null && !packageName.isEmpty()
        ? packageName
        : NameUtils.namespaceURIToPackage(conceptScheme.getVersionId().toString());
  }


  protected String fromTemplate(String templateId, Map<String, Object> context) {
    StringWriter sw = new StringWriter();
    Mustache m = registry.get(templateId);
    if (m == null) {
      return "Mustache Template " + templateId + " not found";
    }
    try {
      m.execute(sw, context).flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return sw.toString();
  }



  protected File getFile(File outputDir, Map<String, Object> context, String ext) {
    String packageName = (String) context.get("packageName");
    String fileName = (String) context.get("typeName");

    File packageDir = new File(outputDir, packageName.replace('.', File.separatorChar));
    if (!packageDir.exists()) {
      packageDir.mkdirs();
    }

    return new File(packageDir, fileName + ext);
  }

  protected void writeToFile(
      String content,
      File outputFile) {

    //System.out.println( content );
    FileWriter writer = null;
    try {
      writer = new FileWriter(outputFile);
      writer.write(content);
      writer.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

}
