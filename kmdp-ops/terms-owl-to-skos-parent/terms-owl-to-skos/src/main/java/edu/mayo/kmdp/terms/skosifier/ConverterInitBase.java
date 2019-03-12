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

import edu.mayo.kmdp.util.JenaUtil;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ConverterInitBase {

  protected String baseURI;

  protected List<Query> arQueries;

  protected List<Query> getQueriesForModes(Modes... modes) {
    return loadQueries(Arrays.stream(modes)
        .map((m) -> m.queries)
        .reduce(new ArrayList<>(),
            (acc, list) -> {
              acc.addAll(list);
              return acc;
            }));
  }

  private List<Query> loadQueries(List<String> queryFiles) {
    return queryFiles.stream()
        .map(JenaUtil::read)
        .map(this::createParamQueryString)
        .map(ParameterizedSparqlString::toString)
        .map(QueryFactory::create)
        .collect(Collectors.toList());
  }

  private ParameterizedSparqlString createParamQueryString(String s) {
    ParameterizedSparqlString pqs = new ParameterizedSparqlString(s, baseURI);
    pqs.setNsPrefix("tgt", baseURI + "#");
    return pqs;
  }


}
