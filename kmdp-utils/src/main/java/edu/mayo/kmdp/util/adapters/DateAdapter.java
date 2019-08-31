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
package edu.mayo.kmdp.util.adapters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class DateAdapter extends XmlAdapter<String, Date> {

  private static final Logger logger = LoggerFactory.getLogger(DateAdapter.class);

  public static final String PATTERN = "yyyy-MM-dd";

  private static DateAdapter instance = new DateAdapter();

  public static DateAdapter instance() {
    return instance;
  }

  SimpleDateFormat sdf = new SimpleDateFormat(PATTERN);

  public Date unmarshal(String v) {
    return read(v);
  }

  public Date read(String v) {
    try {
      return sdf.parse(v);
    } catch (ParseException e) {
      logger.error(e.getMessage(),e);
      return null;
    }
  }

  public String write(Date v) {
    return v != null ? sdf.format(v) : null;
  }

  public String marshal(Date v) {
    return write(v);
  }

  public boolean isDate(String s) {
    try {
      sdf.parse(s);
      return true;
    } catch (ParseException e) {
      return false;
    }
  }
}
