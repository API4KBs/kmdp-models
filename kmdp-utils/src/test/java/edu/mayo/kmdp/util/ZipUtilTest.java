package edu.mayo.kmdp.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ZipUtilTest {

  public static String ENTRY_NAME = "entry";
  public static String DATA = "{'test': 'yes'}";

  @Test
  void testZipRoundTrip() throws IOException {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ByteArrayOutputStream zipos = new ByteArrayOutputStream();

    baos.write(DATA.getBytes());
    ZipUtil.zip(ENTRY_NAME, baos, zipos);

    String unzipped = ZipUtil.readZipEntry(ENTRY_NAME, Util.pipeStreams(zipos))
        .map(String::new)
        .orElseGet(Assertions::fail);

    assertEquals(DATA, unzipped);
  }

  @Test
  void testZipAndReturnInputStream() throws IOException {

    String outputFilename = "zipped-file";
    String entry = outputFilename + ".json";

    ByteArrayOutputStream byteArrayOutputStream = JSonUtil.writeJson(DATA).get();

    InputStream zippedData = ZipUtil.zip(entry, byteArrayOutputStream);

    String unzippedEntry = ZipUtil.readZipEntry(entry, zippedData)
        .map(String::new).orElseGet(Assertions::fail);
    assertTrue(unzippedEntry.contains(DATA));

  }

}
