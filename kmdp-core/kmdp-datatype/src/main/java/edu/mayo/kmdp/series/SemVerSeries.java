package edu.mayo.kmdp.series;

import com.github.zafarkhaja.semver.Version;
import java.util.LinkedList;
import java.util.List;

public interface SemVerSeries<T extends SemVersionable<T>> extends Series<T> {

  default Version getLatestVersion() {
    return getLatest().getVersionIdentifier().getSemanticVersionTag();
  }

  @Override
  default List<T> sortedByVersion() {
    List<T> list = new LinkedList<>(getVersions());
    list.sort(SemVersionable.highestVersionFirstComparator());
    return list;
  }

}
