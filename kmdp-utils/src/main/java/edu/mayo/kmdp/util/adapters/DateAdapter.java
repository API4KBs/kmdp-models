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

import edu.mayo.kmdp.util.DateTimeUtil;
import java.util.Date;
import javax.xml.bind.annotation.adapters.XmlAdapter;


public class DateAdapter extends XmlAdapter<String, Date> {

  private static final String XML_DATETIME_PATTERN = "yyyy-MM-dd'T'hh:mm:ss";

  private static DateAdapter instance = new DateAdapter();

  public static DateAdapter instance() {
    return instance;
  }

  public Date unmarshal(String v) {
    return read(v);
  }

  public Date read(String v) {
    return DateTimeUtil.parseDate(v, XML_DATETIME_PATTERN);
  }

  public String write(Date v) {
    return v != null ? DateTimeUtil.format(v, XML_DATETIME_PATTERN) : null;
  }

  public String marshal(Date v) {
    return write(v);
  }

}
