package edu.mayo.kmdp.comparator;

import java.util.List;
import org.javers.core.Javers;
import org.javers.core.diff.Change;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.NewObject;
import org.javers.core.diff.changetype.ObjectRemoved;
import org.javers.core.diff.changetype.PropertyChange;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.core.diff.changetype.container.ContainerChange;
import org.javers.core.diff.changetype.map.MapChange;

public abstract class AbstractDiffer<T> extends Contrastor<T> {

  public enum Mode { EQUALITY_ONLY, PARTIAL_ORDER, SYMMETRIC }

  protected Javers differ;
  protected final Mode mode;

  protected AbstractDiffer(Mode mode) {
    this.mode = mode;
    this.differ = init();
  }

  protected AbstractDiffer() {
    this(Mode.EQUALITY_ONLY);
  }

  protected abstract Javers init();

  public Diff diff(T subject, T base) {
    return differ.compare(
        normalize(base),
        normalize(subject));
  }

  @Override
  public boolean comparable(T first, T second) {
    return true;
  }

  @Override
  public int compare(T subject, T base) {
    Diff diff = diff(subject, base);
    List<Change> delta = diff.getChanges();

    if (delta.isEmpty()) {
      return 0;
    }
    if (mode.ordinal() >= Mode.PARTIAL_ORDER.ordinal()
        && delta.stream().allMatch(this::isAdditive)) {
      return 1;
    }
    if (mode.ordinal() >= Mode.SYMMETRIC.ordinal()
        && delta.stream().allMatch(this::isSubtractive)) {
      return -1;
    }
    return Integer.MAX_VALUE;
  }

  private boolean isAdditive(Change change) {
    if (change instanceof ContainerChange) {
      ContainerChange cchange = (ContainerChange) change;
      return !cchange.getValueAddedChanges().isEmpty()
          && cchange.getValueRemovedChanges().isEmpty();
    } else if (change instanceof MapChange) {
      MapChange mapChange = (MapChange) change;
      return ! mapChange.getEntryAddedChanges().isEmpty()
          && mapChange.getEntryRemovedChanges().isEmpty()
          && mapChange.getEntryValueChanges().isEmpty();
    } else if (change instanceof NewObject) {
      return true;
    } else if (change instanceof ObjectRemoved) {
      return false;
    } else if (change instanceof ValueChange) {
      ValueChange vChange = (ValueChange) change;
      return vChange.getLeft() == null;
    } else if (change instanceof PropertyChange) {
      PropertyChange pChange = (PropertyChange) change;
      return pChange.isPropertyAdded();
    }
    throw new UnsupportedOperationException("Unexpected Change Type " + change);
  }


  private boolean isSubtractive(Change change) {
    if (change instanceof ContainerChange) {
      ContainerChange cchange = (ContainerChange) change;
      return cchange.getValueAddedChanges().isEmpty()
          && ! cchange.getValueRemovedChanges().isEmpty();
    } else if (change instanceof MapChange) {
      MapChange mapChange = (MapChange) change;
      return mapChange.getEntryAddedChanges().isEmpty()
          && ! mapChange.getEntryRemovedChanges().isEmpty()
          && mapChange.getEntryValueChanges().isEmpty();
    } else if (change instanceof NewObject) {
      return false;
    } else if (change instanceof ObjectRemoved) {
      return true;
    } else if (change instanceof ValueChange) {
      ValueChange vChange = (ValueChange) change;
      return vChange.getRight() == null;
    } else if (change instanceof PropertyChange) {
      PropertyChange pChange = (PropertyChange) change;
      return pChange.isPropertyRemoved();
    }
    throw new UnsupportedOperationException("Unexpected Change Type " + change);
  }

  @Override
  public Comparison contrast(T first, T second) {
    if (first == null || second == null) {
      return Comparison.UNKNOWN;
    }
    if (first == second) {
      return Comparison.IDENTICAL;
    }
    int comp = compare(first, second);
    switch (comp) {
      case 0:
        return Comparison.EQUIVALENT;
      case 1:
        return Comparison.BROADER;
      case -1:
        return Comparison.NARROWER;
      default:
    }
    if (first.equals(second)) {
      return Comparison.EQUAL;
    }
    return Comparison.DISTINCT;
  }

  protected T normalize(T x) {
    return x;
  }

}
