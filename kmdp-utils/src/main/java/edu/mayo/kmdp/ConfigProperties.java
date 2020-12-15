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

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public abstract class ConfigProperties<P extends ConfigProperties<P, O>,
    O extends Option<?>> extends Properties {

  private static Logger logger = LoggerFactory.getLogger(ConfigProperties.class);

  protected static <O extends Option> Properties defaulted(Class<O> barOptsClass) {
    Properties defaults = new Properties();
    for (O opt : barOptsClass.getEnumConstants()) {
      if (opt.getDefaultValue() != null) {
        defaults.put(opt.getName(), opt.getDefaultValue());
      }
    }
    return defaults;
  }

  protected ConfigProperties(Properties defaults) {
    super(defaults);
  }


  public Optional<String> get(O param) {
    return Optional.ofNullable(getProperty(asKey(param), param.getDefaultValue()));
  }


  private String asKey(O param) {
    return param.getOption().getName();
  }

  @SuppressWarnings("unchecked")
  public <T> T getTyped(O param) {
    return getTyped(param, (Class<T>) param.getOption().getType());
  }

  @SuppressWarnings("unchecked")
  public <T> T getTyped(O param, Class<T> type) {
    try {
      String s = get(param).orElse(null);
      if (s == null) {
        return null;
      } else if (type.equals(Class.class)) {
        return (T) Class.forName(s).getConstructor().newInstance();
      } else if (type.isEnum()) {
        return asEnum(type,s);
      } else {
        return type.getConstructor(String.class).newInstance(s);
      }
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
      logger.error(e.getMessage(),e);
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  private <T extends Enum,X> X asEnum(Class<X> type, String s) {
    return (X) Enum.valueOf(((Class<T>) type),s);
  }

  @SuppressWarnings("unchecked")
  public P with(O param, Object value) {
    if (value == null) {
      return (P) this;
    }
    put(asKey(param), value.toString());
    return (P) this;
  }

  @SuppressWarnings("unchecked")
  public P withOptional(O param, Object value) {
    if (value != null) {
      return with(param, value);
    }
    return (P) this;
  }

  public void consume(BiConsumer<String, String> consumer) {
    for (O o : listProperties()) {
      String key = asKey(o);
      get(o).ifPresent(value ->
          consumer.accept(key, value));
    }
  }

  public void consumeTyped(BiConsumer<String, Object> consumer) {
    for (O o : listProperties()) {
      String key = asKey(o);
      Object value = getTyped(o, o.getOption().getType());
      if (value != null) {
        consumer.accept(key, value);
      }
    }
  }

  protected Iterable<O> listProperties() {
    return Arrays.asList(properties());
  }

  public abstract O[] properties();

  @SuppressWarnings("unchecked")
  public P from(Properties p) {
    if (p != null) {
      this.putAll(p);
    }
    return (P) this;
  }

  public Map<String,Object> toMap() {
    return Arrays.stream(properties())
        .collect(Collectors.toMap(Object::toString,
            prop -> getTyped(prop,String.class)));
  }
}