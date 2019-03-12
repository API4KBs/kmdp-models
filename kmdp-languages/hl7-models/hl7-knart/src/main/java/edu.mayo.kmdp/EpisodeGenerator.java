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

import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.Util;
import org.hl7.elm.r1.ExpressionDef;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import javax.xml.bind.annotation.XmlType;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

public class EpisodeGenerator {

  public static void main(String... args) {

    Optional<File> src = loadTemplate();
    if (src.isPresent()) {
      File f = src.get();
      File dir = f.getParentFile();
      if (dir.isDirectory()) {
        dir = new File(dir, "META-INF");
        if (!dir.exists()) {
          if (!dir.mkdir()) {
            System.exit(-1);
          }
        }
        File out = new File(dir, "sun-jaxb.episode");
        FileUtil.read(f)
            .map(TemplateCompiler::compileTemplate)
            .map((t) -> TemplateRuntime.execute(t, getContext()))
            .ifPresent((episode) -> FileUtil.write(episode.toString(), out));
      }
    }

  }

  private static Map<String, Object> getContext() {
    HashMap<String, Object> context = new HashMap<>();
    HashMap<String, String> klasses = new HashMap<>();

    String packname = ExpressionDef.class.getPackage().getName();
    String xmlNamespace = ExpressionDef.class.getAnnotation(javax.xml.bind.annotation.XmlType.class)
        .namespace();

    new Reflections(new ConfigurationBuilder()
        .forPackages(packname)
        .setUrls(ClasspathHelper.forClassLoader(ClasspathHelper.staticClassLoader()))
        .setScanners(new SubTypesScanner(false),
            new ResourcesScanner())
//				                                       .filterInputsBy( new FilterBuilder().include( FilterBuilder.prefix( packname ) ) )
    ).getSubTypesOf(Object.class).stream()
        .filter(Objects::nonNull)
        .filter(
            (k) -> k.getPackage() != null && k.getPackage().getName().equalsIgnoreCase(packname))
        .map(EpisodeGenerator::getEntry)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter((e) -> !Util.isEmpty(e.getKey()))
        .forEach((e) -> klasses.put(e.getKey(), e.getValue()));

    context.put("packageName", packname);
    context.put("namespace", xmlNamespace);
    context.put("klasses", klasses);

    return context;
  }

  private static Optional<Map.Entry<String, String>> getEntry(Class<?> k) {
    if (k == null || k.getAnnotation(XmlType.class) == null) {
      return Optional.empty();
    }
    return Optional.of(new AbstractMap.SimpleEntry<>(k.getAnnotation(XmlType.class).name(),
        k.getName()));
  }

  private static Optional<File> loadTemplate() {
    try {
      return Optional.of(new File(EpisodeGenerator.class.getResource("/episode.template").toURI()));
    } catch (URISyntaxException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }
}
