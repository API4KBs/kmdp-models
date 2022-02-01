package edu.mayo.kmdp.util;

import static edu.mayo.kmdp.util.Util.isEmpty;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * The core Java class {@link java.nio.charset.Charset} combines the notion of Character Set in the
 * strict sense - the collection of signs/symbols included in an Alphabet - with the notion of
 * Encoding, i.e. how each character is mapped to a sequence of bits.
 * <p>
 * In an API4KP platform, the term Char(acter)Set is more strictly used for the set of Characters.
 * The java enumeration {@link StandardCharsets} lists the commonly used sets.
 * <p>
 * Each (standard) Charset implies the use of a DEFAULT Encoding, but additional Encoding(s) may be
 * overlaid: compressions such as GZip or re-codings such as Base64 are common examples.
 * <p>
 * This class collects common utilities / functions that help with mappings and validation between
 * Charsets and the relative Encodings
 */
public class CharsetEncodingUtil {

  /**
   * Regex that <b>excludes</b> non-ASCII, non-printable characters
   */
  public static final String NON_ASCII_PRINTABLE_RANGE = "[^\\x20-\\x7e]";

  /**
   * Regex that <b>excludes</b> non-ASCII characters
   */
  public static final String NON_ASCII_RANGE = "[^\\x00-\\x7f]";

  /**
   * Regex that <b>excludes</b> non-UTF8 characters
   */
  public static final String NON_UTF8_RANGE = "[^\\x00-\\xff]";

  /**
   * Sanitizes an input String into an ASCII String only composed of printable characters
   *
   * @param str the source String
   * @return an ASCII String of printable characters
   */
  public static String sanitizeToASCIItext(String str) {
    return sanitize(str, NON_ASCII_PRINTABLE_RANGE, "");
  }

  /**
   * Sanitizes an input String into a ASCII String
   *
   * @param str the source String
   * @return an ASCII compliant String
   */
  public static String sanitizeToASCII(String str) {
    return sanitize(str, NON_ASCII_RANGE, "");
  }

  /**
   * Sanitizes an input String, excluding non UTF-8 characters
   *
   * @param str the source String
   * @return an UTF8 compliant String
   */
  public static String sanitizeToUTF8(String str) {
    return sanitize(str, NON_UTF8_RANGE, "");
  }


  /**
   * Removes any characters in a given String that does not match a given regex.
   * <p>
   * Note that unlike {@link String#getBytes(Charset)}, special characters will be removed instead
   * of being replaced by a default char such as '?'. Unlike {@link java.text.Normalizer}, this
   * method will also not attempt to remap characters to a canonical approximation.
   * <p>
   * Work based on @{link https://stackoverflow.com/questions/8519669/how-can-non-ascii-characters-be-removed-from-a-string}
   *
   * @param str         the String to be sanitized
   * @param regex       the Regular Expression, such that any matching character/sub-string will be
   *                    removed, replaced by the empty String
   * @param replacement the String used to replace the characters
   * @return a sanitized String
   * @see String#getBytes(Charset)
   * @see java.text.Normalizer
   */
  public static String sanitize(String str, String regex, String replacement) {
    if (isEmpty(str)) {
      return "";
    }
    return str.replaceAll(regex, replacement);
  }

  /**
   * Predicate that determines whether a character is a core ASCII character
   *
   * @param charCodePoint the index of a character in the universal character set (Unicode)
   * @return true if the character is part of the ASCII sub-set
   */
  public static boolean isASCII(int charCodePoint) {
    return (charCodePoint & 0xFFFFFF80) == 0;
  }

  /**
   * Utility method that uses {@link Base64#getEncoder()} to recode a String into Base64 form
   *
   * @param str the original String
   * @return a Base64 re-encoded String
   */
  public static String recodeToBase64(String str) {
    if (isEmpty(str)) {
      return "";
    }
    return new String(Base64.getEncoder().encode(str.getBytes()));
  }

  /**
   * Utility method that uses {@link Base64#getDecoder()} to decode a Base64 String
   *
   * @param str a Base64 encoded String
   * @return the decoded String
   */
  public static String decodeFromBase64(String str) {
    if (isEmpty(str)) {
      return "";
    }
    return new String(Base64.getDecoder().decode(str));
  }

  /**
   * Utility method that converts a String into a String of bits, where each chunk of bits is the
   * binary encoding of a character in the original String, according to the given Charset. The
   * chunks will be interleaved by a separator sequence of characters (which can be empty).
   * <p>
   * The character order is preserved left to right. Within each 8-bit chunk, the most significant
   * bit is on the left. For example, "abc" will become |01100001|01100010|01100011|
   *
   * @param src       the String to be binary - encoded
   * @param charset   the java Charset, providing the charater to bit Encoding
   * @param separator a separator to divide the bit sequences pertaining to each character in the
   *                  original String. Separators are appended both at the beginning and the end of
   *                  the resulting String
   * @return a possibly interleaved sequence of bits, as a String of 0/1s
   */
  public static String recodeToBinary(String src, Charset charset, String separator) {
    if (separator == null) {
      separator = "";
    }
    if (src == null) {
      return separator;
    }

    byte[] bytes = src.getBytes(charset);
    StringBuilder sb = new StringBuilder(separator);
    for (byte aByte : bytes) {
      String s = String.format(
              "%8s",
              Integer.toBinaryString(aByte & 0xFF))
          .replace(' ', '0');
      sb.append(s).append(separator);
    }

    return sb.toString();
  }

  /**
   * Removes characters which are invalid or generally problematic in a file name, based on the
   * regex [:\\/*?|<>%",$]
   *
   * @param name the original name
   * @return name, where any special character has been replaced by '_'
   */
  public static String sanitizeFileName(String name) {
    return name.replaceAll("[:\\\\/*?|<>%\",$]", "_");
  }

  private CharsetEncodingUtil() {
    // static functions only
  }

}
