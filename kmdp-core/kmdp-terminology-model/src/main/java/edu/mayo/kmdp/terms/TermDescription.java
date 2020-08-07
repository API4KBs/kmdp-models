package edu.mayo.kmdp.terms;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.omg.spec.api4kp._1_0.id.Term;

public interface TermDescription extends Taxonomic<Term> {

  UUID getUuid();

  String getLabel();

  String getTag();

  String getVersionTag();

  List<String> getTags();

  URI getReferentId();

  URI getResourceId();

  URI getNamespaceUri();

}
