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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class PropertiesUtil {

  public static final Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);
  
  private PropertiesUtil() {}

  public static Properties empty() {
    return new PropertyBuilder().get();
  }

  public static PropertyBuilder props(String defaults) {
    return new PropertyBuilder(defaults);
  }

  public static PropertyBuilder props(Properties defaults) {
    return new PropertyBuilder(defaults);
  }

  public static PropertyBuilder props() {
    return new PropertyBuilder();
  }

  public static Optional<Object> pObject(String name, Properties p) {
    try {
      return p.containsKey(name)
          ? Optional.of(Class.forName(p.getProperty(name)).getConstructor().newInstance())
          : Optional.empty();
    } catch (InstantiationException
        | IllegalAccessException
        | ClassNotFoundException
        | NoSuchMethodException
        | InvocationTargetException e) {
      logger.error(e.getMessage(),e);
      return Optional.empty();
    }
  }

  public static Optional<String> pString(String name, Properties p) {
    return Optional.ofNullable(p.getProperty(name));
  }

  public static Optional<Boolean> pBool(String name, Properties p) {
    return p.containsKey(name) ? Optional.of(Boolean.valueOf(p.getProperty(name)))
        : Optional.empty();
  }

  public static <T> Optional<T> pEnum(String name, Properties p, Function<String, T> builder) {
    return p.containsKey(name)
        ? Optional.of(builder.apply(p.getProperty(name)))
        : Optional.empty();
  }

  public static <T> Optional<T> pCustom(String name, Properties p, Function<String, T> builder) {
    return p.containsKey(name)
        ? Optional.of(builder.apply(p.getProperty(name)))
        : Optional.empty();
  }

  public static String serializeProps(Properties props) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      props.store(baos,"");
    } catch (IOException e) {
      logger.error(e.getMessage(),e);
    }
    return new String(baos.toByteArray());
  }

  public static Optional<Properties> parse(String serializedProperties) {
    if (Util.isEmpty(serializedProperties)) {
      return Optional.empty();
    }
    try {
      Properties prop = new Properties();
      prop.load(new ByteArrayInputStream(serializedProperties.getBytes()));
      return Optional.of(prop);
    } catch (IOException e) {
      logger.error(e.getMessage(),e);
      return Optional.empty();
    }
  }

  public static Properties parseProperties(String serializedProperties) {
    return parse(serializedProperties).orElse(new Properties());
  }

  public static class PropertyBuilder {

    Properties p;

    public PropertyBuilder() {
      p = new Properties();
    }

    public PropertyBuilder(String path) {
      try {
        Properties defaults = new Properties();
        defaults.load(PropertiesUtil.class.getResourceAsStream(path));
        this.p = defaults;
      } catch (IOException e) {
        logger.error(e.getMessage(),e);
        this.p = new Properties();
      }
    }

    public PropertyBuilder(Properties defaults) {
      this.p = new Properties(defaults);
    }

    public PropertyBuilder set(Object key, Object value) {
      String v;

      if (value instanceof Boolean) {
        v = value.toString();
      } else if (value instanceof Enum) {
        v = ((Enum) value).name();
      } else if (value instanceof String) {
        v = ((String) value).trim();
      } else if (value instanceof Number) {
        v = value.toString();
      } else if (value instanceof Class<?>) {
        v = ((Class) value).getName();
      } else {
        throw new UnsupportedOperationException("TO be implemented..");
      }

      p.setProperty(key.toString(), v);
      return this;
    }

    public Properties get() {
      return p;
    }

  }
}
