package edu.mayo.kmdp.util.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that ensures a Callable method is executed just once in a multi-threaded
 * environment
 *
 * @param <T> The return type of the Callable method
 */
public class Once<T> {

  Logger logger = LoggerFactory.getLogger(Once.class);

  private final String name;
  private final Callable<T> method;
  private final Semaphore signal = new Semaphore(1, true);
  private final AtomicBoolean done = new AtomicBoolean(false);
  private T result;

  /**
   * Constructor
   * @param name a logical identifier for the operation (used for logging purposes)
   * @param method the function to be called just once
   */
  public Once(String name, Callable<T> method) {
    this.method = method;
    this.name = name;
  }

  /**
   * Constructor
   * @param method the function to be called just once
   */
  public Once(Callable<T> method) {
    this("(ONCE)", method );
  }

  /**
   * Lets the current thread run the wrapped Callable,
   * if the callable has not been executed yet.
   *
   * Suspends other threads trying to execute the method,
   * while the callable is being executed.
   *
   * Returns immediately after the first completion
   */
  public T executeIfNotDone() {
    Callable<T> runner = () -> {
      if (done.get()) {
        return result;
      }
      signal.acquireUninterruptibly();
      if (done.get()) {
        signal.release();
        return result;
      }
      try {
        logger.trace("[{}] executed by {}", name, Thread.currentThread().getName());
        result = method.call();
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        done.set(true);
        signal.release();
      }
      return result;
    };

    try {
      return runner.call();
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return null;
    }
  }

  /**
   * Observes the completion state
   * @return true if the one-time execution has been completed
   */
  public boolean isDone() {
    return done.get();
  }


}
