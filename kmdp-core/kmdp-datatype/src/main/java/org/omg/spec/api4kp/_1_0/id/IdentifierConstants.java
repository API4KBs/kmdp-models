package org.omg.spec.api4kp._1_0.id;

import edu.mayo.kmdp.util.DateTimeUtil;
import java.net.URI;
import java.util.Date;
import java.util.regex.Pattern;

public final class IdentifierConstants {

  public static final String SNAPSHOT = "SNAPSHOT";
  public static final String SNAPSHOT_DATE_PATTERN = "yyyyMMdd-HHmmSS";

  public static final String VERSION_ZERO = "0.0.0";
  public static final String VERSION_LATEST = "LATEST";

  public static final String VERSIONS = "/versions/";
  public static final Pattern VERSIONS_RX = Pattern.compile("^(.*/)?(.*)/versions/(.+)$");

  public static final Pattern SEMVER_RX = Pattern.compile("^(\\d+\\.)(\\d+\\.)(\\*|\\d+)$");

  public static final URI SNOMED_URI = URI.create("http://snomed.info/sct");
  public static final URI SNOMED_BASE_URI = URI.create("http://snomed.info/id/");
  public static final String SNOMED_VERSION = "20200309";
  public static final Date SNOMED_DATE = DateTimeUtil.parseDate(SNOMED_VERSION, "YYYYmmDD");

  private IdentifierConstants() {
    // do not instantiate
  }
}