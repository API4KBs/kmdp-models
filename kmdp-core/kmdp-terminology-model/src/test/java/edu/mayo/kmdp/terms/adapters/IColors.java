package edu.mayo.kmdp.terms.adapters;

import edu.mayo.kmdp.terms.VersionableTerm;

@com.fasterxml.jackson.databind.annotation.JsonDeserialize( using = ColorsSeries.JsonDeserializer.class )
public interface IColors extends VersionableTerm<IColors,ColorsSeries> {


}
