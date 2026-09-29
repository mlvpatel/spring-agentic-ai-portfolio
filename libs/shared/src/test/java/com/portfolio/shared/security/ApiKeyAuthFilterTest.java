package com.portfolio.shared.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ApiKeyAuthFilterTest {

    private static final String CONFIGURED = "configured-api-key-for-unit-tests-only";

    private final ApiKeyProperties properties = properties(CONFIGURED);
    private final ApiKeyAuthFilter filter = new ApiKeyAuthFilter(properties);

    @Test
    @DisplayName("Rejects missing API key")
    void rejectsMissingKey() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/demo");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Rejects wrong API key")
    void rejectsWrongKey() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/demo");
        request.addHeader("X-API-Key", "wrong-key");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Rejects ey* Bearer when not the configured key")
    void rejectsEyBearer() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/demo");
        request.addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.fake.sig");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Rejects valid-test-token when configured key differs")
    void rejectsValidTestToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/demo");
        request.addHeader("Authorization", "Bearer valid-test-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Accepts configured X-API-Key")
    void acceptsConfiguredKey() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/demo");
        request.addHeader("X-API-Key", CONFIGURED);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(chain).doFilter(request, response);
    }

    private static ApiKeyProperties properties(String apiKey) {
        ApiKeyProperties props = new ApiKeyProperties();
        props.setApiKey(apiKey);
        return props;
    }
}
