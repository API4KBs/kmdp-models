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
    return sanitize(str, NON_ASCII_PRINTABLE_RANGE);
  }

  /**
   * Sanitizes an input String into a ASCII String
   *
   * @param str the source String
   * @return an ASCII compliant String
   */
  public static String sanitizeToASCII(String str) {
    return sanitize(str, NON_ASCII_RANGE);
  }

  /**
   * Sanitizes an input String, excluding non UTF-8 characters
   *
   * @param str the source String
   * @return an UTF8 compliant String
   */
  public static String sanitizeToUTF8(String str) {
    return sanitize(str, NON_UTF8_RANGE);
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
   * @param str   the String to be sanitized
   * @param regex the Regular Expression, such that any matching character/sub-string will be
   *              removed, replaced by the empty String
   * @return a sanitized String
   * @see String#getBytes(Charset)
   * @see java.text.Normalizer
   */
  public static String sanitize(String str, String regex) {
    if (isEmpty(str)) {
      return "";
    }
    return str.replaceAll(regex, "");
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

  private CharsetEncodingUtil() {
    // static functions only
  }

}
