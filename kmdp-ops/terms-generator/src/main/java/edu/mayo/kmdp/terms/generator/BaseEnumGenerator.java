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

import static edu.mayo.kmdp.util.NameUtils.namespaceURIStringToPackage;
import static edu.mayo.kmdp.util.NameUtils.removeTrailingPart;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.ConceptScheme;
import edu.mayo.kmdp.terms.TermsJsonAdapter;
import edu.mayo.kmdp.terms.TermsXMLAdapter;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig.EnumGenerationParams;
import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.NameUtils;
import edu.mayo.kmdp.util.PropertiesUtil;
import edu.mayo.kmdp.util.Util;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public abstract class BaseEnumGenerator {

  protected static Map<String, Template> registry = new HashMap<>();

  static {
    prepareTemplates();
  }

  protected BaseEnumGenerator() {
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

  private static Template getResource(String templ) {
    String s = FileUtil.read(BaseEnumGenerator.class.getResourceAsStream("/templates/" + templ))
        .orElse("Mustache Template " + templ + " not found");
    return Mustache.compiler().compile(s);
  }


  protected Map<String, Object> getContext(ConceptScheme<Term> conceptScheme,
      EnumGenerationConfig options,
      SkosTerminologyAbstractor.ConceptGraph graph) {

    Boolean jaxb = options.getTyped(EnumGenerationParams.WITH_JAXB);
    Boolean jsonld = options.getTyped(EnumGenerationParams.WITH_JSONLD);
    Boolean json = options.getTyped(EnumGenerationParams.WITH_JSON);

    String defaultPackage = options.getTyped(EnumGenerationParams.PACKAGE_NAME);
    Properties overrides = PropertiesUtil.doParse(options.getTyped(EnumGenerationParams.PACKAGE_OVERRIDES));

    String className = conceptScheme.getPublicName();
    String innerPackageName = getPackageName(conceptScheme, defaultPackage, overrides);

    Map<String, Object> context = new HashMap<>();
    context.put("conceptScheme", conceptScheme);
    context.put("conceptSchemeID", NameUtils.getTrailingPart(conceptScheme.getId().toString()));
    context.put("concepts", graph.getConceptList(conceptScheme.getId()));
    context.put("typeName", className);
    context.put("namespace", edu.mayo.kmdp.util.NameUtils.removeFragment(conceptScheme.getVersionId()));
    context.put("packageName", innerPackageName);
    context.put("overridePk", overridePk(defaultPackage,overrides));
    context.put("baseJsonAdapter", options.get(EnumGenerationParams.JSON_ADAPTER)
        .orElse(TermsJsonAdapter.Deserializer.class.getName()));
    context.put("baseXmlAdapter", options.get(EnumGenerationParams.XML_ADAPTER)
        .orElse(TermsXMLAdapter.class.getName()));
    context.put("implClassName",
        edu.mayo.kmdp.terms.impl.model.AnonymousConceptScheme.class.getName());
    context.put("typeIntf", Term.class);

    context.put("jaxb", jaxb);
    context.put("jsonld", jsonld);
    context.put("json", json);

    return context;
  }

  private Mustache.Lambda overridePk(String defaultPackage, Properties overrides) {
    return (frag, out) -> {
      String key = frag.execute();
      out.write(getPackageName(key,defaultPackage,overrides));
    };
  }

  protected String getPackageName(ConceptScheme<Term> conceptScheme, String defaultPackage, Properties packageNameOverrides) {
    String packageName = namespaceURIStringToPackage(removeTrailingPart(conceptScheme.getVersionId().toString()));
    return getPackageName(packageName,defaultPackage,packageNameOverrides);
  }

  protected String getPackageName(String nativePackage, String defaultPackage, Properties packageNameOverrides) {
    if (!Util.isEmpty(defaultPackage)) {
      return defaultPackage;
    }
    return packageNameOverrides.getOrDefault(nativePackage,nativePackage).toString();
  }


  protected String fromTemplate(String templateId, Map<String, Object> context) {
    StringWriter sw = new StringWriter();
    Template m = registry.get(templateId);
    if (m == null) {
      return "Mustache Template " + templateId + " not found";
    }
    m.execute(context, sw);
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

    try(FileWriter writer = new FileWriter(outputFile)) {
      writer.write(content);
      writer.flush();
    } catch (IOException e) {
      throw new EnumGenerationException(e);
    }
  }

  protected class EnumGenerationException extends RuntimeException {
    public EnumGenerationException(Exception e) {
      super(e);
    }
  }
}
