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
import java.io.InputStream;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtil {

  public static Optional<byte[]> readZipEntry(String id, InputStream stream) {
    Optional<InputStream> entryStream = getZipEntry(id, stream);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    return entryStream.flatMap((zis) -> {
      try {

        byte[] byteBuff = new byte[1024];
        int bytesRead = 0;
        while ((bytesRead = zis.read(byteBuff)) != -1) {
          out.write(byteBuff, 0, bytesRead);
        }
        return Optional.of(out.toByteArray());
      } catch (Exception e) {
        e.printStackTrace();
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
      e.printStackTrace();
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
      e.printStackTrace();
      return Optional.empty();
    }
  }


}
