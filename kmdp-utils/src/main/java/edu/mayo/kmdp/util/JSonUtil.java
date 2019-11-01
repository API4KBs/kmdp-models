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

import static edu.mayo.kmdp.util.PropertiesUtil.pCustom;
import static edu.mayo.kmdp.util.PropertiesUtil.pEnum;
import static edu.mayo.kmdp.util.PropertiesUtil.props;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import edu.mayo.kmdp.util.adapters.DateAdapter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSonUtil {
  
  private static Logger logger = LoggerFactory.getLogger(JSonUtil.class);
  
  private JSonUtil() {}

  private static final String PRETTYPRINT = DefaultPrettyPrinter.class.getName();
  private static final String INCLUDES = JsonInclude.class.getName();
  private static final String DATEFORMAT = DateAdapter.class.getName();

  public static Properties defaultProperties() {
    return props()
        .set(INCLUDES, JsonInclude.Include.NON_EMPTY)
        .set(PRETTYPRINT, true)
        .set(DATEFORMAT, DateTimeUtil.DEFAULT_DATE_PATTERN).get();
  }

  public static Optional<JsonNode> readJson(InputStream data) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return Optional.ofNullable(objectMapper.readTree(data));
    } catch (IOException e) {
      logger.error(e.getMessage(),e);
      return Optional.empty();
    }
  }

  public static Optional<JsonNode> readJson(String data) {
    return data != null ? readJson(data.getBytes()) : Optional.empty();
  }

  public static <T> Optional<T> readJson(String data, Class<T> klass) {
    return data != null ? readJson(data.getBytes(), klass) : Optional.empty();
  }

  public static Optional<JsonNode> readJson(byte[] data) {
    return readJson(new ByteArrayInputStream(data));
  }

  public static <T> Optional<T> readJson(InputStream data, Class<T> klass) {
    ObjectMapper objectMapper = configureMapper(new ObjectMapper(), defaultProperties());
    try {
      return Optional.ofNullable(objectMapper.readValue(data, klass));
    } catch (IOException e) {
      logger.error(e.getMessage(),e);
      return Optional.empty();
    }
  }


  public static <T> Optional<T> readJson(byte[] data, Class<T> klass) {
    return readJson(new ByteArrayInputStream(data), klass);
  }

  public static Optional<String> writeJsonAsString(Object root) {
    return writeJson(root, null, defaultProperties())
        .flatMap(Util::asString);
  }


  public static Optional<ByteArrayOutputStream> writeJson(Object root) {
    return writeJson(root, null, defaultProperties());
  }

  public static Optional<ByteArrayOutputStream> writeXML(Object root) {
    JacksonXmlModule module = new JacksonXmlModule();
    return writeJson(root, new XmlMapper(module), module, defaultProperties());
  }

  public static Optional<String> printJson(Object root) {
    return writeJson(root, null, defaultProperties())
        .flatMap(Util::asString);
  }

  public static Optional<String> printJson(Object root, Properties p) {
    return writeJson(root, null, p)
        .flatMap(Util::asString);
  }
  public static Optional<String> printJson(Object root, Module m, Properties p) {
    return writeJson(root, m, p)
        .flatMap(Util::asString);
  }

  public static void printOutJson(Object root) {
    writeJson(root, null, defaultProperties())
        .flatMap(Util::asString)
        .ifPresent(logger::trace);
  }

  public static Optional<ByteArrayOutputStream> writeJson(Object root, Properties p) {
    return writeJson(root, null, p);
  }

  public static Optional<ByteArrayOutputStream> writeJsonLD(Object root, Properties p) {
    return writeJson(root, JSonLDUtil.initLDModule(), p);
  }

  public static Optional<ByteArrayOutputStream> writeJsonLD(Object root) {
    return writeJsonLD(root, defaultProperties());
  }

  public static Optional<ByteArrayOutputStream> writeJson(Object root, Module module,
      Properties p) {
    return writeJson(root, new ObjectMapper(), module, p);
  }

  public static Optional<ByteArrayOutputStream> writeJson(Object root, ObjectMapper mapper,
      Module module, Properties p) {
    ObjectMapper objectMapper = configure(mapper, module, p);
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      if (Boolean.TRUE.equals(Boolean.valueOf(p.getProperty(PRETTYPRINT, Boolean.TRUE.toString())))) {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(baos, root);
      } else {
        objectMapper.writeValue(baos, root);
      }
      return Optional.of(baos);
    } catch (IOException e) {
      logger.error(e.getMessage(),e);
      return Optional.empty();
    }
  }

  public static String prettyPrintJsonString(JsonNode jsonNode) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(mapper.readValue(jsonNode.toString(),
              Object.class));
    } catch (IOException e) {
      logger.error(e.getMessage(),e);
      return "";
    }
  }


  public static ObjectMapper configureMapper(ObjectMapper objectMapper) {
    return configureMapper(objectMapper, defaultProperties());
  }

  public static ObjectMapper configureMapper(ObjectMapper objectMapper, Properties p) {
    objectMapper.setSerializationInclusion(pEnum(INCLUDES, p, JsonInclude.Include::valueOf)
        .orElse(JsonInclude.Include.USE_DEFAULTS));
    objectMapper.setDateFormat(pCustom(DATEFORMAT, p, SimpleDateFormat::new)
        .orElse(new SimpleDateFormat(DateTimeUtil.DEFAULT_DATE_PATTERN)));
    objectMapper.configure(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID, true);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    objectMapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));

    return objectMapper;
  }


  private static ObjectMapper configure(ObjectMapper objectMapper, Module module, Properties p) {
    if (module != null) {
      objectMapper.registerModule(module);
    }
    return configureMapper(objectMapper, p);
  }


  public static Optional<Boolean> jBool(String name, JsonNode parent) {
    return parent != null && parent.has(name) ? Optional.of(parent.get(name).asBoolean())
        : Optional.empty();
  }

  public static Optional<String> jString(String name, JsonNode parent) {
    return parent != null && parent.has(name) ? Optional.ofNullable(parent.get(name).asText())
        : Optional.empty();
  }

  public static Optional<Date> jDate(String name, JsonNode parent) {
    return parent != null && parent.has(name) ? Optional
        .ofNullable(DateAdapter.instance().read(parent.get(name).asText())) : Optional.empty();
  }

  public static Optional<JsonNode> jNode(String name, JsonNode parent) {
    return parent != null && parent.has(name) ? Optional.ofNullable(parent.get(name))
        : Optional.empty();
  }


  public static <T> Optional<T> parseJson(JsonNode jsonNode,
      TypeReference<T> type) {
    Optional<String> jsonTxt = JSonUtil.printJson(jsonNode);
    if (jsonTxt.isPresent()) {
      try {
        return Optional.of(new ObjectMapper().readValue(jsonTxt.get(), type));
      } catch (IOException e) {
        logger.error(e.getMessage(),e);
        return Optional.empty();
      }
    }
    return Optional.empty();
  }

  public static <T> Optional<T> parseJson(JsonNode jsonNode,
      Class<T> type) {
    try {
      return Optional
          .of(new ObjectMapper().readValue(JSonUtil.printJson(jsonNode).orElse(""), type));
    } catch (IOException e) {
      logger.error(e.getMessage(),e);
      return Optional.empty();
    }
  }

  public static <T> Optional<T> tryParseJson(JsonNode jsonNode,
      Class<T> type) {
    try {
      return Optional
          .of(new ObjectMapper().readValue(JSonUtil.printJson(jsonNode).orElse(""), type));
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  public static <T> Optional<T> parseJson(String json,
      Class<T> type) {
    try {
      return Optional.of(new ObjectMapper().readValue(json, type));
    } catch (IOException e) {
      logger.error(e.getMessage(),e);
      return Optional.empty();
    }
  }

  public static <T> Optional<T> tryParseJson(String json,
      Class<T> type) {
    try {
      return Optional.of(new ObjectMapper().readValue(json, type));
    } catch (IOException e) {
      return Optional.empty();
    }
  }


  public static Optional<Object> parseJson(String json) {
    try {
      return Optional.of(new ObjectMapper().readValue(json, Object.class));
    } catch (IOException e) {
      logger.error(e.getMessage(),e);
      return Optional.empty();
    }
  }

  public static <T> Optional<T> parseJson(String json, Module mod, Class<T> klass) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      configure(mapper, mod, defaultProperties());
      Object x = mapper.readValue(json, klass);
      return klass.isInstance(x) ? Optional.of(klass.cast(x)) : Optional.empty();
    } catch (IOException e) {
      logger.error(e.getMessage(),e);
      return Optional.empty();
    }
  }


  public static <T> Optional<List<T>> parseJsonList(InputStream data, Class<T> memberKlass) {
    return parseJsonList(data, null, memberKlass);
  }

  public static <T> Optional<List<T>> parseJsonList(InputStream data, Module m, Class<T> memberKlass) {
    ObjectMapper objectMapper = configure(new ObjectMapper(), m, defaultProperties());
    try {
      return Optional.of(Arrays.asList(objectMapper.readValue(data, asArrayOf(memberKlass))));
    } catch (IOException e) {
      logger.error(e.getMessage(),e);
      return Optional.empty();
    }
  }

  public static <T> Optional<T> parseJson(String json, Module mod, TypeReference<T> type) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      configure(mapper, mod, defaultProperties());
      if (type instanceof JavaTypeReference) {
        JavaType jt = ((JavaTypeReference<T>) type).getjType();
        return Optional.of(mapper.readValue(json, jt));
      } else {
        return Optional.of(mapper.readValue(json, type));
      }
    } catch (IOException e) {
      logger.error(e.getMessage(),e);
      return Optional.empty();
    }
  }


  public static Optional<Object> parseJson(String json, Module mod) {
    return parseJson(json, mod, Object.class);
  }

  public static Optional<JsonNode> toJsonNode(Object root) {
    return toJsonNode(root, null, defaultProperties());
  }

  public static Optional<JsonNode> toJsonNode(Object root, Module module, Properties p) {
   return writeJson(root, module, p)
        .map(ByteArrayOutputStream::toByteArray)
        .flatMap(JSonUtil::readJson);
  }

  public static <K, V> JavaTypeReference<Map<K, V>> asMapOf(Class<K> keyClass,
      Class<V> valueClass) {
    return new JavaTypeReference<>(
        TypeFactory.defaultInstance().constructMapType(Map.class, keyClass, valueClass)
    );
  }

  public static <X> ArrayType asArrayOf(Class<X> klass) {
    return TypeFactory.defaultInstance().constructArrayType(klass);
  }

  private static class JavaTypeReference<T> extends TypeReference<T> {

    JavaType jType;

    public JavaTypeReference(JavaType type) {
      jType = type;
    }

    public JavaType getjType() {
      return jType;
    }
  }

}
