package edu.mayo.kmdp.util.ws;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class WebSessionContextTest {

    @Test
    public void testSetAndGetHeaderSameThread() {
        Map<String, String> testHeaders = Maps.newHashMap();
        testHeaders.put("Test", "1");

        WebSessionContext.setHeaders(testHeaders);

        assertEquals(testHeaders, WebSessionContext.getHeaders());
    }

    @Test
    public void testSetAndGetHeaderDifferentThread() throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Void> t1 = () -> {
            Map<String, String> testHeaders = Maps.newHashMap();
            testHeaders.put("Test", "1");

            WebSessionContext.setHeaders(testHeaders);

            assertEquals(testHeaders, WebSessionContext.getHeaders());

            return null;
        };

        Callable<Void> t2 = () -> {
            Map<String, String> testHeaders = Maps.newHashMap();
            testHeaders.put("Test", "2");

            WebSessionContext.setHeaders(testHeaders);

            assertEquals(testHeaders, WebSessionContext.getHeaders());

            return null;
        };

        executor.invokeAll(Lists.newArrayList(t1, t2)).forEach(future -> {
            try {
                future.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        executor.shutdown();
    }

    @Test
    public void testSetAndGetHeaderDifferentThreadOverlap() throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Void> t1 = () -> {
            Map<String, String> testHeaders = Maps.newHashMap();
            testHeaders.put("Test", "1");

            WebSessionContext.setHeaders(testHeaders);
            System.out.println("Set 1");

            Thread.sleep(100);

            assertEquals(testHeaders, WebSessionContext.getHeaders());
            System.out.println("Checked 1");

            return null;
        };

        Callable<Void> t2 = () -> {
            Map<String, String> testHeaders = Maps.newHashMap();
            testHeaders.put("Test", "2");

            WebSessionContext.setHeaders(testHeaders);
            System.out.println("Set 2");

            assertEquals(testHeaders, WebSessionContext.getHeaders());
            System.out.println("Checked 2");

            return null;
        };

        executor.invokeAll(Lists.newArrayList(t1, t2)).forEach(future -> {
            try {
                future.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        executor.shutdown();
    }

}
