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


import org.omg.spec.api4kp._20200801.terms.ConceptScheme;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig.EnumGenerationParams;
import edu.mayo.kmdp.terms.generator.internal.ConceptGraph;
import java.io.File;
import java.util.Map;
import org.omg.spec.api4kp._20200801.id.Term;

public class XSDEnumTermsGenerator extends BaseEnumGenerator {

  public XSDEnumTermsGenerator() {
    super();
  }

  public XSDEnumTermsGenerator(BaseEnumGenerator other) {
    super(other);
  }

  public void generate(ConceptGraph conceptGraph,
      EnumGenerationConfig options,
      File outputDir) {
    outputDir.mkdirs();

    this.generateConcepts(conceptGraph, options, outputDir);
  }

  protected void generateConcepts(ConceptGraph conceptGraph,
      EnumGenerationConfig options,
      File outputDir) {

    for (ConceptScheme<Term> conceptScheme : conceptGraph.getConceptSchemes()) {

      Map<String, Object> context = getContext(conceptScheme, options, conceptGraph);

      generateXSD(context, outputDir);
      if (Boolean.TRUE.equals(options.getTyped(EnumGenerationParams.WITH_JAXB, Boolean.class))) {
        generateXJB(context, outputDir);
      }
    }
  }

  private void generateXSD(Map<String, Object> context, File outputDir) {
    String mainText = fromTemplate("concepts-xsd", context);

    this.writeToFile(mainText,
        getFile(outputDir, context, ".xsd"));

    if (! context.get("namespace").equals(context.get("seriesNamespace"))) {
      String seriesText = fromTemplate("concepts-xsd-series", context);
      this.writeToFile(seriesText,
          getFile(outputDir, (String) context.get("intfPackageName"),
              (String) context.get("typeName"), ".series.xsd"));
    }
  }

  private void generateXJB(Map<String, Object> context, File outputDir) {
    String mainText = fromTemplate("concepts-xjb", context);

    this.writeToFile(mainText,
        getFile(outputDir, context, ".xjb"));

    if (! context.get("namespace").equals(context.get("seriesNamespace"))) {
      String seriesText = fromTemplate("concepts-xjb-series", context);
      this.writeToFile(seriesText,
          getFile(outputDir, (String) context.get("intfPackageName"),
              (String) context.get("typeName"), ".series.xjb"));
    }
  }


}
