package edu.mayo.kmdp.terms.example.sch1;

import edu.mayo.kmdp.terms.VersionableTerm;
import edu.mayo.kmdp.util.DateTimeUtil;
import java.net.URI;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;

@com.fasterxml.jackson.databind.annotation.JsonDeserialize( using = SCH1Series.JsonDeserializer.class )
public interface ISCH1 extends VersionableTerm<ISCH1,SCH1Series> {

  String schemeName = "SCH1";
  String schemeID = "0.0.0.0";

  URIIdentifier seriesUri = new URIIdentifier()
      .withUri(URI.create("http://test/generator#concept_scheme1"));

  NamespaceIdentifier seriesNamespace = new NamespaceIdentifier()
      .withId(seriesUri.getUri())
      .withLabel("Concept Scheme 1")
      .withTag("concept_scheme_1");

  default URIIdentifier getSeriesUri() {
    return (URIIdentifier) seriesUri.clone();
  }

  default boolean equals(ISCH1 other) {
    return true;
  }

}
