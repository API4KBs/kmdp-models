package edu.mayo.kmdp.util;

import static edu.mayo.kmdp.util.CharsetEncodingUtil.decodeFromBase64;
import static edu.mayo.kmdp.util.CharsetEncodingUtil.recodeToBase64;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CharsetUtilTest {

  @Test
  void testSanitizePrintable() {
    String source = "  abc \n\t \u9999 defg\n";
    System.out.println(source);
    String sanitized = CharsetEncodingUtil.sanitizeToASCIItext(source);
    System.out.println(sanitized);
    assertEquals("  abc   defg", sanitized);
  }

  @Test
  void testSanitizeAscii() {
    String source = "  abc \n\t \u9999\u00e5 defg\n";
    System.out.println(source);
    String sanitized = CharsetEncodingUtil.sanitizeToASCII(source);
    System.out.println(sanitized);
    assertEquals("  abc \n\t  defg\n", sanitized);
  }

  @Test
  void testSanitizeUTF8() {
    String source = "  abc \n\t \u9999\u00e5 defg\n";
    System.out.println(source);
    String sanitized = CharsetEncodingUtil.sanitizeToUTF8(source);
    System.out.println(sanitized);
    assertEquals("  abc \n\t \u00e5 defg\n", sanitized);
  }

  @Test
  void testBase64EncodeDecode() {
    String source = "ipsum lorem";
    String roundtrip = decodeFromBase64(recodeToBase64(source));
    assertEquals(source, roundtrip);
  }

}
