package edu.mayo.kmdp.terms.example.sch1;

import java.net.URI;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.terms.TypedTerm;
import org.omg.spec.api4kp._20200801.id.VersionIdentifier;
import org.omg.spec.api4kp._20200801.series.Series;
import org.omg.spec.api4kp._20200801.terms.ConceptTerm;
import org.omg.spec.api4kp._20200801.terms.VersionableTerm;

@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = SCH1Series.JsonDeserializer.class)
public interface ISCH1 extends ConceptTerm, TypedTerm<ISCH1> {

  interface ISCH1Version extends ISCH1, VersionableTerm<ISCH1Version, ISCH1> {
    @Override
    default VersionIdentifier getVersionIdentifier() {
      return this;
    }

    Series<ISCH1Version, ISCH1> asSeries();
  }
}
