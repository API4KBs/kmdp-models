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
package edu.mayo.kmdp;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import edu.mayo.kmdp.idl.Module;
import edu.mayo.kmdp.util.FileUtil;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

public class IDLSerializer {

  protected IDLSerializer() {}

  public static List<String> serialize(Module m) {
    Template tmpl = Mustache.compiler()
        .withLoader(name -> new StringReader(load(name)))
        .compile(load("idl_module"));

    return m.getSubModules().stream()
        .map(sub -> {
          StringWriter sw = new StringWriter();
          tmpl.execute(sub, sw);
          return sw.toString();
        })
        .collect(Collectors.toList());
  }

  private static String load(String name) {
    return FileUtil.read(IDLSerializer.class.getResourceAsStream("/" + name + ".mustache"))
        .orElse("ERROR : template " + name + "NOT FOUND");
  }
}
