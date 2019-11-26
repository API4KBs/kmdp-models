package edu.mayo.kmdp;

import com.sun.tools.corba.se.idl.toJavaPortable.Compile;
import edu.mayo.kmdp.util.FileUtil;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestIDLCompiler {

  private static final Logger logger = LoggerFactory.getLogger(TestIDLCompiler.class);

  private static Compile compiler;

  static {
    try {
      Class<Compile> compileClass = Compile.class;
      Constructor<Compile> constructor = compileClass.getDeclaredConstructor();
      constructor.setAccessible(true);

      compiler = constructor.newInstance();
      Compile.compiler = compiler;
    } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
      logger.error(e.getMessage(),e);
    }
  }


  public static String tryCompile(File outputDir, File... sourceIDLFiles) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    PrintStream err = System.err;
    System.setErr(new PrintStream(baos));

    String sources = Arrays.stream(sourceIDLFiles)
        .map(File::getAbsolutePath)
        .map(p ->  p)
        .collect(Collectors.joining(" "));

    String target = outputDir.getAbsolutePath();

    compiler.start(new String[] {"-td" , target, sources});

    System.setErr(err);
    return baos.toString();
  }

  protected TestIDLCompiler() {
    // nothing to do
  }

  public static String tryCompileSource(File tmpRoot, String idlSource) {
    File src = new File(tmpRoot, "test.idl");
    File target = new File(tmpRoot, "output");
    if (target.mkdir()) {
      FileUtil.write(idlSource, src);
      return tryCompile(target, src);
    } else {
      return "Unable to create target folder";
    }
  }
}
