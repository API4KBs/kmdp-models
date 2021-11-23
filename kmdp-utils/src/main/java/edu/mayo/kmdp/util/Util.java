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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collection of miscellaneaous utility functions
 */
public class Util {

  private static final Logger logger = LoggerFactory.getLogger(Util.class);

  private static final Pattern uuidPattern = Pattern.compile(
      "^([A-Fa-f0-9]{8})([A-Fa-f0-9]{4})([A-Fa-f0-9]{4})([A-Fa-f0-9]{4})([A-Fa-f0-9]{12})$");

  private static final Pattern oidPattern = Pattern.compile(
      "^([0-2])((\\.0)|(\\.[1-9][0-9]*))*$");

  private Util() {
  }

  public static URL resolveResource(String path) {
    return Util.class.getResource(path);
  }

  /**
   * @param str a String
   * @return true if the string is null, empty (length = 0), or blank (all whitespaces)
   */
  public static boolean isEmpty(String str) {
    return str == null || str.isEmpty() || str.isBlank();
  }

  /**
   * @param str a String
   * @return true if the string is not null, not empty (length > 0), and not blank (at least one non
   * whitespace character)
   */
  public static boolean isNotEmpty(String str) {
    return str != null && !str.isEmpty() && !str.isBlank();
  }

  public static String concat(List<String> str) {
    return str == null ? null : str.stream().collect(Collectors.joining());
  }

  public static String normalize(String str) {
    return str.toLowerCase().trim().replace(" ", "_");
  }

  public static <K, V> Map<K, V> toMap(Collection<V> values, Function<V, K> identifier) {
    return values.stream()
        .collect(Collectors.toMap(
            identifier,
            x -> x
        ));
  }

  @SafeVarargs
  public static <K> Map<K, K> toLinkedMap(K[]... values) {
    return Arrays.stream(values).collect(Collectors.toMap(
        x -> x[0],
        x -> x[1],
        (x, y) -> x,
        LinkedHashMap::new));
  }

  @SuppressWarnings("unchecked")
  public static <T> T[] ensureArray(T[] array, Class<T> type) {
    return array == null ? (T[]) Array.newInstance(type, 0) : array;
  }

  public static Optional<ByteArrayOutputStream> printOut(ByteArrayOutputStream os) {
    try {
      return Optional.of(os);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return Optional.empty();
    }
  }

  public static Optional<String> printOut(String s) {
    try {
      return Optional.of(s);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return Optional.empty();
    }
  }

  public static Optional<InputStream> resolveURL(final URL url) {
    try {
      return Optional.of(url.openStream());
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      return Optional.empty();
    }
  }

