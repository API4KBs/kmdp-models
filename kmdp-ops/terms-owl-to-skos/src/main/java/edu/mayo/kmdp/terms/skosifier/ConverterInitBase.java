/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.mayo.kmdp.terms.skosifier;

import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig.OWLtoSKOSTxParams;
import edu.mayo.kmdp.util.JenaUtil;
import edu.mayo.kmdp.util.NameUtils;
import edu.mayo.kmdp.util.Util;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;
import org.apache.jena.sparql.function.FunctionBase2;
import org.apache.jena.sparql.function.FunctionRegistry;

public abstract class ConverterInitBase {


  static {
    FunctionRegistry.get().put("http://kmdp.mayo.edu/sparql/function#uuidFrom", UuidFrom.class);
    FunctionRegistry.get().put("http://kmdp.mayo.edu/sparql/function#localName", LocalName.class);
  }


  protected List<Query> getQueriesForModes(Modes modes, Owl2SkosConfig cfg) {
    return modes.queries.stream()
        .map(JenaUtil::read)
        .map(qryStr -> createParamQueryString(qryStr,cfg))
        .map(ParameterizedSparqlString::toString)
        .map(QueryFactory::create)
        .collect(Collectors.toList());
  }

  private ParameterizedSparqlString createParamQueryString(String s, Owl2SkosConfig cfg) {
    String baseUri = cfg.getTyped(OWLtoSKOSTxParams.TGT_NAMESPACE);
    ParameterizedSparqlString pqs = new ParameterizedSparqlString(s, baseUri);
    pqs.setNsPrefix("tgt", baseUri.endsWith("/") ? baseUri : baseUri + "#");

    String schemeName = cfg.getTyped(OWLtoSKOSTxParams.SCHEME_NAME);
    if (!Util.isEmpty(schemeName)) {
      pqs.setLiteral("?schemeName",schemeName);
    }

    String topConcept = cfg.getTyped(OWLtoSKOSTxParams.TOP_CONCEPT_NAME);
    if (!Util.isEmpty(topConcept)) {
      pqs.setLiteral("?topName",topConcept);
    }

    return pqs;
  }


  public static class UuidFrom extends FunctionBase2 {

    public UuidFrom() {
      // Need explicit empty constructor
    }

    @Override
    public NodeValue exec(NodeValue nodeValue, NodeValue nodeValue1) {
      String cd = nodeValue.asString();
      if (!Util.isEmpty(nodeValue1.asString())) {
        cd = cd + "-" + nodeValue1.asString();
      }
      return NodeValue.makeString(Util.uuid(cd).toString());
    }

  }

  public static class LocalName extends FunctionBase1 {

    public LocalName() {
      // Need explicit empty constructor
    }

    @Override
    public NodeValue exec(NodeValue nodeValue) {
      return NodeValue.makeString(NameUtils.getTrailingPart(nodeValue.asString()));
    }
  }
}
