package edu.mayo.kmdp.util.concurrent;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that schedules the execution of a callable method with an optional delay, ensuring
 * that no more than one request is active at any time
 *
 * @param <T> the return type of the callable method
 */
public class LatchedScheduleExecutor<T> {

  private static final Logger logger = LoggerFactory.getLogger(LatchedScheduleExecutor.class);

  private final String name;

  private final AtomicBoolean taskPending = new AtomicBoolean(false);
  private final ScheduledExecutorService execs = Executors.newScheduledThreadPool(1);
  private Deque<ScheduledFuture<T>> futures = new LinkedList<>();

  private final Callable<T> method;

  private int delay;
  private TimeUnit delayUnit;

  /**
   * Constructor
   *
   * @param delay  the delay, in seconds,
   * @param method the Callable method
   */
  public LatchedScheduleExecutor(int delay, Callable<T> method) {
    this("", delay, method);
  }

  /**
   * Constructor
   *
   * @param name   a name for this scheduler (used for logging purposes)
   * @param delay  the delay, in seconds,
   * @param method the Callable method
   */
  public LatchedScheduleExecutor(String name, int delay, Callable<T> method) {
    this(name, delay, TimeUnit.SECONDS, method);
  }

  /**
   * Constructor
   *
   * @param name      a name for this scheduler (used for logging purposes)
   * @param delay     the delay, in units
   * @param delayUnit the TimeUnit delay
   * @param method    the Callable method
   */
  public LatchedScheduleExecutor(String name, int delay, TimeUnit delayUnit, Callable<T> method) {
    this.method = wrap(method);
    this.delay = delay;
    this.delayUnit = delayUnit;
    this.name = name;
  }

  /**
   * Wraps the client-provided callable method with additional internal state management
   *
   * @param saveFunction the client-provided callable
   * @return a Callable that invokes the client-provided one, adding state management
   */
  private Callable<T> wrap(Callable<T> saveFunction) {
    return () -> {
      var ans = saveFunction.call();
      taskPending.set(false);
      return ans;
    };
  }

  /**
   * Schedules the execution of the client provided callable, at the programmed delay
   */
  public synchronized void scheduleExecution() {
    if (taskPending.getAndSet(true)) {
      cancelExecution(false);
    }
    scheduleExecution(delay)
        .ifPresent(futures::add);
  }

  /**
   * Schedules the execution of the Callable method after a given delay
   *
   * @param delay the delay
   * @return A scheduled future, if delay is non-negative
   */
  private Optional<ScheduledFuture<T>> scheduleExecution(int delay) {
    logger.trace("[{}] Scheduling in {} seconds", name, delay);
    if (delay < 0) {
      return Optional.empty();
    }
    taskPending.set(true);
    return Optional.of(execs.schedule(method, delay, delayUnit));
  }

  /**
   * cancels the currently scheduled execution, if any
   */
  public synchronized void cancelExecution(boolean withInterrupt) {
    if (taskPending.getAndSet(false)) {
      futures.forEach(t -> t.cancel(withInterrupt));
      futures.clear();
    }
  }

  /**
   * Executes the Callable method with no delay
   *
   * @return the result of the callable invocation
   * @throws ExecutionException   an Exception derived from the call
   * @throws InterruptedException an Excetion due to threading
   */
  public synchronized T executeNow() throws ExecutionException, InterruptedException {
    if (taskPending.getAndSet(true)) {
      cancelExecution(false);
    }
    Optional<ScheduledFuture<T>> s = scheduleExecution(0);
    return s.isPresent()
        ? s.get().get()
        : null;
  }

  /**
   * @return true if the call is scheduled and pending
   */
  public boolean isTaskPending() {
    return taskPending.get();
  }

  /**
   * Closes the execution
   */
  public void shutdown() throws InterruptedException {
    cancelExecution(true);
    execs.shutdown();
    execs.awaitTermination(1, TimeUnit.MINUTES);
  }
}