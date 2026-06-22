package com.codesmell.api;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyFilterTest {

    @Test
    void allowsRequestsWhenApiKeyIsNotConfigured() throws Exception {
        var filter = new ApiKeyFilter("");
        var chain = new RecordingFilterChain();

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        assertThat(chain.wasCalled()).isTrue();
    }

    @Test
    void allowsRequestsWithConfiguredApiKey() throws Exception {
        var filter = new ApiKeyFilter("secret");
        var request = new MockHttpServletRequest();
        request.addHeader("X-API-Key", "secret");
        var chain = new RecordingFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(chain.wasCalled()).isTrue();
    }

    @Test
    void rejectsRequestsWithMissingApiKey() throws Exception {
        var filter = new ApiKeyFilter("secret");
        var response = new MockHttpServletResponse();
        var chain = new RecordingFilterChain();

        filter.doFilter(new MockHttpServletRequest(), response, chain);

        assertThat(chain.wasCalled()).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).isEqualTo("{\"error\":\"invalid API key\"}");
    }

    private static class RecordingFilterChain implements FilterChain {

        private boolean called;

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) {
            called = true;
        }

        boolean wasCalled() {
            return called;
        }
    }
}
