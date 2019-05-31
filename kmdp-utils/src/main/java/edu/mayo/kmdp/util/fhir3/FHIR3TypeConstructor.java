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
package edu.mayo.kmdp.util.fhir3;

import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

public class FHIR3TypeConstructor {

  public static Optional<Type> construct(DataElement schema, Object data) {
    return init(schema, data);
  }

  private static Optional<Type> init(DataElement schema, Object data) {
    if (data instanceof Type) {
      return Optional.of((Type) data);
    }
    return data instanceof Map
        ? constructFromMap(schema, (Map<String, Object>) data)
        : constructFromSimple(schema, data);
  }

  public static Optional<Type> constructFromMap(DataElement schema, Map<String, Object> data) {
    try {
      Enumerations.DataType dt = Enumerations.DataType
          .fromCode(schema.getElementFirstRep().getTypeFirstRep().getCode());
      Type t;
      switch (dt) {
        case DATETIME:
          t = buildDateTime(schema, data);
          break;
        case TIME:
          t = buildTime(schema, data);
          break;
        case DATE:
          t = buildDate(schema, data);
          break;
        case INTEGER:
          t = buildInteger(schema, data);
          break;
        case QUANTITY:
          t = buildQuantity(schema, data);
          break;
        case DECIMAL:
          t = buildDecimal(schema, data);
          break;
        case CODING:
          t = buildCoding(schema, data);
          break;
        case CODE:
          t = buildCode(schema, data);
          break;
        case BOOLEAN:
          t = buildBoolean(schema, data);
          break;
        case STRING:
          t = buildString(schema, data);
          break;
        case ATTACHMENT:
          t = buildAttachment(schema, data);
          break;
        default:
          throw new UnsupportedOperationException("Unable construct an instance of Type " + dt);
      }
      return Optional.ofNullable(t);
    } catch (FHIRException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }

  public static Optional<Type> constructFromSimple(DataElement schema, Object data) {
    try {
      Enumerations.DataType dt = Enumerations.DataType
          .fromCode(schema.getElementFirstRep().getTypeFirstRep().getCode());
      Type t;
      switch (dt) {
        case DATETIME:
          t = buildDateTimeSimple(schema, data);
          break;
        case TIME:
          t = buildTimeSimple(schema, data);
          break;
        case DATE:
          t = buildDateSimple(schema, data);
          break;
        case INTEGER:
          t = buildIntegerSimple(schema, data);
          break;
        case QUANTITY:
          t = buildQuantitySimple(schema, data);
          break;
        case DECIMAL:
          t = buildDecimalSimple(schema, data);
          break;
        case CODING:
          t = buildCodingSimple(schema, data);
          break;
        case CODE:
          t = buildCodeSimple(schema, data);
          break;
        case BOOLEAN:
          t = buildBooleanSimple(schema, data);
          break;
        case STRING:
          t = buildStringSimple(schema, data);
          break;
        case ATTACHMENT:
          t = buildAttachmentSimple(schema, data);
          break;
        default:
          throw new UnsupportedOperationException("Unable construct an instance of Type " + dt);
      }
      return Optional.ofNullable(t);
    } catch (FHIRException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }


  private static IntegerType buildInteger(DataElement schema, Map<String, Object> data) {
    return data.containsKey("value")
        ? (IntegerType) new IntegerType().setValue((Integer) data.get("value"))
        : null;
  }

  private static IntegerType buildIntegerSimple(DataElement schema, Object data) {
    if (data instanceof Integer) {
      return (IntegerType) new IntegerType().setValue((Integer) data);
    } else if (data instanceof Number) {
      return (IntegerType) new IntegerType().setValue(((Number) data).intValue());
    } else {
      return null;
    }
  }

  private static DateTimeType buildDateTime(DataElement schema, Map<String, Object> data) {
    return data.containsKey("year")
        ? (DateTimeType) new DateTimeType().setYear((Integer) data.getOrDefault("year", 1900))
        .setMonth((Integer) data.getOrDefault("month", 1))
        .setDay((Integer) data.getOrDefault("day", 1))
        .setHour((Integer) data.getOrDefault("hour", 0))
        .setMinute((Integer) data.getOrDefault("minute", 0))
        .setSecond((Integer) data.getOrDefault("second", 0))
        .setMillis((Integer) data.getOrDefault("millis", 0))
        .setNanos((Integer) data.getOrDefault("nanos", 0))
        : null;
  }

  private static DateTimeType buildDateTimeSimple(DataElement schema, Object data) {
    return data instanceof Date
        ? (DateTimeType) new DateTimeType().setValue((Date) data)
        : null;
  }

  private static DateType buildDate(DataElement schema, Map<String, Object> data) {
    return data.containsKey("year")
        ? (DateType) new DateType().setYear((Integer) data.getOrDefault("year", 1900))
        .setMonth((Integer) data.getOrDefault("month", 1))
        .setDay((Integer) data.getOrDefault("day", 1))
        .setHour((Integer) data.getOrDefault("hour", 0))
        .setMinute((Integer) data.getOrDefault("minute", 0))
        .setSecond((Integer) data.getOrDefault("second", 0))
        .setMillis((Integer) data.getOrDefault("millis", 0))
        .setNanos((Integer) data.getOrDefault("nanos", 0))
        : null;
  }

  private static DateType buildDateSimple(DataElement schema, Object data) {
    return data instanceof Date
        ? (DateType) new DateType().setValue((Date) data)
        : null;
  }


  private static TimeType buildTime(DataElement schema, Map<String, Object> data) {
    return data.containsKey("value")
        ? (TimeType) new TimeType().setValue((String) data.get("value"))
        : null;
  }

  private static TimeType buildTimeSimple(DataElement schema, Object data) {
    return data instanceof String
        ? (TimeType) new TimeType().setValue((String) data)
        : null;
  }


  private static DecimalType buildDecimal(DataElement schema, Map<String, Object> data) {
    return data.containsKey("value")
        ? (DecimalType) new DecimalType().setValue((BigDecimal) data.get("value"))
        : null;
  }


  private static DecimalType buildDecimalSimple(DataElement schema, Object data) {
    return data instanceof BigDecimal
        ? (DecimalType) new DecimalType().setValue((BigDecimal) data)
        : null;
  }


  private static Quantity buildQuantity(DataElement schema, Map<String, Object> data) {
    Quantity q = data.containsKey("value")
        ? new Quantity().setValue((BigDecimal) data.get("value"))
        .setUnit((String) data.get("unit"))
        .setCode((String) data.get("code"))
        .setSystem((String) data.get("unit"))
        : null;
    return decorateQuantity(schema, q);
  }

  private static Quantity buildQuantitySimple(DataElement schema, Object data) {
    Quantity q = null;
    if (data instanceof BigDecimal) {
      q = new Quantity().setValue((BigDecimal) data);
    } else if (data instanceof Double) {
      q = new Quantity().setValue((Double) data);
    } else if (data instanceof Integer) {
      q = new Quantity().setValue((Integer) data);
    } else if (data instanceof Long) {
      q = new Quantity().setValue((Long) data);
    }
    return decorateQuantity(schema, q);
  }

  public static Quantity toFQuantity(Double value) {
    return new Quantity().setValue(value);
  }

  private static Quantity decorateQuantity(DataElement schema, Quantity q) {
    if (q != null
        && schema.getElementFirstRep().getPattern() instanceof Quantity) {
      Quantity ref = (Quantity) schema.getElementFirstRep().getPattern();
      if (ref.getCode() != null && q.getCode() == null) {
        q.setCode(ref.getCode());
      }
      if (ref.getUnit() != null && q.getUnit() == null) {
        q.setUnit(ref.getUnit());
      }
      if (ref.getSystem() != null && q.getSystem() == null) {
        q.setSystem(ref.getSystem());
      }
    }
    return q;
  }


  private static CodeType buildCode(DataElement schema, Map<String, Object> data) {
    return data.containsKey("value")
        ? (CodeType) new CodeType().setValue((String) data.get("value"))
        : null;
  }

  private static CodeType buildCodeSimple(DataElement schema, Object data) {
    return data instanceof String
        ? (CodeType) new CodeType().setValue((String) data)
        : null;
  }


  private static Coding buildCoding(DataElement schema, Map<String, Object> data) {
    Coding c = data.containsKey("value")
        ? new Coding().setCode((String) data.get("code"))
        .setSystem((String) data.get("system"))
        .setVersion((String) data.get("version"))
        .setDisplay((String) data.get("display"))
        : null;
    return decorateCoding(schema, c);
  }

  private static Coding buildCodingSimple(DataElement schema, Object data) {
    Coding c = data instanceof String
        ? new Coding().setCode((String) data)
        : null;
    return decorateCoding(schema, c);
  }

  public static Coding toFCoding(String code) {
    return (Coding) new Coding().setCode(code);
  }

  private static Coding decorateCoding(DataElement schema, Coding c) {
    if (c != null
        && schema.getElementFirstRep().getPattern() instanceof Coding) {
      Coding ref = (Coding) schema.getElementFirstRep().getPattern();
      if (ref.getCode() != null && c.getCode() == null) {
        c.setCode(ref.getCode());
      }
      if (ref.getSystem() != null && c.getSystem() == null) {
        c.setSystem(ref.getSystem());
      }
      if (ref.getVersion() != null && c.getVersion() == null) {
        c.setVersion(ref.getVersion());
      }
      if (ref.getDisplay() != null && c.getDisplay() == null) {
        c.setDisplay(ref.getDisplay());
      }
    }
    return c;
  }


  private static StringType buildString(DataElement schema, Map<String, Object> data) {
    return data.containsKey("value")
        ? (StringType) new StringType().setValue((String) data.get("value"))
        : null;
  }

  private static StringType buildStringSimple(DataElement schema, Object data) {
    return data instanceof String
        ? (StringType) new StringType().setValue((String) data)
        : null;
  }

  private static BooleanType buildBoolean(DataElement schema, Map<String, Object> data) {
    return data.containsKey("value")
        ? (BooleanType) new BooleanType().setValue((Boolean) data.get("value"))
        : null;
  }

  private static BooleanType buildBooleanSimple(DataElement schema, Object data) {
    return data instanceof Boolean
        ? (BooleanType) new BooleanType().setValue((Boolean) data)
        : null;
  }

  public static BooleanType toFBoolean(Boolean b) {
    return (BooleanType) new BooleanType().setValue(b);
  }


  private static Attachment buildAttachment(DataElement schema, Map<String, Object> data) {
    return data.containsKey("url")
        ? new Attachment().setTitle((String) data.get("title"))
        .setUrl((String) data.get("url"))
        .setSize((Integer) data.get("size"))
        .setLanguage((String) data.get("language"))
        : null;
  }

  private static Attachment buildAttachmentSimple(DataElement schema, Object data) {
    return data instanceof String
        ? new Attachment().setUrl((String) data)
        : null;
  }

  public static Object destruct(Object value) {
    if (!(value instanceof Type)) {
      return value;
    }
    try {
      Enumerations.DataType dt = Enumerations.DataType
          .fromCode(value.getClass().getAnnotation(DatatypeDef.class).name());
      switch (dt) {
        case DATETIME:
          return ((DateTimeType) value).getValue();
        case TIME:
          return ((TimeType) value).getValue();
        case DATE:
          return ((DateType) value).getValue();
        case INTEGER:
          return ((IntegerType) value).getValue();
        case QUANTITY:
          return ((Quantity) value).getValue();
        case DECIMAL:
          return ((DecimalType) value).getValue();
        case CODING:
          return ((Coding) value).getCode();
        case CODE:
          return ((CodeType) value).getValue();
        case BOOLEAN:
          return ((BooleanType) value).getValue();
        case STRING:
          return ((StringType) value).getValue();
        case ATTACHMENT:
          return ((Attachment) value).getUrl();
        default:
          return value;
      }
    } catch (FHIRException e) {
      e.printStackTrace();
    }
    return value;
  }
}
