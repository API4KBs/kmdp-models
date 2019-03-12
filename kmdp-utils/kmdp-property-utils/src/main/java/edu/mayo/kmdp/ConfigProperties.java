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
import java.util.Optional;
import java.util.Properties;
import java.util.function.BiConsumer;

public abstract class ConfigProperties<P extends ConfigProperties<P, O>,
    O extends Option<?>> extends Properties {

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

  public <T> T getTyped(O param) {
    return getTyped(param, (Class<T>) param.getOption().getType());
  }

  public <T> T getTyped(O param, Class<T> type) {
    try {
      String s = get(param).orElse(null);
      if (s == null) {
        return null;
      } else if (type.equals(Class.class)) {
        return (T) Class.forName(s).newInstance();
      } else {
        return type.getConstructor(String.class).newInstance(s);
      }
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
      e.printStackTrace();
      return null;
    }
  }

  public P with(O param, Object value) {
    put(asKey(param), value != null ? value.toString() : null);
    return (P) this;
  }

  public P withOptional(O param, Object value) {
    if (value != null) {
      return with(param, value);
    }
    return (P) this;
  }

  public void consume(BiConsumer<String, String> consumer) {
    for (O o : listProperties()) {
      String key = asKey(o);
      get(o).ifPresent((value) ->
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

  protected abstract O[] properties();

  public P from(Properties p) {
    this.putAll(p);
    return (P) this;
  }
}