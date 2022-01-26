package edu.mayo.kmdp.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ZipUtilTest {

  public static String DATA = "Hello World";
  public static String ENTRY_NAME = "entry";

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
  void testZipToZippedInputStream() throws IOException {

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    outputStream.write(DATA.getBytes());

    LengthProvidingInputStream zippedInputStream = ZipUtil.zip(outputStream, ENTRY_NAME);

    assertEquals(137, zippedInputStream.getLength());

    String unzippedEntry = ZipUtil.readZipEntry(ENTRY_NAME, zippedInputStream)
        .map(String::new).orElseGet(Assertions::fail);
    assertEquals(DATA, unzippedEntry);

  }

}
