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
package edu.mayo.kmdp.util.fhir.fhir2;


import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.dstu2.composite.AttachmentDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.resource.DataElement;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.CodeDt;
import ca.uhn.fhir.model.primitive.DateDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.DecimalDt;
import ca.uhn.fhir.model.primitive.IntegerDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.model.primitive.TimeDt;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class FHIR2DataTypeConstructor {

  private static final Logger logger = LoggerFactory.getLogger(FHIR2DataTypeConstructor.class);
  
  private FHIR2DataTypeConstructor() {}
  
  // Used so often it deserves a constant
  private static final String VALUE = "value";
  
  public static Optional<IDatatype> construct(DataElement schema, Object data) {
    return data instanceof Map
        ? constructFromMap(schema, (Map<String, Object>) data)
        : constructFromSimple(schema, data);
  }

  public static Optional<IDatatype> constructFromMap(DataElement schema, Map<String, Object> data) {
    try {
      String dt = schema.getElementFirstRep().getTypeFirstRep().getCode().toLowerCase();
      IDatatype t;
      switch (dt) {
        case "datetime":
          t = buildDateTime(data);
          break;
        case "time":
          t = buildTime(data);
          break;
        case "date":
          t = buildDate(data);
          break;
        case "integer":
          t = buildInteger(data);
          break;
        case "quantity":
          t = buildQuantity(schema, data);
          break;
        case "decimal":
          t = buildDecimal(data);
          break;
        case "coding":
          t = buildCoding(schema, data);
          break;
        case "code":
          t = buildCode(data);
          break;
        case "boolean":
          t = buildBoolean(data);
          break;
        case "string":
          t = buildString(data);
          break;
        case "attachment":
          t = buildAttachment(data);
          break;
        default:
          throw new UnsupportedOperationException("Unable construct an instance of Type " + dt);
      }
      return Optional.ofNullable(t);
    } catch (Exception e) {
      logger.error(e.getMessage(),e);
    }
    return Optional.empty();
  }

  public static Optional<IDatatype> constructFromSimple(DataElement schema, Object data) {
    try {
      String dt = schema.getElementFirstRep().getTypeFirstRep().getCode().toLowerCase();
      IDatatype t;
      switch (dt) {
        case "datetime":
          t = buildDateTimeSimple(data);
          break;
        case "time":
          t = buildTimeSimple(data);
          break;
        case "date":
          t = buildDateSimple(data);
          break;
        case "integer":
          t = buildIntegerSimple(data);
          break;
        case "quantity":
          t = buildQuantitySimple(schema, data);
          break;
        case "decimal":
          t = buildDecimalSimple(data);
          break;
        case "coding":
          t = buildCodingSimple(schema, data);
          break;
        case "code":
          t = buildCodeSimple(data);
          break;
        case "boolean":
          t = buildBooleanSimple(data);
          break;
        case "string":
          t = buildStringSimple(data);
          break;
        case "attachment":
          t = buildAttachmentSimple(data);
          break;
        default:
          throw new UnsupportedOperationException("Unable construct an instance of Type " + dt);
      }
      return Optional.ofNullable(t);
    } catch (Exception e) {
      logger.error(e.getMessage(),e);
    }
    return Optional.empty();
  }


  private static IntegerDt buildInteger(Map<String, Object> data) {
    return data.containsKey(VALUE)
        ? (IntegerDt) new IntegerDt().setValue((Integer) data.get(VALUE))
        : null;
  }

  private static IntegerDt buildIntegerSimple(Object data) {
    return data instanceof Integer
        ? (IntegerDt) new IntegerDt().setValue((Integer) data)
        : null;
  }

  private static DateTimeDt buildDateTime(Map<String, Object> data) {
    return data.containsKey("year")
        ? (DateTimeDt) new DateTimeDt().setYear((Integer) data.getOrDefault("year", 1900))
        .setMonth((Integer) data.getOrDefault("month", 1))
        .setDay((Integer) data.getOrDefault("day", 1))
        .setHour((Integer) data.getOrDefault("hour", 0))
        .setMinute((Integer) data.getOrDefault("minute", 0))
        .setSecond((Integer) data.getOrDefault("second", 0))
        .setMillis((Integer) data.getOrDefault("millis", 0))
        .setNanos((Integer) data.getOrDefault("nanos", 0))
        : null;
  }

  private static DateTimeDt buildDateTimeSimple(Object data) {
    return data instanceof Date
        ? (DateTimeDt) new DateTimeDt().setValue((Date) data)
        : null;
  }

  private static DateDt buildDate(Map<String, Object> data) {
    return data.containsKey("year")
        ? (DateDt) new DateDt().setYear((Integer) data.getOrDefault("year", 1900))
        .setMonth((Integer) data.getOrDefault("month", 1))
        .setDay((Integer) data.getOrDefault("day", 1))
        .setHour((Integer) data.getOrDefault("hour", 0))
        .setMinute((Integer) data.getOrDefault("minute", 0))
        .setSecond((Integer) data.getOrDefault("second", 0))
        .setMillis((Integer) data.getOrDefault("millis", 0))
        .setNanos((Integer) data.getOrDefault("nanos", 0))
        : null;
  }

  private static DateDt buildDateSimple(Object data) {
    return data instanceof Date
        ? (DateDt) new DateDt().setValue((Date) data)
        : null;
  }


  private static TimeDt buildTime(Map<String, Object> data) {
    return data.containsKey(VALUE)
        ? (TimeDt) new TimeDt().setValue((String) data.get(VALUE))
        : null;
  }

  private static TimeDt buildTimeSimple(Object data) {
    return data instanceof String
        ? (TimeDt) new TimeDt().setValue((String) data)
        : null;
  }


  private static DecimalDt buildDecimal(Map<String, Object> data) {
    return data.containsKey(VALUE)
        ? (DecimalDt) new DecimalDt().setValue((BigDecimal) data.get(VALUE))
        : null;
  }


  private static DecimalDt buildDecimalSimple(Object data) {
    return data instanceof BigDecimal
        ? (DecimalDt) new DecimalDt().setValue((BigDecimal) data)
        : null;
  }


  private static QuantityDt buildQuantity(DataElement schema, Map<String, Object> data) {
    QuantityDt q = data.containsKey(VALUE)
        ? new QuantityDt().setValue((BigDecimal) data.get(VALUE))
        .setUnit((String) data.get("unit"))
        .setCode((String) data.get("code"))
        .setSystem((String) data.get("unit"))
        : null;
    return decorateQuantity(schema, q);
  }

  private static QuantityDt buildQuantitySimple(DataElement schema, Object data) {
    QuantityDt q = null;
    if (data instanceof BigDecimal) {
      q = new QuantityDt().setValue((BigDecimal) data);
    } else if (data instanceof Double) {
      q = new QuantityDt().setValue((Double) data);
    } else if (data instanceof Integer) {
      q = new QuantityDt().setValue((Integer) data);
    } else if (data instanceof Long) {
      q = new QuantityDt().setValue((Long) data);
    }
    return decorateQuantity(schema, q);
  }

  private static QuantityDt decorateQuantity(DataElement schema, QuantityDt q) {
    if (q != null
        && schema.getElementFirstRep().getPattern() instanceof QuantityDt) {
      QuantityDt ref = (QuantityDt) schema.getElementFirstRep().getPattern();
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


  private static CodeDt buildCode(Map<String, Object> data) {
    return data.containsKey(VALUE)
        ? (CodeDt) new CodeDt().setValue((String) data.get(VALUE))
        : null;
  }

  private static CodeDt buildCodeSimple(Object data) {
    return data instanceof String
        ? (CodeDt) new CodeDt().setValue((String) data)
        : null;
  }


  private static CodingDt buildCoding(DataElement schema, Map<String, Object> data) {
    CodingDt c = data.containsKey(VALUE)
        ? new CodingDt().setCode((String) data.get("code"))
        .setSystem((String) data.get("system"))
        .setVersion((String) data.get("version"))
        .setDisplay((String) data.get("display"))
        : null;
    return decorateCoding(schema, c);
  }

  private static CodingDt buildCodingSimple(DataElement schema, Object data) {
    CodingDt c = data instanceof String
        ? new CodingDt().setCode((String) data)
        : null;
    return decorateCoding(schema, c);
  }

  private static CodingDt decorateCoding(DataElement schema, CodingDt c) {
    if (c != null
        && schema.getElementFirstRep().getPattern() instanceof CodingDt) {
      CodingDt ref = (CodingDt) schema.getElementFirstRep().getPattern();
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


  private static StringDt buildString(Map<String, Object> data) {
    return data.containsKey(VALUE)
        ? (StringDt) new StringDt().setValue((String) data.get(VALUE))
        : null;
  }

  private static StringDt buildStringSimple(Object data) {
    return data instanceof String
        ? (StringDt) new StringDt().setValue((String) data)
        : null;
  }

  private static BooleanDt buildBoolean(Map<String, Object> data) {
    return data.containsKey(VALUE)
        ? (BooleanDt) new BooleanDt().setValue((Boolean) data.get(VALUE))
        : null;
  }

  private static BooleanDt buildBooleanSimple(Object data) {
    return data instanceof Boolean
        ? (BooleanDt) new BooleanDt().setValue((Boolean) data)
        : null;
  }


  private static AttachmentDt buildAttachment(Map<String, Object> data) {
    return data.containsKey("url")
        ? new AttachmentDt().setTitle((String) data.get("title"))
        .setUrl((String) data.get("url"))
        .setSize((Integer) data.get("size"))
        .setLanguage((String) data.get("language"))
        : null;
  }

  private static AttachmentDt buildAttachmentSimple(Object data) {
    return data instanceof String
        ? new AttachmentDt().setUrl((String) data)
        : null;
  }

}
