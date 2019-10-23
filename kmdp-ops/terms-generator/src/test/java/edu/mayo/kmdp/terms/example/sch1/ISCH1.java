package edu.mayo.kmdp.terms.example.sch1;

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.VersionableTerm;
import java.net.URI;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;

public interface ISCH1 extends VersionableTerm<ISCH1> {

  String schemeName = "SCH1";
  String schemeID = "0.0.0.0";

  URIIdentifier seriesUri = new URIIdentifier()
      .withUri(URI.create("http://test/generator#concept_scheme1"));

  default URIIdentifier getSeriesUri() {
    return (URIIdentifier) seriesUri.clone();
  }

}
