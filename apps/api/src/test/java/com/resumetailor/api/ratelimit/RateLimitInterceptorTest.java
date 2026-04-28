package com.resumetailor.api.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RateLimitInterceptorTest {

    private RateLimitInterceptor interceptor;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() throws Exception {
        interceptor = new RateLimitInterceptor();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));
    }

    @Test
    void blocksAfterTenRequests() throws Exception {
        for (int i = 0; i < 10; i++) {
            assertThat(interceptor.preHandle(request, response, null)).isTrue();
        }
        assertThat(interceptor.preHandle(request, response, null)).isFalse();
    }

    @Test
    void differentIpsHaveSeparateBuckets() throws Exception {
        HttpServletRequest otherRequest = mock(HttpServletRequest.class);
        when(otherRequest.getRemoteAddr()).thenReturn("192.168.1.1");

        for (int i = 0; i < 10; i++) {
            interceptor.preHandle(request, response, null);
        }

        assertThat(interceptor.preHandle(otherRequest, response, null)).isTrue();
    }
}
