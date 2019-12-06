package edu.mayo.kmdp.terms;

import edu.mayo.kmdp.id.Term;
import java.net.URI;
import java.util.List;
import java.util.UUID;

public interface TermDescription extends Taxonomic<Term> {

  String getLabel();

  String getTag();

  List<String> getTags();

  UUID getConceptUUID();

  URI getRef();

  URI getConceptId();

}
