package edu.mayo.kmdp.terms.example.sch1;

import org.omg.spec.api4kp._20200801.terms.VersionableTerm;
import java.net.URI;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;

@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = SCH1Series.JsonDeserializer.class)
public interface ISCH1 extends VersionableTerm<ISCH1, SCH1Series> {

  String schemeName = "SCH1";
  String schemeID = "0.0.0.0";

  ResourceIdentifier seriesUri =
      SemanticIdentifier.newId(URI.create("http://test/generator#concept_scheme1"));

  ResourceIdentifier seriesNamespace =
      SemanticIdentifier.newNamedId(
          seriesUri.getResourceId(),
          "concept_scheme_1",
          "Concept Scheme 1");

  default ResourceIdentifier getSeriesUri() {
    return (ResourceIdentifier) seriesUri.clone();
  }

  default boolean equals(ISCH1 other) {
    return true;
  }

  default URI getNamespaceUri() {
    return seriesUri.getResourceId();
  }
}
