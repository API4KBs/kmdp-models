package edu.mayo.kmdp.util;

import static edu.mayo.kmdp.util.CharsetEncodingUtil.decodeFromBase64;
import static edu.mayo.kmdp.util.CharsetEncodingUtil.recodeToBase64;
import static edu.mayo.kmdp.util.CharsetEncodingUtil.recodeToBinary;
import static java.lang.Integer.toBinaryString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

  @Test
  void testNBSPtoBits() {
    String nbsp = " ";

    assertEquals("|11000010|10100000|",
        recodeToBinary(nbsp, StandardCharsets.UTF_8, "|"));
    assertEquals("|10100000|",
        recodeToBinary(nbsp, Charset.forName("windows-1252"), "|"));
  }

  @Test
  void testStringToBits() {
    String str = "a b";
    String bitCoded = recodeToBinary(str, StandardCharsets.UTF_8, null);

    assertEquals("011000010010000001100010", bitCoded);
    assertTrue(bitCoded.contains(toBinaryString('a')));
    assertTrue(bitCoded.contains(toBinaryString(' ')));
    assertTrue(bitCoded.contains(toBinaryString('b')));
  }

}
