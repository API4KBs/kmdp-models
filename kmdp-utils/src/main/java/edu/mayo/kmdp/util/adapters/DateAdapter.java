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

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DateAdapter extends XmlAdapter<String, Date> {

  public static String PATTERN = "yyyy-MM-dd";

  static SimpleDateFormat sdf = new SimpleDateFormat(PATTERN);

  public Date unmarshal(String v) {
    return read(v);
  }

  public static Date read(String v) {
    try {
      return sdf.parse(v);
    } catch (ParseException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static String write(Date v) {
    return v != null ? sdf.format(v) : null;
  }

  public String marshal(Date v) {
    return write(v);
  }

  public static boolean isDate(String s) {
    try {
      sdf.parse(s);
      return true;
    } catch (ParseException e) {
      return false;
    }
  }
}