  public static Optional<InputStream> resolveURL(final String local) {
    URL fileURL = Util.class.getResource(local);
    if (fileURL == null) {
      return Optional.empty();
    }
    File file = new File(fileURL.getPath());
    try {
      if (file.exists()) {
        return Optional.of(new FileInputStream(file));
      } else {
        return Optional.ofNullable(Util.class.getResourceAsStream(local));
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }

    return Optional.empty();
  }


  public static void processDir(File root, File dir, BiConsumer<File, File> processor) {
    if (dir != null && dir.isDirectory()) {
      FileUtil.streamChildFiles(dir).forEach(f -> {
        if (f.isDirectory()) {
          processDir(root, f, processor);
        } else {
          try {
            processor.accept(root, f);
          } catch (Exception e) {
            logger.error(e.getMessage(), e);
          }
        }
      });
    }
  }


  public static String pluralize(String name) {
    if (name.endsWith("_")) {
      int start = name.length() - 1;
      while (name.charAt(start - 1) == '_') {
        start--;
      }
      String root = getPluralForm(name.substring(0, start));
      String trail = name.substring(start);
      return root + trail;
    } else {
      return getPluralForm(name);
    }
  }


  public static String getPluralForm(String word) {
    // remember the casing of the word
    boolean allUpper = true;

    // check if the word looks like an English word.
    // if we see non-ASCII characters, abort
    for (int i = 0; i < word.length(); i++) {
      char ch = word.charAt(i);
      if (ch >= 0x80) {
        return word;
      }

      allUpper &= !Character.isLowerCase(ch);
    }

    for (Entry e : TABLE) {
      String r = e.apply(word);
      if (r != null) {
        if (allUpper) {
          r = r.toUpperCase();
        }
        return r;
      }
    }

    // failed
    return word;
  }

  private static final Entry[] TABLE;

  static {
    String[] source = {
        "(.*)child", "$1children",
        "(.+)fe", "$1ves",
        "(.*)mouse", "$1mise",
        "(.+)f", "$1ves",
        "(.+)ch", "$1ches",
        "(.+)sh", "$1shes",
        "(.*)tooth", "$1teeth",
        "(.+)um", "$1a",
        "(.+)an", "$1en",
        "(.+)ato", "$1atoes",
        "(.*)basis", "$1bases",
        "(.*)axis", "$1axes",
        "(.+)is", "$1ises",
        "(.+)ss", "$1sses",
        "(.+)us", "$1uses",
        "(.+)s", "$1s",
        "(.*)foot", "$1feet",
        "(.+)ix", "$1ixes",
        "(.+)ex", "$1ices",
        "(.+)nx", "$1nxes",
        "(.+)x", "$1xes",
        "(.+)y", "$1ies",
        "(.+)", "$1s",
    };

    TABLE = new Entry[source.length / 2];

    for (int i = 0; i < source.length; i += 2) {
      TABLE[i / 2] = new Entry(source[i], source[i + 1]);
    }
  }

  public static String clearLineSeparators(String s) {
    return s.replace("\n", "").replace("\r", "").replace(System.getProperty("line.separator"), "");
  }

  public static String removeLastChar(String str) {
    return str != null && str.length() > 1
        ? str.substring(0, str.length() - 1)
        : str;
  }

  public static <E extends Enum<E>> Set<E> newEnumSet(Iterable<E> iterable,
      Class<E> elementType) {
    EnumSet<E> set = EnumSet.noneOf(elementType);
    iterable.forEach(set::add);
    return set;
  }

  public static <T> Optional<T> as(Object o, Class<T> type) {
    return type.isInstance(o)
        ? Optional.of(type.cast(o))
        : Optional.empty();
  }

  public static <T> Function<Object, Optional<T>> as(Class<T> type) {
    return x -> type.isInstance(x)
        ? Optional.of(type.cast(x))
        : Optional.empty();
  }

  public static UUID hashUUID(UUID uuid1, UUID uuid2) {
    long l1 = uuid1.getLeastSignificantBits() ^ uuid2.getMostSignificantBits();
    long l2 = uuid2.getLeastSignificantBits() ^ uuid1.getMostSignificantBits();
    long l3 = l1 ^ l2;

    byte[] x = ByteBuffer.allocate(Long.BYTES).putLong(l3).array();

    return UUID.nameUUIDFromBytes(x);
  }

  public static String hashString(String s1, String s2) {
    int h1 = s1.hashCode();
    int h2 = s2.hashCode();
    if (h1 == h2) {
      h2 += 37;
    }
    return Integer.toString(h1 ^ h2);
  }


  public static <T> T coalesce(T... values) {
    return Arrays.stream(values)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  public static <T extends Enum<T>> UUID uuid(Enum<T> val) {
    return uuid(val.getDeclaringClass().getName() + val.name());
  }

  private static class Entry {

    private final Pattern pattern;
    private final String replacement;

    public Entry(String pattern, String replacement) {
      this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
      this.replacement = replacement;
    }

    String apply(String word) {
      Matcher m = pattern.matcher(word);
      if (m.matches()) {
        StringBuffer buf = new StringBuffer();
        m.appendReplacement(buf, replacement);
        return buf.toString();
      } else {
        return null;
      }
    }
  }

  public static Optional<String> asString(ByteArrayOutputStream baos) {
    if (baos == null) {
      return Optional.empty();
    } else if (baos.size() == 0) {
      return Optional.of("");
    } else {
      return Optional.of(baos.toString());
    }
  }

  public static Optional<String> ensureUUIDFormat(String tag) {
    Matcher matcher = uuidPattern.matcher(tag);
    String id = tag.replace("-", "");
    if (!uuidPattern.matcher(id).matches()) {
      return Optional.empty();
    } else {
      return Optional.of(matcher.replaceAll("$1-$2-$3-$4-$5"));
    }
  }

  public static Optional<UUID> ensureUUID(String tag) {
    return ensureUUIDFormat(tag).map(UUID::fromString);
  }

  public static UUID toUUID(String tag) {
    return UUID.fromString(tag);
  }

  public static boolean isUUID(String tag) {
    String id = tag.replace("-", "");
    return uuidPattern.matcher(id).matches();
  }

  public static UUID uuid(String from) {
    return UUID.nameUUIDFromBytes(from.getBytes());
  }

  public static String compactUUID(UUID tag) {
    return tag.toString().replace("-", "");
  }

  /**
   * Removes non-printable characters from a String
   *
   * @param str the input string
   * @return the input str, where non-printable characters have been removed
   * @see CharsetEncodingUtil#sanitizeToASCIItext(String)
   * @deprecated the behavior of the method is narrower than the name implies
   */
  @Deprecated(since = "KMDP v11", forRemoval = true)
  public static String ensureUTF8(String str) {
    return CharsetEncodingUtil.sanitizeToASCIItext(str);
  }

  public static boolean isOID(String tag) {
    return oidPattern.matcher(tag).matches();
  }


  public static <T> List<T> paginate(List<T> list, Integer offset, Integer limit,
      Comparator<? super T> comparator) {
    if (list == null || list.isEmpty()) {
      return Collections.emptyList();
    }

    if (comparator != null) {
      list = new ArrayList<>(list);
      list.sort(comparator);
    }

    int num = list.size();
    int off = offset != null && offset > 0 ? offset : 0;
    if (off > num) {
      return Collections.emptyList();
    }

    int lim = limit != null && limit > 0 ? limit : (Integer.MAX_VALUE - off - 1);
    return list.subList(off, Math.min(off + lim, num));
  }

  public static <T> List<T> mergeLists(List<T> l1, List<T> l2) {
    if (l1 == null && l2 == null) {
      return Collections.emptyList();
    }
    if (l1 == null) {
      return new ArrayList<>(l2);
    }
    if (l2 == null) {
      return new ArrayList<>(l1);
    }

    List<T> combined = new ArrayList<>(l1);
    combined.addAll(l2);
    return combined;
  }

  public static <T> Set<T> mergeSets(Set<T> l1, Set<T> l2) {
    if (l1 == null && l2 == null) {
      return Collections.emptySet();
    }
    if (l1 == null) {
      return new HashSet<>(l2);
    }
    if (l2 == null) {
      return new HashSet<>(l1);
    }

    Set<T> combined = new HashSet<>(l1);
    combined.addAll(l2);
    return combined;
  }

  /**
   * Copied from org.apache.xmlBeans.IOUtil to avoid dependency
   *
   * @param input
   * @param output
   * @throws IOException
   */
  public static void copyCompletely(InputStream input, OutputStream output)
      throws IOException {
    //if both are file streams, use channel IO
    if ((output instanceof FileOutputStream) && (input instanceof FileInputStream)) {
      try {
        FileChannel target = ((FileOutputStream) output).getChannel();
        FileChannel source = ((FileInputStream) input).getChannel();

        source.transferTo(0, Integer.MAX_VALUE, target);

        source.close();
        target.close();

        return;
      } catch (Exception e) { /* failover to byte stream version */ }
    }

    byte[] buf = new byte[8192];
    while (true) {
      int length = input.read(buf);
      if (length < 0) {
        break;
      }
      output.write(buf, 0, length);
    }

    try {
      input.close();
    } catch (IOException ignore) {
      // do nothing
    }
    try {
      output.close();
    } catch (IOException ignore) {
      // do nothing
    }
  }
}
