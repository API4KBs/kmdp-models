package org.omg.spec.api4kp._20200801.id;

import static edu.mayo.kmdp.util.DateTimeUtil.parseDateTime;
import static java.lang.Long.parseLong;

import com.github.zafarkhaja.semver.Version;
import edu.mayo.kmdp.comparator.Contrastor;
import edu.mayo.kmdp.util.DateTimeUtil;
import java.util.function.Function;

public class VersionTagContrastor extends Contrastor<String> {

  Function<String,Long> mapper;

  public VersionTagContrastor() {
    mapper = Long::parseLong;
  }

  public VersionTagContrastor(String datePattern) {
    mapper = s -> DateTimeUtil.parseDateTime(s,datePattern).getTime();
  }

  public VersionTagContrastor(Function<String,Long> mapper) {
    this.mapper = mapper;
  }



  @Override
  public boolean comparable(String v1, String v2) {
    if (v1 == null || v2 == null) {
      return false;
    }
    VersionTagType t1 = VersionIdentifier.detectVersionTag(v1);
    VersionTagType t2 = VersionIdentifier.detectVersionTag(v2);
    // lenient - always try to compare two generic types
    return t1 == t2 || t1 == VersionTagType.GENERIC || t2 == VersionTagType.GENERIC;
  }

  @Override
  public int compare(String v1, String v2) {
    VersionTagType t1 = VersionIdentifier.detectVersionTag(v1);
    VersionTagType t2 = VersionIdentifier.detectVersionTag(v2);
    VersionTagType tagType = (t1 == t2)
        ? t1
        : VersionTagType.GENERIC;

    switch (tagType) {
      case SEM_VER:
        Version sv1 = Version.valueOf(v1);
        Version sv2 = Version.valueOf(v2);
        return sv1.compareWithBuildsTo(sv2);
      case TIMESTAMP:
        return parseDateTime(v1).compareTo(parseDateTime(v2));
      case SEQUENTIAL:
        return (int) (parseLong(v1) - parseLong(v2));
      case GENERIC:
        long i1 = mapper.apply(v1);
        long i2 = mapper.apply(v2);
        return (int) (i1 - i2);
      default:
        throw new UnsupportedOperationException(
            "Unable to compare generic version tags <" + v1 + "> vs <" + v2 + ">");
    }

  }

}
