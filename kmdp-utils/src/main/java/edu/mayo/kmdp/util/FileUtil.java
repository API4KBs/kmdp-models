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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {

  private static Logger logger = LoggerFactory.getLogger(FileUtil.class);

  protected FileUtil() {

  }

  public static Optional<String> read(String file) {
    try(FileInputStream fis = new FileInputStream(file)) {
      return read(fis);
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      return Optional.empty();
    }
  }

  public static Optional<String> read(File file) {
    try {
      return read(new FileInputStream(file));
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      return Optional.empty();
    }
  }

  public static Optional<String> read(Path path) {
    try {
      return Optional.ofNullable(Files.readString(path));
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      return Optional.empty();
    }
  }

  public static Optional<String> read(InputStream is) {
    try {
      int available = is.available();
      byte[] data = new byte[available];
      int actual = is.read(data);

      if (available != actual) {
        throw new IOException("Unable to read all data");
      }

      return Optional.of(new String(data));
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      return Optional.empty();
    }
  }


  public static Optional<String> read(URL url) {
    try {
      File f = new File(url.toURI());
      if (!f.exists()) {
        throw new FileNotFoundException("Unable to find file for url " + url);
      }
      return read(f);
    } catch (URISyntaxException | IOException e) {
      logger.error(e.getMessage(), e);
      return Optional.empty();
    }
  }

  public static Optional<String> read(URI uri) {
    try {
      return read(uri.toURL());
    } catch (MalformedURLException e) {
      logger.error(e.getMessage(), e);
      return Optional.empty();
    }
  }


  public static Optional<byte[]> readBytes(InputStream is) {
    try {
      DataInputStream dis = new DataInputStream(is);
      int available = is.available();
      byte[] data = new byte[available];
      dis.readFully(data);

      return Optional.of(data);
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      return Optional.empty();
    }
  }

  public static Optional<byte[]> readBytes(URL url) {
    try {
      return readBytes(url.openStream());
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      return Optional.empty();
    }
  }

  public static Optional<byte[]> readBytes(URI uri) {
    try {
      return readBytes(uri.toURL());
    } catch (MalformedURLException e) {
      logger.error(e.getMessage(), e);
      return Optional.empty();
    }
  }

  public static Optional<byte[]> readBytes(Path path) {
    try {
      return Optional.ofNullable(Files.readAllBytes(path));
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      return Optional.empty();
    }
  }

  public static String readStatic(String path, Class<?> ctx) {
    return new Scanner(
        ctx.getResourceAsStream(path),
        Charset.defaultCharset().name())
        .useDelimiter("\\A")
        .next();
  }

  public static List<URL> findFiles(URL rootURL, String regxpFilter) {
    try {
      File root = new File(rootURL.toURI());
      if (root.isDirectory()) {
        List<URL> files = new LinkedList<>();
        findFiles(root, regxpFilter, files);
        return files;
      } else if (root.getPath().matches(regxpFilter)) {
        return Collections.singletonList(root.toURI().toURL());
      } else {
        return Collections.emptyList();
      }
    } catch (URISyntaxException | MalformedURLException e) {
      logger.error(e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  private static void findFiles(File root, String regxpFilter, List<URL> fileURLs)
      throws MalformedURLException {
    if (root == null) {
      return;
    }
    for (File child : Objects.requireNonNull(root.listFiles())) {
      if (child.isDirectory()) {
        findFiles(child, regxpFilter, fileURLs);
      } else {
        if (child.getPath().matches(regxpFilter)) {
          fileURLs.add(child.toURI().toURL());
        }
      }
    }
  }

  public static boolean copyTo(URL source, File targetFile) {
    try {
      return copyTo(source.openStream(), targetFile);
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      return false;
    }
  }

  public static boolean copyTo(InputStream sourceStream, File targetFile) {
    try {
      if (targetFile.isDirectory()) {
        return false;
      }

      BufferedReader reader = new BufferedReader(new InputStreamReader(sourceStream));
      StringBuilder out = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        out.append(line);
      }
      reader.close();

      if (!targetFile.getParentFile().exists() && !targetFile.getParentFile().mkdirs()) {
        return false;
      }

      try (FileOutputStream fos = new FileOutputStream(targetFile)) {
        fos.write(out.toString().getBytes());
      }

      return true;
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      return false;
    }
  }

  public static void delete(File root) {
    Path pathToBeDeleted = root.toPath();

    try (Stream<Path> pathStream = Files.walk(pathToBeDeleted)) {
      pathStream
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }

  public static File relativePathToFile(String rootPath, String relativePath) {
    return new File(rootPath + asPlatformSpecific(relativePath));
  }


  public static String asPlatformSpecific(String path) {
    return path.contains("/")
        ? path.replace("/", Matcher.quoteReplacement(File.separator))
        : path;
  }

  public static void write(byte[] content, File file) {
    try (FileOutputStream fos = new FileOutputStream(file);) {
      fos.write(content);
      fos.flush();
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }

  public static void write(String content, File file) {
    try (PrintWriter wr = new PrintWriter(file)) {
      wr.write(content);
      wr.flush();
    } catch (FileNotFoundException e) {
      logger.error(e.getMessage(), e);
    }
  }

  public static Stream<File> streamChildFiles(File f) {
    File[] children = f.listFiles();
    return Arrays.stream(children != null ? children : new File[0]);
  }
}
