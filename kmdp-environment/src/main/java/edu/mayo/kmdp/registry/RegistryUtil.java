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
package edu.mayo.kmdp.registry;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

public class RegistryUtil {

  private RegistryUtil() {}

  public static List<Map<String, String>> askQuery(String qryString, Model knowledgeBase) {
    Query query = QueryFactory.create(qryString);
    try(QueryExecution qexec = QueryExecutionFactory.create(query, knowledgeBase)) {
      ResultSet rs = qexec.execSelect();
      List<Map<String, String>> result = new ArrayList<>();
      while (rs.hasNext()) {
        QuerySolution ans = rs.next();
        Map<String, String> vals = new HashMap<>();
        rs.getResultVars()
            .forEach(k -> vals.put(k, ans.contains(k) ? ans.get(k).toString() : ""));
        result.add(vals);
      }
      return result;
    }
  }

  public static String read(String path) {
    return new Scanner(
        Registry.class.getResourceAsStream(path),
        Charset.defaultCharset().name())
        .useDelimiter("\\A")
        .next();
  }

}
