package edu.mayo.kmdp.util;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.mayo.kmdp.util.concurrent.LatchedScheduleExecutor;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class ScheduledExecutorTest {

  LatchedScheduleExecutor<String> saver;

  @Test
  void testScheduleWithDelay() {
    Set<String> data = new HashSet<>();
    saver = new LatchedScheduleExecutor<>("Test", 1, () -> write(data));

    saver.scheduleExecution();
    await()
        .atMost(5, TimeUnit.SECONDS)
        .pollDelay(500, TimeUnit.MILLISECONDS)
        .until(() -> !saver.isTaskPending());
    assertFalse(data.isEmpty());
  }

  @Test
  void testSaveNow() {
    Set<String> data = new HashSet<>();
    saver = new LatchedScheduleExecutor<>("Test", 10000, () -> write(data));

    try {
      assertTrue(saver.executeNow().contains("Msg"));
      await()
          .atMost(1, TimeUnit.SECONDS)
          .pollDelay(250, TimeUnit.MILLISECONDS)
          .until(() -> !saver.isTaskPending());
      assertFalse(data.isEmpty());
    } catch (ExecutionException | InterruptedException e) {
      fail(e.getMessage(), e);
    }
  }

  @Test
  void testSaveCancel() {
    Set<String> data = new HashSet<>();
    saver = new LatchedScheduleExecutor<>("Test", 2, () -> write(data));
    saver.scheduleExecution();
    assertTrue(saver.isTaskPending());

    try {
      saver.shutdown();
      await().pollDelay(100, TimeUnit.MILLISECONDS)
          .until(() -> !saver.isTaskPending());
      assertTrue(data.isEmpty());
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Test
  void testSaveShutdown() {
    Set<String> data = new HashSet<>();
    saver = new LatchedScheduleExecutor<>("Test", 2, () -> write(data));

    saver.scheduleExecution();

    assertTrue(saver.isTaskPending());
    try {
      saver.shutdown();
      await().pollDelay(100, TimeUnit.MILLISECONDS)
          .until(() -> !saver.isTaskPending());
      assertTrue(data.isEmpty());
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

  }

  @Test
  void testSaveMultiple() {
    Set<String> data = new HashSet<>();
    saver = new LatchedScheduleExecutor<>("Test", 1, () -> write(data));

    saver.scheduleExecution();
    await().until(() -> !saver.isTaskPending());

    saver.scheduleExecution();
    await().until(() -> !saver.isTaskPending());

    assertEquals(2, data.size());
  }

  @Test
  void testSaveRebounded() {
    Set<String> data = new HashSet<>();
    saver = new LatchedScheduleExecutor<>("Test", 2, () -> write(data));

    saver.scheduleExecution();
    await()
        .atLeast(100, TimeUnit.MILLISECONDS)
        .atMost(1, TimeUnit.SECONDS)
        .until(() -> saver.isTaskPending());

    saver.scheduleExecution();
    assertTrue(saver.isTaskPending());
    await()
        .atMost(4, TimeUnit.SECONDS)
        .pollDelay(200, TimeUnit.MILLISECONDS)
        .until(() -> !saver.isTaskPending());

    assertEquals(1, data.size());
  }


  @Test
  void testSaveNowOverSchedule() {
    Set<String> data = new HashSet<>();
    saver = new LatchedScheduleExecutor<>("Test", 2, () -> write(data));

    try {
      saver.scheduleExecution();
      saver.executeNow();

      await().until(() -> !saver.isTaskPending());

      assertEquals(1, data.size());
    } catch (ExecutionException | InterruptedException e) {
      fail(e.getMessage(), e);
    }
  }

  private String write(Set<String> data) {
    String msg = "Msg AT " + System.currentTimeMillis();
    data.add(msg);
    return msg;
  }

}
