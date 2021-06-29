package edu.mayo.kmdp.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.util.FileUtil;
import java.io.InputStream;
import java.net.URL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FileUtilTest {

  @Test
  void testReadLongStream() {
    InputStream is = FileUtilTest.class.getResourceAsStream("/lorem_ipsum.txt");
    assertNotNull(is);
    String str = FileUtil.read(is).orElseGet(Assertions::fail);
    assertTrue(str.contains("Nunc eget lorem dolor sed viverra ipsum"));
  }

  @Test
  void testReadLongStreamAsURL() {
    URL url = FileUtilTest.class.getResource("/lorem_ipsum.txt");
    assertNotNull(url);
    String str = FileUtil.read(url).orElseGet(Assertions::fail);
    assertTrue(str.contains("Nunc eget lorem dolor sed viverra ipsum"));
  }


}
