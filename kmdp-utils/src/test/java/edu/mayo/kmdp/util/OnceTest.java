package edu.mayo.kmdp.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.mayo.kmdp.util.concurrent.Once;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class OnceTest {

  @Test
  void testDoJustOnce() {
    ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
    int numTasks = 5;
    int numSubmits = 20;

    Set<Once<String>> onces = IntStream.rangeClosed(1, numTasks)
        .mapToObj(j -> new Once<>("Test" + j, () -> addTo(map, j)))
        .collect(Collectors.toSet());

    ExecutorService executor = Executors.newFixedThreadPool(2);
    for (int j = 0; j < numSubmits; ++j) {
      onces.forEach(task -> executor.submit(task::executeIfNotDone));
    }

    try {
      executor.shutdown();
      assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    map.forEachKey(1, System.out::println);
    assertEquals(numTasks, map.size());
  }

  @Test
  void testWithResult() {
    int x = new Once<>("Test", () -> 42).executeIfNotDone();
    assertEquals(42, x);
  }

  @Test
  void testWithException() {
    List<String> l = new ArrayList<>();
    Once<Void> once = new Once<>("Fail",
        () -> {
          addTo(l);
          throw new Exception("Deliberate Test Exception");
        });

    ExecutorService executor = Executors.newFixedThreadPool(10);
    for (int j = 0; j < 5; ++j) {
      executor.submit(once::executeIfNotDone);
    }
    executor.shutdown();
    try {
      assertTrue(executor.awaitTermination(1, TimeUnit.SECONDS));
    } catch (InterruptedException e) {
      fail(e);
    }

    assertEquals(1, l.size());
  }

  private void addTo(List<String> list) {
    list.add(Thread.currentThread().getName());
  }

  private String addTo(ConcurrentHashMap<String, String> list, int j) {
    return list.put(Thread.currentThread().getName() + "-" + j, "");
  }
}
