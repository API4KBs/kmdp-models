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

import edu.mayo.kmdp.id.LexicalIdentifier;
import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.ConceptScheme;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig.EnumGenerationParams;
import edu.mayo.kmdp.util.NameUtils;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.SimpleTemplateRegistry;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class BaseEnumGenerator {

  protected SimpleTemplateRegistry registry;

  public BaseEnumGenerator() {
    registry = new SimpleTemplateRegistry();
    prepareTemplates();
  }

  private void prepareTemplates() {
    registry.addNamedTemplate("concepts-java",
        TemplateCompiler.compileTemplate(getResource("concepts-java.mvel")));
    registry.addNamedTemplate("concepts-xsd",
        TemplateCompiler.compileTemplate(getResource("concepts-xsd.mvel")));
    registry.addNamedTemplate("concepts-xjb",
        TemplateCompiler.compileTemplate(getResource("concepts-xjb.mvel")));
    registry.addNamedTemplate("catalog",
        TemplateCompiler.compileTemplate(getResource("catalog.mvel")));
  }

  private InputStream getResource(String templ) {
    return JavaEnumTermsGenerator.class.getResourceAsStream("/templates/" + templ);
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
    context.put("ancestors",
        ((SkosTerminologyAbstractor.MutableConceptScheme) conceptScheme).getAncestorsMap());
    context.put("closure", graph.getClosure());
    context.put("concepts", graph.getConceptList(conceptScheme.getId()));
    context.put("typeName", className);
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
    CompiledTemplate compiled = registry.getNamedTemplate(templateId);

    return (String) TemplateRuntime.execute(compiled, this, context);
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
