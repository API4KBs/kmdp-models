package edu.mayo.kmdp.terms.example.cito;

import edu.mayo.kmdp.terms.VersionableTerm;
import java.net.URI;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;

public interface ICito extends VersionableTerm<ICito, CitoSeries> {

  String schemeName = "cito";
  String schemeID = "cito";

  ResourceIdentifier seriesUri =
      SemanticIdentifier.newId(URI.create("http://test.skos.foo#cito"));

  default ResourceIdentifier getSeriesUri() {
    return (ResourceIdentifier) seriesUri.clone();
  }

  default URI getNamespaceUri() {
    return seriesUri.getResourceId();
  }
}
