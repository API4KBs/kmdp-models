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
package edu.mayo.kmdp.util;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.sun.tools.xjc.Options;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.validation.constraints.NotNull;
import org.jvnet.mjiip.v_2.XJC2Mojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CodeGenTestBase {

  protected CodeGenTestBase() {}
  
  private static Logger logger = LoggerFactory.getLogger(CodeGenTestBase.class);
  
  private static List<Diagnostic> doCompile(File source, File gen,
      File target) {
    List<File> list = new LinkedList<>();

    explore(source, list);
    if (gen != source) {
      explore(gen, list);
    }

    JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    StandardJavaFileManager fileManager = jc.getStandardFileManager(diagnostics, null, null);
    Iterable<? extends JavaFileObject> compilationUnits =
        fileManager.getJavaFileObjectsFromFiles(list);
    List<String> jcOpts = Arrays.asList("-d", target.getPath());
    JavaCompiler.CompilationTask task = jc
        .getTask(null, fileManager, diagnostics, jcOpts, null, compilationUnits);
    task.call();
    return diagnostics.getDiagnostics().stream()
        .map(Diagnostic.class::cast)
        .collect(Collectors.toList());
  }

  public static void ensureSuccessCompile(File src, File gen, File target) {
    List<Diagnostic> diagnostics = doCompile(src, gen, target);

    boolean success = true;
    for (Diagnostic diag : diagnostics) {
      System.err.println(diag);
      if (logger.isWarnEnabled()) {
        logger.warn(String.format("%s : %s", diag.getKind(), diag));
      }
      if (diag.getKind() == Diagnostic.Kind.ERROR) {
        success = false;
      }
    }
    assertTrue(success);
  }


  public static void showDirContent(File folder) {
    showDirContent(folder,false);
  }

  public static void showDirContent(File folder, boolean enablePrintout) {
    showDirContent(folder, 0, enablePrintout);
  }

  public static void showDirContent(File file, int i, boolean enablePrintout) {
    if (enablePrintout) {
      String msg = String.format("%s : %s",tab(i),file.getName());
      logger.info(msg);

      if (file.isDirectory()) {
        FileUtil.streamChildFiles(file)
            .forEach(sub -> showDirContent(sub, i + 1, enablePrintout));
      }
    }
  }

  private static String tab(int n) {
    StringBuilder sb = new StringBuilder();
    for (int j = 0; j < n; j++) {
      sb.append("\t");
    }
    return sb.toString();
  }

  private static void explore(File dir, List<File> files) {
    for (File f : Util.ensureArray(dir.listFiles(),File.class)) {
      if (f.getName().endsWith(".java")) {
        files.add(f);
      }
      if (f.isDirectory()) {
        explore(f, files);
      }
    }
  }


  public static Class<?> getNamedClass(String name, File tgt) {
    try {
      ClassLoader urlKL = new URLClassLoader(
          new URL[]{tgt.toURI().toURL()},
          Thread.currentThread().getContextClassLoader()
      );

      return Class.forName(name, true, urlKL);
    } catch (Exception e) {
      logger.error(e.getMessage(),e);
      fail(e.getMessage());
    }
    return Object.class;
  }

  public static String deployResource(String resourcePath, File targetFolder, String targetFileName) {
    return deployResource(resourcePath, targetFolder, targetFileName, CodeGenTestBase::read);
  }

  public static byte[] read(InputStream inputStream) {
    Optional<String> content = FileUtil.read(inputStream);
    if (!content.isPresent()) {
      fail("Unable to read file content ");
      return new byte[0];
    } else {
      return content.get().getBytes();
    }
  }


  public static String deployResource(String resourcePath, File targetFolder, String targetFileName,
      Function<InputStream, byte[]> mapper) {
    return deployResource(CodeGenTestBase.class.getResourceAsStream(resourcePath), targetFolder,
        targetFileName, mapper);
  }

  public static String deployResource(InputStream is, File targetFolder, String targetFileName,
      Function<InputStream, byte[]> mapper) {
    assertTrue(targetFolder.exists());
    assertTrue(targetFolder.isDirectory());
    File out = new File(targetFolder.getAbsolutePath() + File.separator + targetFileName);
    FileUtil.write(mapper.apply(is),out);
    return out.getAbsolutePath();
  }


  public static void printSourceFile(File f, PrintStream out) {
    try (FileInputStream inputStream = new FileInputStream(f)) {
      int n = inputStream.available();
      byte[] buf = new byte[n];
      if (n == inputStream.read(buf)) {
        out.println(new String(buf));
      }
    } catch (Exception e) {
      logger.error(e.getMessage(),e);
      fail(e.getMessage());
    }
  }

  public static void applyJaxb(List<File> schemas, List<File> binds, File gen) {
    applyJaxb(schemas, binds, gen, null);
  }

  public static void applyJaxb(List<File> schemas, List<File> binds, File gen, File catalog) {
    applyJaxb(schemas, binds, gen, null, catalog, false, false);
  }

  public static void applyJaxb(List<File> schemas, List<File> binds, File gen,
      boolean withAnnotations) {
    applyJaxb(schemas, binds, gen, null, null, withAnnotations, false);
  }

  public static void applyJaxb(List<File> schemas, List<File> binds, File gen, File episode,
      File catalog, boolean withAnnotations, boolean withExtensions) {
    checkResourcesExist(schemas, binds, gen);

    Options opts = new Options();
    opts.targetDir = gen;
    opts.compatibilityMode = Options.EXTENSION;

    registerSources(opts, schemas, binds);

    checkAndRegisterCatalog(catalog, opts);

    XJC2Mojo mojo = configureMojo(opts, episode, withAnnotations, withExtensions);

    try {
      int n = mojo.getArgs().size();
      opts.parseArguments(mojo.getArgs().toArray(new String[n]));
      mojo.doExecute(opts);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  private static XJC2Mojo configureMojo(Options opts, File episode, boolean withAnnotations,
      boolean withExtensions) {
    XJC2Mojo mojo = new XJC2Mojo();
    mojo.setVerbose(false);
    mojo.setExtension(true);

    mojo.setEpisode(true);
    mojo.setAddIfExistsToEpisodeSchemaBindings(true);
    mojo.setScanDependenciesForBindings(true);
    mojo.setUseDependenciesAsEpisodes(true);
    mojo.setDebug(false);

    if (episode != null) {
      mojo.setEpisodeFile(episode);
    }

    if (withExtensions) {
      mojo.getArgs().add("-Xinheritance");
      mojo.getArgs().add("-Xnamespace-prefix");
      mojo.getArgs().add("-Xfluent-api");
    }

    if (withAnnotations) {
      mojo.getArgs().add("-Xannotate");
      opts.pluginURIs.add("http://annox.dev.java.net");
    }
    return mojo;
  }

  private static void checkAndRegisterCatalog(File catalog, Options opts) {
    if (catalog != null) {
      if (!catalog.exists()) {
        fail("Catalog File Not Found : " + catalog);
      }
      try {
        opts.entityResolver = XMLUtil.catalogResolver(catalog.toURI().toURL());
      } catch (IOException e) {
        logger.error(e.getMessage(),e);
        fail(e.getMessage());
      }
    }
  }

  private static void registerSources(Options opts, List<File> schemas, List<File> binds) {
    schemas.forEach(src -> {
      if (src.isDirectory()) {
        opts.addGrammarRecursive(src);
      } else {
        opts.addGrammar(src);
      }
    });

    binds.forEach(xjb -> {
      if (xjb.isDirectory()) {
        opts.addBindFileRecursive(xjb);
      } else {
        opts.addBindFile(xjb);
      }
    });
  }

  private static void checkResourcesExist(List<File> schemas, List<File> binds, File gen) {
    schemas.forEach(src -> {
      if (!src.exists()) {
        fail("Schema File or Dir Not Found : " + src);
      }
    });
    binds.forEach(xjb -> {
      if (!xjb.exists()) {
        fail("Schema File or Dir Not Found : " + xjb);
      }
    });
    if (!gen.exists() || !gen.isDirectory()) {
      fail("Generated Source Dir Not Found : " + gen);
    }
  }


  public static void deploy(InputStream is, File root, String path) {
    assertTrue(FileUtil.copyTo(is, FileUtil.relativePathToFile(root.getAbsolutePath(), path)));
  }

  public static void deploy(File root, String path) {
    URL def = CodeGenTestBase.class.getResource(path);
    assertTrue(FileUtil.copyTo(def, FileUtil.relativePathToFile(root.getAbsolutePath(), path)));
  }

  public static void deploy(File root, String path, @NotNull Class<?> context) {
    URL def = context.getResource(path);
    assertTrue(FileUtil.copyTo(def, FileUtil.relativePathToFile(root.getAbsolutePath(), path)));
  }

  public static File initSourceFolder(File folder) {
    return initFolder(folder,"test");
  }
  public static File initTargetFolder(File folder) {
    return initFolder(folder,"out");
  }

  public static File initGenSourceFolder(File folder) {
    return initFolder(folder,"gen");
  }

  public static File initFolder(File folder, String subFolder) {
    if ( ! folder.exists()) {
      return null;
    }
    File f = new File(folder,subFolder);
    return f.exists() || f.mkdirs() ? f : null;
  }

}
