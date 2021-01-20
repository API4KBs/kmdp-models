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
package edu.mayo.kmdp.id.adapter;

import java.util.HashMap;
import java.util.Map;
import org.jvnet.jaxb2_commons.lang.CopyStrategy2;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;
import org.omg.spec.api4kp._20200801.datatypes.Bindings;

// May not be necessary if copyTo is not enabled
public class CopyableHashMap<K, V, M extends CopyableHashMap<K,V,M>> extends HashMap<K, V> {

  public CopyableHashMap() {

  }

  public CopyableHashMap(Map<K, V> src) {
    super(src);
  }

  @SuppressWarnings("unchecked")
  public M addEntry(K key, V value) {
    super.put(key,value);
    return (M) this;
  }

  @SuppressWarnings("unchecked")
  public java.lang.Object copyTo(ObjectLocator locator, java.lang.Object target,
      CopyStrategy2 strategy) {
    if (target instanceof Map) {
      Map targetMap = (Map) target;
      this.forEach((k, v) -> targetMap.put(k, strategy.copy(locator, v, true)));
    }
    return target;
  }

  public static Bindings<String, String> toBinds(Object... vars) {
    if ((vars.length & 1) == 0) {
      Bindings<String, String> binds = new Bindings<>();
      for (int j = 0; j < vars.length; j = j + 2) {
        binds.put(vars[j].toString(), vars[j + 1].toString());
      }
      return binds;
    } else {
      throw new IllegalArgumentException(
          "Bindings must consist in an even number of key/value pairs");
    }
  }
}
