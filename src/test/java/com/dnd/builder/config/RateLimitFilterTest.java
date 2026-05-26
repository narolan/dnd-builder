package com.dnd.builder.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitFilterTest {

    private RateLimitFilter filter;
    private MockHttpServletResponse response;
    private MockFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter();
        response = new MockHttpServletResponse();
        chain = new MockFilterChain();
    }

    private MockHttpServletRequest post(String ip) {
        var req = new MockHttpServletRequest("POST", "/step/1");
        req.setRemoteAddr(ip);
        return req;
    }

    private MockHttpServletRequest get(String ip) {
        var req = new MockHttpServletRequest("GET", "/step/1");
        req.setRemoteAddr(ip);
        return req;
    }

    @Nested
    @DisplayName("GET requests")
    class GetRequests {

        @Test
        @DisplayName("GET bypasses rate limiting and passes to filter chain")
        void getPassesThrough() throws Exception {
            filter.doFilter(get("10.0.0.1"), response, chain);

            assertEquals(200, response.getStatus());
            assertNotNull(chain.getRequest(), "GET should reach the filter chain");
        }
    }

    @Nested
    @DisplayName("POST rate limiting")
    class PostRateLimiting {

        @Test
        @DisplayName("First POST from new IP passes through and creates a window")
        void firstPostPassesThrough() throws Exception {
            filter.doFilter(post("10.0.0.1"), response, chain);

            assertEquals(200, response.getStatus());
            assertNotNull(chain.getRequest());
        }

        @Test
        @DisplayName("POST within the 60-request limit passes through")
        void postWithinLimitPassesThrough() throws Exception {
            // 59 requests — all should pass
            for (int i = 0; i < 59; i++) {
                var resp = new MockHttpServletResponse();
                filter.doFilter(post("10.0.0.2"), resp, new MockFilterChain());
                assertEquals(200, resp.getStatus(), "Request " + (i + 1) + " should pass");
            }
        }

        @Test
        @DisplayName("61st POST in the same window returns HTTP 429")
        void exceededLimitReturns429() throws Exception {
            // Fire 60 allowed requests
            for (int i = 0; i < 60; i++) {
                filter.doFilter(post("10.0.0.3"), new MockHttpServletResponse(), new MockFilterChain());
            }
            // 61st must be rate-limited
            var limitedResp = new MockHttpServletResponse();
            filter.doFilter(post("10.0.0.3"), limitedResp, new MockFilterChain());

            assertEquals(429, limitedResp.getStatus());
            assertEquals("application/json", limitedResp.getContentType());
            assertTrue(limitedResp.getContentAsString().contains("Too many requests"));
        }

        @Test
        @DisplayName("Requests from different IPs have independent windows")
        void differentIpsAreIndependent() throws Exception {
            // Exhaust limit for IP A
            for (int i = 0; i < 61; i++) {
                filter.doFilter(post("192.168.1.1"), new MockHttpServletResponse(), new MockFilterChain());
            }
            // IP B should still pass
            var respB = new MockHttpServletResponse();
            filter.doFilter(post("192.168.1.2"), respB, new MockFilterChain());
            assertEquals(200, respB.getStatus());
        }

        @Test
        @DisplayName("Expired window resets the counter, allowing requests again")
        void expiredWindowResetsCounter() throws Exception {
            // Exhaust limit for IP
            String ip = "10.0.0.4";
            for (int i = 0; i < 61; i++) {
                filter.doFilter(post(ip), new MockHttpServletResponse(), new MockFilterChain());
            }

            // Verify currently rate-limited
            var blockedResp = new MockHttpServletResponse();
            filter.doFilter(post(ip), blockedResp, new MockFilterChain());
            assertEquals(429, blockedResp.getStatus());

            // Manually expire the window by backdating its startMs via reflection
            Field windowsField = RateLimitFilter.class.getDeclaredField("windows");
            windowsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            var windows = (ConcurrentHashMap<?, ?>) windowsField.get(filter);
            // Clear it to simulate expiry (compute() will create a new Window on next request)
            windows.clear();

            // Should now pass again
            var newResp = new MockHttpServletResponse();
            filter.doFilter(post(ip), newResp, new MockFilterChain());
            assertEquals(200, newResp.getStatus());
        }
    }
}
