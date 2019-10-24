package edu.mayo.kmdp.series;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.zafarkhaja.semver.Version;
import edu.mayo.kmdp.id.SemVerIdentifier;
import edu.mayo.kmdp.id.VersionedIdentifier;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.omg.spec.api4kp._1_0.identifiers.VersionIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.VersionTagType;

public interface SemVerSeries<T extends SemVersionable<T>> extends Series<T> {

  default Version getLatestVersion() {
    return getLatest().getVersionIdentifier().getSemanticVersion();
  }

  @Override
  default VersionedIdentifier newIdentifier(String tag, String versionTag, Date d) {
    return new SemVerId()
        .withTag(tag)
        .withVersion(versionTag)
        .withVersioning(VersionTagType.SEM_VER)
        .withEstablishedOn(d);
  }

  @Override
  default List<T> sortedByVersion() {
    List<T> list = new LinkedList<>(getVersions());
    list.sort(SemVersionable.highestVersionFirstComparator());
    return list;
  }


  class SemVerId extends VersionIdentifier implements SemVerIdentifier {

    transient Version semVer;

    @JsonIgnore
    @Override
    public Version getSemanticVersion() {
      if (semVer == null) {
        semVer = Version.valueOf(getVersion());
      }
      return semVer;
    }

    @Override
    public boolean equals(Object o) {
      return super.equals(o);
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }
  }
}
