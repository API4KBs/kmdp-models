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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import org.apache.xerces.util.XMLChar;


public final class NameUtils {


  public enum IdentifierType {
    CLASS,
    INTERFACE,
    GETTER,
    SETTER,
    VARIABLE,
    CONSTANT
  }

  public static final String JAXB_URI = "http://java.sun.com/xml/ns/jaxb";


  private static final char[] XML_NAME_PUNCTUATION_CHARS = new char[]{
      /* hyphen                       */ '\u002D',
      /* period                       */ '\u002E',
      /* colon                        */'\u003A',
      /* dot                          */ '\u00B7',
      /* greek ano teleia             */ '\u0387',
      /* arabic end of ayah           */ '\u06DD',
      /* arabic start of rub el hizb  */ '\u06DE',
      /* underscore                   */ '\u005F',
  };

  private static final String XML_NAME_PUNCTUATION_STRING = new String(XML_NAME_PUNCTUATION_CHARS);

  private static final Map<String, String> BUILTIN_DATATYPES_MAP;
  private static final Map<String, Class<?>> HOLDER_TYPES_MAP;

  private static final String NEG = "ObjectComplementOf";


  static {
    BUILTIN_DATATYPES_MAP = new HashMap<>();
    BUILTIN_DATATYPES_MAP.put("string", String.class.getName());
    BUILTIN_DATATYPES_MAP.put("integer", BigInteger.class.getName());
    BUILTIN_DATATYPES_MAP.put("int", int.class.getName());
    BUILTIN_DATATYPES_MAP.put("long", long.class.getName());
    BUILTIN_DATATYPES_MAP.put("short", short.class.getName());
    BUILTIN_DATATYPES_MAP.put("decimal", BigDecimal.class.getName());
    BUILTIN_DATATYPES_MAP.put("float", float.class.getName());
    BUILTIN_DATATYPES_MAP.put("double", double.class.getName());
    BUILTIN_DATATYPES_MAP.put("boolean", boolean.class.getName());
    BUILTIN_DATATYPES_MAP.put("byte", byte.class.getName());
    BUILTIN_DATATYPES_MAP.put("QName", QName.class.getName());
    BUILTIN_DATATYPES_MAP.put("dateTime", Date.class.getName());
    BUILTIN_DATATYPES_MAP.put("base64Binary", byte.class.getName() + "[]");
    BUILTIN_DATATYPES_MAP.put("hexBinary", byte.class.getName() + "[]");
    BUILTIN_DATATYPES_MAP.put("unsignedInt", long.class.getName());
    BUILTIN_DATATYPES_MAP.put("unsignedShort", short.class.getName());
    BUILTIN_DATATYPES_MAP.put("unsignedByte", byte.class.getName());
    BUILTIN_DATATYPES_MAP.put("time", XMLGregorianCalendar.class.getName());
    BUILTIN_DATATYPES_MAP.put("date", XMLGregorianCalendar.class.getName());
    BUILTIN_DATATYPES_MAP.put("gYear", XMLGregorianCalendar.class.getName());
    BUILTIN_DATATYPES_MAP.put("gYearMonth", XMLGregorianCalendar.class.getName());
    BUILTIN_DATATYPES_MAP.put("gMonth", XMLGregorianCalendar.class.getName());
    BUILTIN_DATATYPES_MAP.put("gMonthDay", XMLGregorianCalendar.class.getName());
    BUILTIN_DATATYPES_MAP.put("gDay", XMLGregorianCalendar.class.getName());
    BUILTIN_DATATYPES_MAP.put("duration", Duration.class.getName());
    BUILTIN_DATATYPES_MAP.put("NOTATION", QName.class.getName());

    HOLDER_TYPES_MAP = new HashMap<>();
    HOLDER_TYPES_MAP.put(int.class.getName(), Integer.class);
    HOLDER_TYPES_MAP.put(long.class.getName(), Long.class);
    HOLDER_TYPES_MAP.put(short.class.getName(), Short.class);
    HOLDER_TYPES_MAP.put(float.class.getName(), Float.class);
    HOLDER_TYPES_MAP.put(double.class.getName(), Double.class);
    HOLDER_TYPES_MAP.put(boolean.class.getName(), Boolean.class);
    HOLDER_TYPES_MAP.put(byte.class.getName(), Byte.class);
  }

