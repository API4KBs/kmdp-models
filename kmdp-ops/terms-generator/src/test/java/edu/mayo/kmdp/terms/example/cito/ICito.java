package edu.mayo.kmdp.terms.example.cito;

import edu.mayo.kmdp.terms.VersionableTerm;
import java.net.URI;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;

public interface ICito extends VersionableTerm<ICito> {

  String schemeName = "cito";
  String schemeID = "cito";

  URIIdentifier seriesUri = new URIIdentifier()
      .withUri(URI.create("http://test.skos.foo#cito"));

  default URIIdentifier getSeriesUri() {
    return (URIIdentifier) seriesUri.clone();
  }

}
