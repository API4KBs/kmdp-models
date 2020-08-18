package edu.mayo.kmdp.terms.adapters;

import org.omg.spec.api4kp._20200801.terms.VersionableTerm;

@com.fasterxml.jackson.databind.annotation.JsonDeserialize( using = ColorsSeries.JsonDeserializer.class )
public interface IColors extends VersionableTerm<IColors,ColorsSeries> {


}
