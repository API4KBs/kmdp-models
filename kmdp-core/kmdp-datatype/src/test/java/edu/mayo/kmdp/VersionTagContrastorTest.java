package edu.mayo.kmdp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.mayo.kmdp.comparator.Contrastor.Comparison;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalQueries;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.id.VersionTagContrastor;

public class VersionTagContrastorTest {

  VersionTagContrastor con = new VersionTagContrastor(this::toInstant);

  @Test
  void testCompareSequential() {
    assertEquals(Comparison.BROADER, con.contrast("200", "100"));
    assertEquals(Comparison.IDENTICAL, con.contrast("200", "200"));
  }

  @Test
  void testCompareSemVer() {
    assertEquals(Comparison.BROADER, con.contrast("1.0.1", "1.0.0"));
    assertEquals(Comparison.BROADER, con.contrast("1.0.1", "1.0.1-alpha"));
  }

  @Test
  void testCompareGeneric() {
    assertEquals(Comparison.BROADER, con.contrast("20200101", "20190101"));
    assertEquals(Comparison.BROADER, con.contrast("20200101-123456", "20200101"));
    assertEquals(Comparison.BROADER, con.contrast("20200101-123456", "20200101-100"));
  }

  private long toInstant(String version) {
    String timeComponent = "0";

    if (version.contains("-")) {
      int split = version.lastIndexOf("-");
      timeComponent = version.substring(split + 1);
      version = version.substring(0, split);
    }

    long base = DateTimeFormatter.ofPattern("yyyyMMdd")
        .parse(version, TemporalQueries.localDate())
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant().toEpochMilli();
    long detail = Long.parseLong(timeComponent);

    return base + detail;
  }


}
