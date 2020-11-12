package edu.mayo.kmdp.terms.example.cito;

import java.net.URI;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.terms.TypedTerm;
import org.omg.spec.api4kp._20200801.id.VersionIdentifier;
import org.omg.spec.api4kp._20200801.series.Series;
import org.omg.spec.api4kp._20200801.terms.ConceptTerm;
import org.omg.spec.api4kp._20200801.terms.VersionableTerm;

public interface ICito extends ConceptTerm, TypedTerm<ICito> {

  String schemeName = "cito";
  String schemeID = "cito";

  ResourceIdentifier schemeSeriesIdentifier =
      SemanticIdentifier.newNamedId(
          URI.create("http://test.skos.foo#cito"),
          schemeID,
          schemeName);

  default CitoSeries asEnum() {
    return CitoSeries.resolve(this);
  }

  interface ICitoVersion extends ICito, VersionableTerm<ICito.ICitoVersion, ICito> {
    @Override
    default VersionIdentifier getVersionIdentifier() {
      return this;
    }

    Series<ICitoVersion,ICito> asSeries();
  }

}
