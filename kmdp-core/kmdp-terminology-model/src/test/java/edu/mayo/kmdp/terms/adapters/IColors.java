package edu.mayo.kmdp.terms.adapters;

import org.omg.spec.api4kp._20200801.terms.TypedTerm;
import org.omg.spec.api4kp._20200801.series.Series;
import org.omg.spec.api4kp._20200801.terms.ConceptTerm;
import org.omg.spec.api4kp._20200801.terms.VersionableTerm;

@com.fasterxml.jackson.databind.annotation.JsonDeserialize( using = ColorsSeries.JsonDeserializer.class )
public interface IColors extends ConceptTerm, TypedTerm<IColors> {

  default ColorsSeries asEnum() {
    return ColorsSeries.resolve(this);
  }


  interface IColorsVersion extends IColors, VersionableTerm<IColorsVersion, IColors> {

    Series<IColorsVersion,IColors> asSeries();

  }
}
