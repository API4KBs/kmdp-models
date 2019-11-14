package edu.mayo.kmdp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sun.tools.corba.se.idl.toJavaPortable.Compile;
import edu.mayo.kmdp.util.FileUtil;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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


  public static String tryCompile(String... sourceIDLFilePath) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    PrintStream err = System.err;
    System.setErr(new PrintStream(baos));

    compiler.start(sourceIDLFilePath);

    System.setErr(err);
    return baos.toString();
  }

  protected TestIDLCompiler() {
    // nothing to do
  }

  public static String tryCompileSource(File tmpRoot, String idlSource) {
    File src = new File(tmpRoot,"test.idl");
    FileUtil.write(idlSource,src);
    return tryCompile(src.getAbsolutePath());
  }
}
