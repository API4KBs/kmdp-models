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
package edu.mayo.kmdp.util;

import io.swagger.models.ComposedModel;
import io.swagger.models.ModelImpl;
import io.swagger.models.RefModel;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.parser.Swagger20Parser;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class SwaggerTestUtil {

  public static Swagger parseValidate(String data) {
    return parseValidate(data, "");
  }

  public static Swagger parseValidateGroup(String path) {
    try {
      return parseValidate(FileUtil.read(SwaggerTestUtil.class.getResourceAsStream(path))
              .orElseThrow(IOException::new),
          path);
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    return null;
  }

  public static Swagger parseValidate(InputStream data) {
    try {
      return parseValidate(FileUtil.read(data).orElseThrow(IOException::new), "");
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    return null;
  }

  protected static Swagger parseValidate(String data, String path) {
    Swagger20Parser parser = new Swagger20Parser();
    try {
      Swagger model = parser.parse(data);
      assertNotNull(model);
      checkSemanticIntegrity(model, path);
      return model;
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    return null;
  }

  public static Swagger parse(InputStream data) {
    Swagger20Parser parser = new Swagger20Parser();
    try {
      Swagger model = parser.parse(FileUtil.read(data).orElseThrow(IOException::new));
      assertNotNull(model);
      return model;
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    return null;
  }

  private static void checkSemanticIntegrity(final Swagger model, final String path) {
    model.getDefinitions().values().forEach((def) -> {
      if (def instanceof ModelImpl) {
        assertNotNull(((ModelImpl) def).getXml());
        assertNotNull(((ModelImpl) def).getXml().getNamespace());
      }
      if (def instanceof RefModel) {
        assertNotNull(((RefModel) def).getReference());
      }
      if (def instanceof ComposedModel) {
        ComposedModel comp = (ComposedModel) def;
        assertEquals(2, comp.getAllOf().size());
        comp.getAllOf().forEach((part) -> {
          if (part.getReference() != null) {
            checkType(part.getReference(), model, path);
          }
          if (part.getProperties() != null) {
            part.getProperties().values().forEach((prop) -> checkProperty(prop, model, path));
          }
        });
      } else {
        if (def.getProperties() != null) {
          def.getProperties().values().forEach((prop) -> checkProperty(prop, model, path));

        }
      }

    });
  }

  private static void checkProperty(final Property prop, final Swagger model, String path) {
    if (prop instanceof RefProperty) {
      checkType(((RefProperty) prop).get$ref(), model, path);
      assertNotNull(prop.getXml());
      assertNotNull(prop.getXml().getNamespace());
    }
  }

  private static void checkType(final String reference, final Swagger model,
      final String basePath) {
    String type = reference.substring(reference.lastIndexOf('/') + 1);
    if (reference.startsWith("#")) {
      assertTrue(model.getDefinitions().containsKey(type));
    } else {
      if (basePath.length() > 0) {
        String base = basePath.substring(0, basePath.lastIndexOf('/'));
        String ref = reference.substring(0, reference.indexOf('#'));
        String path = base + '/' + ref;
        Swagger refModel = parseValidate(SwaggerTestUtil.class.getResourceAsStream(path));
        assertTrue(refModel.getDefinitions().containsKey(type));
      }
    }
  }

}
