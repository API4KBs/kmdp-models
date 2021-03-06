package org.omg.spec.api4kp._20200801.terms;

import java.net.URI;
import java.util.List;
import java.util.UUID;

public interface TermDescription extends Taxonomic {

  UUID getUuid();

  String getLabel();

  String getTag();

  String getVersionTag();

  List<String> getTags();

  URI getReferentId();

  URI getResourceId();

  URI getVersionId();

  URI getNamespaceUri();

}
