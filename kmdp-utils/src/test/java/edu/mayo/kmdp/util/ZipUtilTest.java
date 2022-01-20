package edu.mayo.kmdp.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ZipUtilTest {

  @Test
  void testZipRoundTrip() throws IOException {
    String data = "Hello World";
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ByteArrayOutputStream zipos = new ByteArrayOutputStream();

    baos.write(data.getBytes());
    ZipUtil.zip("entry", baos, zipos);

    String unzipped = ZipUtil.readZipEntry("entry", Util.pipeStreams(zipos))
        .map(String::new)
        .orElseGet(Assertions::fail);

    assertEquals(data, unzipped);
  }
}
