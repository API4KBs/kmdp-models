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
package edu.mayo.kmdp.terms.generator.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TransitiveClosure {

  private TransitiveClosure() {
  }

  public static <T> Map<T, List<T>> closure(Map<T, Set<T>> hierarchy) {
    Set<T> allNodes = new HashSet<>(hierarchy.keySet());
    hierarchy.values().forEach(allNodes::addAll);
    ArrayList<T> list = new ArrayList<>(allNodes);

    int num = list.size();
    boolean[][] closure = getClosure(hierarchy,list,num);

    Map<T, List<T>> closedHierarchy = new HashMap<>();
    for (int i = 0; i < num; i++) {
      T t = list.get(i);
      List<T> ancestors = new ArrayList<>();
      for (int k = 0; k < num; k++) {
        if (closure[i][k] && i != k) {
          ancestors.add(list.get(k));
        }
      }
      closedHierarchy.put(t, ancestors);
    }
    return closedHierarchy;
  }

  private static <T> boolean[][] getClosure(Map<T, Set<T>> hierarchy, ArrayList<T> list, int num) {
    boolean[][] closure = new boolean[num][num];

    initClosure(closure, list, hierarchy, num);

    propagateClosure(closure, num);

    return closure;
  }

  private static void propagateClosure(boolean[][] closure, int num) {
    for (int i = 0; i < num; i++) {
      for (int j = 0; j < num; j++) {
        if (closure[j][i]) {
          for (int k = 0; k < num; k++) {
            closure[j][k] = closure[j][k] || closure[j][i] && closure[i][k];
          }
        }
      }
    }
  }

  private static <T> void initClosure(boolean[][] closure, ArrayList<T> list, Map<T, Set<T>> hierarchy, int num) {
    for (int i = 0; i < num; i++) {
      for (int j = 0; j < num; j++) {
        if (hierarchy.getOrDefault(list.get(i), Collections.emptySet()).contains(list.get(j))) {
          closure[i][j] = true;
        }
        closure[i][i] = true;
      }
    }
  }

}
