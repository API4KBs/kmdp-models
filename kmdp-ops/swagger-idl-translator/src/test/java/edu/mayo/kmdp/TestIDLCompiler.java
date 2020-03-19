package edu.mayo.kmdp;

import com.sun.tools.corba.ee.idl.toJavaPortable.Compile;
import edu.mayo.kmdp.util.FileUtil;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestIDLCompiler {

  private static final Logger logger = LoggerFactory.getLogger(TestIDLCompiler.class);

  private static Compile compiler;

  static {
    try {
      Class<Compile> compileClass
          = Compile.class;
      Constructor<Compile> constructor
          = compileClass.getDeclaredConstructor();
      constructor.setAccessible(true);

      compiler = constructor.newInstance();
      Compile.compiler = compiler;
    } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
      logger.error(e.getMessage(),e);
    }
  }

  public static String tryCompile(File outputDir, List<File> sourceIDLFiles) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    PrintStream err = System.err;
    System.setErr(new PrintStream(baos));

    int numSources = sourceIDLFiles.size();
    int numArgs = 9;
    String[] args = new String[numArgs];

    args[0] = "-emitAll";

    args[1] = "-i";
    args[2] = sourceIDLFiles.stream()
        .map(File::getParentFile)
        .map(File::getAbsolutePath)
        .collect(Collectors.toSet())
        .stream()
        .collect(Collectors.joining(System.getProperty ("path.separator")));

    args[3] = "-v";

    args[4] = "-corba";
    args[5] = "3.0";

    args[6] = "-td";
    args[7] = outputDir.getAbsolutePath();

    args[8] = sourceIDLFiles.get(numSources-1).getAbsolutePath();

    compiler.start(args);

    System.setErr(err);
    return baos.toString();
  }

  protected TestIDLCompiler() {
    // nothing to do
  }

  public static String tryCompileSource(File tmpRoot, List<String> idlSources) {
    File target = new File(tmpRoot, "output");
    if (target.mkdir()) {
      List<File> sources = idlSources.stream().map(idlSource -> {
        int j = idlSources.indexOf(idlSource);
        String currFileName = "test" + j + ".idl";
        String content = idlSource;
        if (j > 0) {
          String prevFileName = "test" + (j-1) + ".idl";
          content = "#include <" + prevFileName + ">\n" + content;
        }
        File src = new File(tmpRoot, currFileName);
        FileUtil.write(content, src);
        return src;
      }).collect(Collectors.toList());
      return tryCompile(target, sources);
    } else {
      return "Unable to create target folder";
    }
  }
}
