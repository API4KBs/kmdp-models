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
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class Util {

  private static final Logger logger = LoggerFactory.getLogger(Util.class);
  
  private static final Pattern uuidPattern = Pattern.compile(
      "^([A-Fa-f0-9]{8})([A-Fa-f0-9]{4})([A-Fa-f0-9]{4})([A-Fa-f0-9]{4})([A-Fa-f0-9]{12})$");

  private Util() {}

  public static URL resolveResource(String path) {
    return Util.class.getResource(path);
  }

  public static boolean isEmpty(String str) {
    return str == null || str.trim().length() == 0;
  }

  public static String concat(List<String> str) {
    return str == null ? null : str.stream().reduce("",String::concat);
  }

  public static String normalize(String str) {
    return str.toLowerCase().trim().replace(" ", "_");
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
    return array == null ? (T[]) Array.newInstance(type,0) : array;
  }

  public static Optional<ByteArrayOutputStream> printOut(ByteArrayOutputStream os) {
    try {
      return Optional.of(os);
    } catch (Exception e) {
      logger.error(e.getMessage(),e);
      return Optional.empty();
    }
  }

  public static Optional<String> printOut(String s) {
    try {
      return Optional.of(s);
    } catch (Exception e) {
      logger.error(e.getMessage(),e);
      return Optional.empty();
    }
  }

  public static Optional<InputStream> resolveURL(final URL url) {
    try {
      return Optional.of(url.openStream());
    } catch (IOException e) {
      logger.error(e.getMessage(),e);
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
      logger.error(e.getMessage(),e);
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

  public static <T,X> Stream<T> streamAs(X instance, Class<T> type) {
    return as(instance,type)
        .map(Stream::of)
        .orElseGet(Stream::empty);
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
      return Optional.of(new String(baos.toByteArray()));
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

  public static String ensureUTF8(String str) {
    if (isEmpty(str)) {
      return "";
    }
    return str.replaceAll("[^\\x20-\\x7e]", "");
  }

  public static <T> Stream<T> trimStream(Optional<T> opt) {
    return opt.map(Stream::of).orElseGet(Stream::empty);
  }

}
