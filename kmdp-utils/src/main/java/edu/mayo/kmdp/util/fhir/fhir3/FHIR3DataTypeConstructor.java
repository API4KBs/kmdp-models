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
package edu.mayo.kmdp.util.fhir.fhir3;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import org.hl7.fhir.dstu3.model.Attachment;
import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.CodeType;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DataElement;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.DateType;
import org.hl7.fhir.dstu3.model.DecimalType;
import org.hl7.fhir.dstu3.model.ElementDefinition;
import org.hl7.fhir.dstu3.model.ElementDefinition.TypeRefComponent;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.Enumerations.DataType;
import org.hl7.fhir.dstu3.model.IntegerType;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.TimeType;
import org.hl7.fhir.dstu3.model.Type;
import org.hl7.fhir.exceptions.FHIRException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FHIR3DataTypeConstructor {

  private static final Logger logger = LoggerFactory.getLogger(FHIR3DataTypeConstructor.class);

  private FHIR3DataTypeConstructor() {}

  // Used so often it deserves a constant
  private static final String VALUE = "value";

  @SuppressWarnings("unchecked")
  public static Optional<Type> construct(DataElement schema, Object data) {
    return data instanceof Map<?,?>
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
          t = buildDateTime(data);
          break;
        case TIME:
          t = buildTime(data);
          break;
        case DATE:
          t = buildDate(data);
          break;
        case INTEGER:
          t = buildInteger(data);
          break;
        case QUANTITY:
          t = buildQuantity(schema, data);
          break;
        case DECIMAL:
          t = buildDecimal(data);
          break;
        case CODING:
          t = buildCoding(schema, data);
          break;
        case CODE:
          t = buildCode(data);
          break;
        case BOOLEAN:
          t = buildBoolean(data);
          break;
        case STRING:
          t = buildString(data);
          break;
        case ATTACHMENT:
          t = buildAttachment(data);
          break;
        default:
          throw new UnsupportedOperationException("Unable construct an instance of Type " + dt);
      }
      return Optional.ofNullable(t);
    } catch (FHIRException e) {
      logger.error(e.getMessage(),e);
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
          t = buildDateTimeSimple(data);
          break;
        case TIME:
          t = buildTimeSimple(data);
          break;
        case DATE:
          t = buildDateSimple(data);
          break;
        case INTEGER:
          t = buildIntegerSimple(data);
          break;
        case QUANTITY:
          t = buildQuantitySimple(schema, data);
          break;
        case DECIMAL:
          t = buildDecimalSimple(data);
          break;
        case CODING:
          t = buildCodingSimple(schema, data);
          break;
        case CODE:
          t = buildCodeSimple(data);
          break;
        case BOOLEAN:
          t = buildBooleanSimple(data);
          break;
        case STRING:
          t = buildStringSimple(data);
          break;
        case ATTACHMENT:
          t = buildAttachmentSimple(data);
          break;
        default:
          throw new UnsupportedOperationException("Unable construct an instance of Type " + dt);
      }
      return Optional.ofNullable(t);
    } catch (FHIRException e) {
      logger.error(e.getMessage(),e);
    }
    return Optional.empty();
  }


  private static IntegerType buildInteger(Map<String, Object> data) {
    return data.containsKey(VALUE)
        ? (IntegerType) new IntegerType().setValue((Integer) data.get(VALUE))
        : null;
  }

  private static IntegerType buildIntegerSimple(Object data) {
    return data instanceof Integer
        ? (IntegerType) new IntegerType().setValue((Integer) data)
        : null;
  }

  private static DateTimeType buildDateTime(Map<String, Object> data) {
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

  private static DateTimeType buildDateTimeSimple(Object data) {
    return data instanceof Date
        ? (DateTimeType) new DateTimeType().setValue((Date) data)
        : null;
  }

  private static DateType buildDate(Map<String, Object> data) {
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

  private static DateType buildDateSimple(Object data) {
    return data instanceof Date
        ? (DateType) new DateType().setValue((Date) data)
        : null;
  }


  private static TimeType buildTime(Map<String, Object> data) {
    return data.containsKey(VALUE)
        ? (TimeType) new TimeType().setValue((String) data.get(VALUE))
        : null;
  }

  private static TimeType buildTimeSimple(Object data) {
    return data instanceof String
        ? (TimeType) new TimeType().setValue((String) data)
        : null;
  }


  private static DecimalType buildDecimal(Map<String, Object> data) {
    return data.containsKey(VALUE)
        ? (DecimalType) new DecimalType().setValue((BigDecimal) data.get(VALUE))
        : null;
  }


  private static DecimalType buildDecimalSimple(Object data) {
    return data instanceof BigDecimal
        ? (DecimalType) new DecimalType().setValue((BigDecimal) data)
        : null;
  }


  private static Quantity buildQuantity(DataElement schema, Map<String, Object> data) {
    Quantity q = data.containsKey(VALUE)
        ? new Quantity().setValue((BigDecimal) data.get(VALUE))
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


  private static CodeType buildCode(Map<String, Object> data) {
    return data.containsKey(VALUE)
        ? (CodeType) new CodeType().setValue((String) data.get(VALUE))
        : null;
  }

  private static CodeType buildCodeSimple(Object data) {
    return data instanceof String
        ? (CodeType) new CodeType().setValue((String) data)
        : null;
  }


  private static Coding buildCoding(DataElement schema, Map<String, Object> data) {
    Coding c = data.containsKey(VALUE)
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


  private static StringType buildString(Map<String, Object> data) {
    return data.containsKey(VALUE)
        ? (StringType) new StringType().setValue((String) data.get(VALUE))
        : null;
  }

  private static StringType buildStringSimple(Object data) {
    return data instanceof String
        ? (StringType) new StringType().setValue((String) data)
        : null;
  }

  private static BooleanType buildBoolean(Map<String, Object> data) {
    return data.containsKey(VALUE)
        ? (BooleanType) new BooleanType().setValue((Boolean) data.get(VALUE))
        : null;
  }

  private static BooleanType buildBooleanSimple(Object data) {
    return data instanceof Boolean
        ? (BooleanType) new BooleanType().setValue((Boolean) data)
        : null;
  }


  private static Attachment buildAttachment(Map<String, Object> data) {
    return data.containsKey("url")
        ? new Attachment().setTitle((String) data.get("title"))
        .setUrl((String) data.get("url"))
        .setSize((Integer) data.get("size"))
        .setLanguage((String) data.get("language"))
        : null;
  }

  private static Attachment buildAttachmentSimple(Object data) {
    return data instanceof String
        ? new Attachment().setUrl((String) data)
        : null;
  }

  @Deprecated
  //TODO
  public static Optional<DataElement> getType() {
    return Optional.ofNullable(
        new DataElement().addElement(
            new ElementDefinition().addType(
                new TypeRefComponent().setCode(DataType.QUANTITY.toCode())
            )
        ));
  }
}
