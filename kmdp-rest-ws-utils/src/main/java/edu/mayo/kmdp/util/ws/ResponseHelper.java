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
package edu.mayo.kmdp.util.ws;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseHelper {

  public static <T> List<T> getAll(ResponseEntity<List<T>> resp) {
    return isSuccess(resp) ? resp.getBody() : Collections.emptyList();
  }

  public static <T> List<T> aggregate(ResponseEntity<List<T>> resp) {
    return isSuccess(resp) ? resp.getBody() : Collections.emptyList();
  }


  public static <T> Optional<T> get(ResponseEntity<T> resp) {
    return isSuccess(resp) ? Optional.ofNullable(resp.getBody()) : Optional.empty();
  }


  public static boolean isSuccess(ResponseEntity<?> resp) {
    return resp.getStatusCode().is2xxSuccessful();
  }


  public static <T> ResponseEntity<T> succeed(T result) {
    return result == null
        ? ResponseEntity.ok().build()
        : ResponseEntity.ok(result);
  }

  public static <T> ResponseEntity<T> notSupported() {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  public static <T> ResponseEntity<T> fail() {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
  }


  public static <T> ResponseEntity<T> attempt(T result) {
    return attempt(Optional.ofNullable(result));
  }

  public static <T> ResponseEntity<T> attempt(Optional<T> result) {
    return result
        .map(ResponseHelper::succeed)
        .orElse(ResponseEntity.notFound().build());
  }

  public static <X, T> ResponseEntity<T> delegate(Optional<X> delegate,
      Function<X, ResponseEntity<T>> fun) {
    ResponseEntity<T> x = delegate
        .map(fun).orElse(null);
    return delegate
        .map(fun)
        .orElse(notSupported());
  }

  public static <X, T> List<T> aggregate(Collection<X> delegates,
      Function<X, ResponseEntity<List<T>>> mapper) {
    return delegates.stream()
        .map(mapper)
        .map(ResponseHelper::getAll)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  public static <X, T> Optional<T> anyDo(Collection<X> delegates,
      Function<X, ResponseEntity<T>> mapper) {
    return delegates.stream()
        .map(mapper)
        .map(ResponseHelper::get)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findAny();
  }

  public static <T> boolean matches(ResponseEntity<T> resp, Predicate<T> condition) {
    return get(resp).filter(condition).isPresent();
  }

  public static <X, T> ResponseEntity<T> map(ResponseEntity<X> resp, Function<X, T> mapper) {
    return attempt(get(resp)
        .map(mapper));
  }

  public static <X, T> ResponseEntity<T> flatMap(ResponseEntity<X> resp,
      Function<X, ResponseEntity<T>> mapper) {
    return get(resp)
        .map(mapper)
        .orElse(fail());
  }

  public static <X> Optional<X> anyAble(Collection<X> delegates, Predicate<X> filter) {
    return delegates.stream()
        .filter(filter)
        .findAny();
  }

  public static <X> ResponseEntity<List<X>> collect(
      Stream<ResponseEntity<X>> responses) {
    return succeed(
        responses
        .map(ResponseHelper::get)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList()));
  }
}
