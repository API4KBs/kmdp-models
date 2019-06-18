package edu.mayo.kmdp.util.ws;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HeaderForwardServerInterceptorTest {

    @Test
    public void setHeader() {
        HashSet<String> headers = Sets.newHashSet("Test");

        HeaderForwardServerInterceptor interceptor = new HeaderForwardServerInterceptor(headers);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Test", "1");

        interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertEquals("1", WebSessionContext.getHeaders().get("Test"));

    }

    @Test
    public void setHeaderNotInList() {
        HashSet<String> headers = Sets.newHashSet("XXXXX");

        HeaderForwardServerInterceptor interceptor = new HeaderForwardServerInterceptor(headers);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Test", "1");

        interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertNull(WebSessionContext.getHeaders().get("Test"));

    }

    @Test
    public void setHeaderInListButNull() {
        HashSet<String> headers = Sets.newHashSet("Test");

        HeaderForwardServerInterceptor interceptor = new HeaderForwardServerInterceptor(headers);

        MockHttpServletRequest request = new MockHttpServletRequest();

        interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertNull(WebSessionContext.getHeaders().get("Test"));

    }

}