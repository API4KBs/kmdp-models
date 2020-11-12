package org.omg.spec.api4kp._20200801.series;

import com.github.zafarkhaja.semver.Version;
import java.util.LinkedList;
import java.util.List;
import org.omg.spec.api4kp._20200801.id.Identifiable;

public interface SemVerSeries<T extends SemVersionable<T,E>,E extends Identifiable> extends Series<T,E> {

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