  /** Use this character as suffix */
  private static final char KEYWORD_PREFIX = '_';

  /**
   * These are java keywords as specified at the following URL.
   * http://java.sun.com/docs/books/jls/second_edition/html/lexical.doc.html#229308
   * Note that false, true, and null are not strictly keywords; they are
   * literal values, but for the purposes of this array, they can be treated
   * as literals.
   */
  private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
      "abstract", "assert", "boolean", "break", "byte", "case", "catch",
      "char", "class", "const", "continue", "default", "do", "double",
      "else", "enum", "extends", "false", "final", "finally", "float", "for", "goto",
      "if", "implements", "import", "instanceof", "int", "interface", "long",
      "native", "new", "null", "package", "private", "protected", "public",
      "return", "short", "static", "strictfp", "super", "switch",
      "synchronized", "this", "throw", "throws", "transient", "true", "try",
      "void", "volatile", "while"
  ));


  private static NameUtils instance;

  public static NameUtils getInstance() {
    if (instance == null) {
      instance = new NameUtils();
    }
    return instance;
  }

  private NameUtils() {

  }


  /**
   * checks if the input string is a valid java keyword.
   *
   * @return boolean true/false
   */
  public static boolean isJavaKeyword(String keyword) {
    return KEYWORDS.contains(keyword);
  }

  /**
   * Turn a java keyword string into a non-Java keyword string. (Right now
   * this simply means appending an underscore.)
   */
  public static String makeNonJavaKeyword(String keyword) {
    return KEYWORD_PREFIX + keyword;
  }


  public static String builtInTypeToJavaType(String type) {
    return BUILTIN_DATATYPES_MAP.get(type);
  }

  public static String builtInTypeToWrappingJavaType(String type) {
    type = type.replace("xsd:", "");
    String klass = BUILTIN_DATATYPES_MAP.get(type);
    if (HOLDER_TYPES_MAP.containsKey(klass)) {
      return HOLDER_TYPES_MAP.get(klass).getName();
    } else {
      return klass;
    }
  }

  public static Class<?> holderClass(String type) {
    return HOLDER_TYPES_MAP.get(type);
  }


  public static String buildFQNameFromIri(String iri) {
    return nameToIdentifier(iri, IdentifierType.VARIABLE);
  }

  public static String buildLowCaseNameFromIri(String iri) {
    return nameToIdentifier(iri, IdentifierType.VARIABLE);
  }

  public static String buildNameFromIri(String iriStart, String iriFragment) {
    String iri = iriFragment != null ? iriFragment : iriStart;
    return nameToIdentifier(iri, IdentifierType.CLASS);
  }

  public static String separatingName(String name) {
    return name.endsWith("/") || name.endsWith("#") ? name : (name + "#");
  }


  public static URI packageToNamespaceURI(String pack) {
    return URI.create("http://" + pack.replace(".", "/"));
  }


  /**
   * Generates a Java package name from a URI according to the
   * algorithm outlined in JAXB 2.0.
   *
   * @param namespaceURI the namespace URI.
   * @return the package name.
   */
  public static String namespaceURIStringToPackage(String namespaceURI) {
    try {
      return nameSpaceURIToPackage(new URI(namespaceURI));
    } catch (URISyntaxException ex) {
      return "INVALID";
    }
  }

  /**
   * Generates a Java package name from a URI according to the
   * algorithm outlined in JAXB 2.0.
   *
   * @param uri the namespace URI.
   * @return the package name.
   */
  public static String nameSpaceURIToPackage(URI uri) {

    StringBuilder packageName = new StringBuilder();
    String authority = uri.getAuthority();
    if (authority == null && "urn".equals(uri.getScheme())) {
      authority = uri.getSchemeSpecificPart();
    }

    if (null != authority && !"".equals(authority)) {
      if ("urn".equals(uri.getScheme())) {
        packageName.append(authority);
        for (int i = 0; i < packageName.length(); i++) {
          if (packageName.charAt(i) == '-') {
            packageName.setCharAt(i, '.');
          }
        }
        authority = packageName.toString();
        packageName.setLength(0);

        StringTokenizer st = new StringTokenizer(authority, ":");
        while (st.hasMoreTokens()) {
          String token = st.nextToken();
          if (packageName.length() > 0) {
            packageName.insert(0, ".");
            packageName.insert(0, normalizePackageNamePart(token));
          } else {
            packageName.insert(0, token);
          }
        }
        authority = packageName.toString();
        packageName.setLength(0);

      }

      StringTokenizer st = new StringTokenizer(authority, ".");
      if (st.hasMoreTokens()) {
        String token = null;
        while (st.hasMoreTokens()) {
          token = st.nextToken();
          if (packageName.length() == 0) {
            if ("www".equals(token)) {
              continue;
            }
          } else {
            packageName.insert(0, ".");
          }
          packageName.insert(0, normalizePackageNamePart(token));
        }

      }
    }

    String path = uri.getPath();
    if (path == null) {
      path = "";
    }
    StringTokenizer st = new StringTokenizer(path, "/");
    while (st.hasMoreTokens()) {
      String token = st.nextToken();
      if (packageName.length() > 0) {
        packageName.append('.');
      }
      packageName.append(normalizePackageNamePart(token));
    }
    return packageName.toString();
  }

  private static String normalizePackageNamePart(String name) {
    StringBuilder sname = new StringBuilder(name.toLowerCase());

    for (int i = 0; i < sname.length(); i++) {
      sname.setCharAt(i, Character.toLowerCase(sname.charAt(i)));
    }

    for (int i = 0; i < sname.length(); i++) {
      if (!Character.isJavaIdentifierPart(sname.charAt(i))) {
        sname.setCharAt(i, '_');
      }
    }

    if (isJavaKeyword(sname.toString())) {
      sname.insert(0, '_');
    }

    if (!Character.isJavaIdentifierStart(sname.charAt(0))) {
      sname.insert(0, '_');
    }

    return sname.toString();
  }


  /**
   * Converts an XML name to a Java identifier according to the mapping
   * algorithm outlines in the JAXB specification
   *
   * @param name the XML name
   * @return the Java identifier
   */
  public static String nameToIdentifier(String name, IdentifierType type) {
    if (Util.isEmpty(name)) {
      return name;
    }

    // algorithm will not change an XML name that is already a legal and
    // conventional (!) Java class, method, or constant identifier

    boolean legalIdentifier = false;
    StringBuilder buf = new StringBuilder(name);
    legalIdentifier = Character.isJavaIdentifierStart(buf.charAt(0));

    for (int i = 1; i < name.length() && legalIdentifier; i++) {
      legalIdentifier = Character.isJavaIdentifierPart(buf.charAt(i));
    }

    boolean conventionalIdentifier = isConventionalIdentifier(buf.toString(), type);
    if (legalIdentifier && conventionalIdentifier) {
      if (NameUtils.isJavaKeyword(name) && type == IdentifierType.VARIABLE) {
        name = normalizePackageNamePart(name);
      }
      return name;
    }

    // split into words

    List<String> words = new ArrayList<>();

    StringTokenizer st = new StringTokenizer(name, XML_NAME_PUNCTUATION_STRING);
    while (st.hasMoreTokens()) {
      words.add(st.nextToken());
    }

    for (int i = 0; i < words.size(); i++) {
      splitWord(words, i);
    }

    return makeConventionalIdentifier(words, type);

  }

  public static String toElementName(String name) {
    if (!XMLChar.isValidName(name)) {
      return KEYWORD_PREFIX + name;
    }
    return name;
  }


  private static void splitWord(List<String> words, int listIndex) {
    String word = words.get(listIndex);
    if (word.length() <= 1) {
      return;
    }
    int index = listIndex + 1;

    StringBuilder sword = new StringBuilder(word);
    int first = 0;
    char firstChar = sword.charAt(first);
    if (Character.isLowerCase(firstChar)) {
      sword.setCharAt(first, Character.toUpperCase(firstChar));
    }
    int i = 1;

    while (i < sword.length()) {
      if (Character.isDigit(firstChar)) {
        while (i < sword.length() && Character.isDigit(sword.charAt(i))) {
          i++;
        }
      } else if (isCasedLetter(firstChar)) {
        boolean previousIsLower = Character.isLowerCase(firstChar);
        while (i < sword.length() && isCasedLetter(sword.charAt(i))) {
          if (Character.isUpperCase(sword.charAt(i)) && previousIsLower) {
            break;
          }
          previousIsLower = Character.isLowerCase(sword.charAt(i));
          i++;
        }
      } else {
        // first must be a mark or an uncased letter
        while (i < sword.length() && (isMark(sword.charAt(i)) || !isCasedLetter(sword.charAt(i)))) {
          i++;
        }
      }

      // characters from first to i are all either
      // * digits
      // * upper or lower case letters, with only the first one an upper
      // * uncased letters or marks

      String newWord = sword.substring(first, i);
      words.add(index, newWord);
      index++;
      if (i >= sword.length()) {
        break;
      } else {
        first = i;
        firstChar = sword.charAt(first);
      }
    }

    if (index > (listIndex + 1)) {
      words.remove(listIndex);
    }
  }

  private static boolean isMark(char c) {
    return Character.isJavaIdentifierPart(c) && !Character.isLetter(c) && !Character.isDigit(c);
  }

  private static boolean isCasedLetter(char c) {
    return Character.isUpperCase(c) || Character.isLowerCase(c);
  }

  private static boolean isConventionalIdentifier(String buf, IdentifierType type) {
    if (null == buf || buf.length() == 0) {
      return false;
    }
    switch (type) {
      case CONSTANT:
        for (int i = 0; i < buf.length(); i++) {
          if (Character.isLowerCase(buf.charAt(i))) {
            return false;
          }
        }
        return true;
      case VARIABLE:
        return Character.isLowerCase(buf.charAt(0));
      case GETTER:
        return buf.length() >= 3
            && "get".contentEquals(buf.subSequence(0, 3))
            && Character.isUpperCase(buf.charAt(3));
      case SETTER:
        return buf.length() >= 3
            && "set".contentEquals(buf.subSequence(0, 3))
            && Character.isUpperCase(buf.charAt(3));
      default:
        return Character.isUpperCase(buf.charAt(0));
    }
  }

  private static String makeConventionalIdentifier(List<String> words, IdentifierType type) {
    StringBuilder buf = new StringBuilder();
    boolean firstWord = true;

    switch (type) {
      case GETTER:
        buf.append("get");
        break;
      case SETTER:
        buf.append("set");
        break;
      default:
    }

    for (String w : words) {
      int l = buf.length();
      if (l > 0 && IdentifierType.CONSTANT == type) {
        buf.append('_');
        l++;
      }

      buf.append(w);
      capitalizeBuffer(buf, type, l, firstWord);
      firstWord = false;
    }
    return buf.toString();
  }

  private static void capitalizeBuffer(StringBuilder buf, IdentifierType type, int l, boolean firstWord) {
    switch (type) {
      case CONSTANT:
        for (int i = l; i < buf.length(); i++) {
          if (Character.isLowerCase(buf.charAt(i))) {
            buf.setCharAt(i, Character.toUpperCase(buf.charAt(i)));
          }
        }
        break;
      case VARIABLE:
        if (firstWord && Character.isUpperCase(buf.charAt(l))) {
          buf.setCharAt(l, Character.toLowerCase(buf.charAt(l)));
        }
        break;
      default:
        if (firstWord && Character.isLowerCase(buf.charAt(l))) {
          buf.setCharAt(l, Character.toUpperCase(buf.charAt(l)));
        }
    }
  }


  public static String reverse(String aPackage) {

    StringTokenizer tok = new StringTokenizer(aPackage, ".");
    StringBuilder ans = Optional.ofNullable(tok.nextToken()).map(StringBuilder::new).orElse(null);

    while (tok.hasMoreTokens()) {
      ans = (ans == null ? new StringBuilder("null") : ans).insert(0, tok.nextToken() + ".");
    }

    return ans == null ? null : ans.toString();
  }

  public static String capitalize(final String s) {
    StringTokenizer tok = new StringTokenizer(s, "_");
    StringBuilder upName = new StringBuilder(tok.nextToken());
    upName = new StringBuilder(upName.substring(0, 1).toUpperCase() + upName.substring(1));
    while (tok.hasMoreTokens()) {
      String word = tok.nextToken();
      upName.append("_").append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
    }
    upName = new StringBuilder(s.startsWith("_") ? "_" + upName : upName.toString());
    if (s.endsWith("_")) {
      upName.append(trail(s));
    }
    return upName.toString();
  }

  private static int trail(String s) {
    int start = s.length() - 1;
    int count = 0;
    while (start > 1 && s.charAt(start) == '_') {
      start--;
      count++;
    }
    return count;
  }

  public static String camelCase(final String name) {
    String idName = nameToIdentifier(name, IdentifierType.CLASS);
    StringTokenizer tok = new StringTokenizer(idName, " _-");
    StringBuilder upName = Optional.ofNullable(tok.nextToken()).map(StringBuilder::new)
        .orElse(new StringBuilder());
    upName = new StringBuilder(
        upName.substring(0, 1).toLowerCase() + upName.substring(1).toLowerCase());
    while (tok.hasMoreTokens()) {
      String word = tok.nextToken();
      upName.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase());
    }
    return upName.toString();
  }

  public static String fixTrail(String name) {
    if (name.endsWith("_")) {
      int numChars = trail(name);
      return name.substring(0, name.length() - numChars) + numChars;
    } else {
      return name;
    }
  }

  public static String compactVariable(final String name) {
    String idName = nameToIdentifier(name, IdentifierType.VARIABLE);
    StringTokenizer tok = new StringTokenizer(idName, "_");
    StringBuilder upName = new StringBuilder(tok.nextToken());
    while (tok.hasMoreTokens()) {
      String word = tok.nextToken();
      upName.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
    }
    if (name.endsWith("_")) {
      upName.append(trail(name));
    }
    return upName.toString();
  }

  public static String negate(String expr) {
    if (expr.startsWith(NEG)) {
      return expr.substring(NEG.length() + 1, expr.length() - 1);
    } else {
      return NEG + "(" + expr + ")";
    }
  }


  public static String strip(String str, String from, Character... separators) {
    int j = 0;
    StringBuilder delta = new StringBuilder();

    if (str == null || from == null || from.isEmpty()) {
      return from;
    }

    for (int i = 0; i < str.length(); i++) {
      if (j == from.length()) {
        break;
      }
      if (str.charAt(i) == from.charAt(j)) {
        j++;
      } else {
        do {
          delta.append(from.charAt(j++));
        } while (j < from.length() && str.charAt(i) != from.charAt(j));
        i--;
      }
    }
    if (j < from.length()) {
      delta.append(from.substring(j));
    }
    if (Arrays.binarySearch(separators, delta.charAt(0)) >= 0) {
      delta.deleteCharAt(0);
    }
    if (Arrays.binarySearch(separators, delta.charAt(delta.length()-1)) >= 0) {
      delta.deleteCharAt(delta.length()-1);
    }
    return delta.toString();
  }


  public static String getTermCodeSystemName(String codeSystemName) {
    return NameUtils.capitalize(codeSystemName.replaceAll("[^a-zA-Z0-9]", "_"));
  }

  public static String getTermConceptName(String conceptCode, String conceptName) {
    String term = Util.isEmpty(conceptName) ? conceptCode : conceptName;
    if (Util.isEmpty(term)) {
      return "";
    }
    term = fixSpecialCharacters(term);
    term = term.replaceAll("[^a-zA-Z0-9]", "_").trim();
    if (term.endsWith("_")) {
      term = term.substring(0, term.length() - 1);
    }
    return NameUtils.capitalize(term);
  }

  private static String fixSpecialCharacters(String s) {
    s = s.replace(">", "_GT_");
    s = s.replace("<", "_LT_");
    return s;
  }


  public static URI removeFragment(URI uri) {
    if (uri != null && !Util.isEmpty(uri.getFragment())) {
      return URI.create(
          uri.toString().substring(0, uri.toASCIIString().lastIndexOf(uri.getFragment()) - 1));
    } else {
      return uri;
    }
  }

  public static String getTrailingPart(String uri) {
    if (Util.isEmpty(uri)) {
      return "";
    }
    try {
      URI u = new URI(uri);
      if (!Util.isEmpty(u.getFragment())) {
        return u.getFragment();
      }
      return uri.substring(uri.lastIndexOf('/') + 1);
    } catch (URISyntaxException e) {
      return uri;
    }

  }

  public static String removeTrailingPart(String uri) {
    return uri.replace(getTrailingPart(uri), "");
  }


}
