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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipUtil {

  private static Logger logger = LoggerFactory.getLogger(ZipUtil.class);

  private ZipUtil() {
  }

  public static Optional<byte[]> readZipEntry(String id, InputStream stream) {
    Optional<InputStream> entryStream = getZipEntry(id, stream);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    return entryStream.flatMap(zis -> {
      try {

        byte[] byteBuff = new byte[1024];
        int bytesRead = 0;
        while ((bytesRead = zis.read(byteBuff)) != -1) {
          out.write(byteBuff, 0, bytesRead);
        }
        return Optional.of(out.toByteArray());
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
        return Optional.empty();
      }
    });

  }

  public static Optional<InputStream> getZipEntry(String id, InputStream source) {
    ZipInputStream zis = new ZipInputStream(source);
    ZipEntry entry;
    try {
      while ((entry = zis.getNextEntry()) != null) {
        if (entry.getName().equals(id)) {
          return Optional.of(zis);
        }
      }
      return Optional.empty();
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return Optional.empty();
    }
  }

  public static Optional<InputStream> getZipEntry(Pattern selector, InputStream source) {
    ZipInputStream zis = new ZipInputStream(source);
    ZipEntry entry;
    try {
      while ((entry = zis.getNextEntry()) != null) {
        if (selector.matcher(entry.getName()).find()) {
          return Optional.of(zis);
        }
      }
      return Optional.empty();
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return Optional.empty();
    }
  }


  /**
   * Zips the data from a source InputStream, adding the zipped binary to a Zip Archive under the
   * given entryName, and streams the result into the provided OutputStream
   *
   * @param entryName the name of the entry in a new Zip archive holding the compressed inputstream
   * @param source    the binary data to be zipped
   * @param target    the OutputStream where the Zipped archive will be streamed
   * @return true if success
   */
  public static boolean zip(
      String entryName, InputStream source, OutputStream target) {
    try {
      ZipOutputStream zos = new ZipOutputStream(target);
      addToZip(entryName, source, zos);
      zos.close();
      return true;
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return false;
    }
  }

  /**
   * Zips the data from a source {@link ByteArrayOutputStream}, adding the zipped binary to a Zip
   * Archive under the given entryName, and streams the result into a target OutputStream
   *
   * @param entryName the name of the entry in a new Zip archive holding the compressed inputstream
   * @param source    the binary data to be zipped
   * @param target    the OutputStream where the Zipped archive will be streamed
   * @return true if success
   */
  public static boolean zip(
      String entryName, ByteArrayOutputStream source, OutputStream target) {
    try {
      return zip(entryName, Util.pipeStreams(source), target);
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      return false;
    }
  }

  /**
   * Zips the data from a number of named source InputStreams, adding the zipped binary to a Zip
   * Archive, and streams the result into the provided OutputStream
   *
   * @param target the OutputStream where the Zipped archive will be streamed
   * @return true if all entries have been successfully zipped and added
   */
  public static boolean zip(
      Map<String, InputStream> entries, OutputStream target) {
    try {
      ZipOutputStream zos = new ZipOutputStream(target);
      boolean success = entries.entrySet().stream()
          .map(e -> addToZip(e.getKey(), e.getValue(), zos))
          .reduce(true, Boolean::logicalAnd);
      zos.close();
      return success;
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return false;
    }
  }

  /**
   * Adds a named entry to a {@link ZipOutputStream}
   *
   * @param entryName the name of the entry
   * @param source    the InputStream providing the data to be zipped
   * @param zos       the {@link ZipOutputStream} to add an entry to
   * @return true if success
   */
  private static boolean addToZip(String entryName, InputStream source, ZipOutputStream zos) {
    try {
      zos.putNextEntry(new ZipEntry(entryName));
      byte[] bytes = source.readAllBytes();
      zos.write(bytes, 0, bytes.length);
      zos.closeEntry();
      return true;
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return false;
    }
  }

  public static LengthProvidingInputStream zip(ByteArrayOutputStream data, String filenameInternal)
      throws IOException {

    InputStream dataInputStream = Util.pipeStreams(data);

    ByteArrayOutputStream zippedDataOutputStream = new ByteArrayOutputStream();
    ZipUtil.zip(filenameInternal, dataInputStream, zippedDataOutputStream);

    // Convert the zipped data into an InputStream so that it can be read downstream
    // Util.pipeStreams could not be used a second time as the length of the stream is then unknown
    byte[] zippedBytes = zippedDataOutputStream.toByteArray();
    InputStream zippedInputStream = new ByteArrayInputStream(zippedBytes);

    return new LengthProvidingInputStream(zippedInputStream, zippedBytes.length);

  }

}
