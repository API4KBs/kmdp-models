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

import static edu.mayo.kmdp.util.Util.as;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamUtil {

  private StreamUtil() {}


  public static <T> Stream<T> trimStream(Optional<T> opt) {
    return opt.map(Stream::of).orElseGet(Stream::empty);
  }

  public static <T> Function<Object,Stream<T>> filterAs(Class<T> type) {
    return x -> trimStream(as(x,type));
  }

  public static <T,X> Set<X> mapToSet(Collection<T> source, Function<T,X> mapper) {
    return source.stream()
        .map(mapper)
        .collect(Collectors.toSet());
  }

}
