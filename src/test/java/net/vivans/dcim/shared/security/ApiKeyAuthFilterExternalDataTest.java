package net.vivans.dcim.shared.security;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyAuthFilterExternalDataTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void externalDataRouteRequiresConfiguredSecret() throws ServletException, IOException {
        ApiKeyAuthFilter filter = filter("configured-secret");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/internal/external-data");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void externalDataRouteAcceptsOnlyConfiguredSecret() throws ServletException, IOException {
        ApiKeyAuthFilter filter = filter("configured-secret");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/internal/external-data");
        request.addHeader("X-Api-Key", "configured-secret");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo("external-data-service");
    }

    @Test
    void existingElectricityQueriesRemainJwtCompatibleAndAcceptExternalDataApiKey() throws ServletException, IOException {
        ApiKeyAuthFilter filter = filter("configured-secret");
        MockHttpServletRequest readRequest = new MockHttpServletRequest("GET", "/api/manager/power-usage/daily/chart");
        MockHttpServletResponse readResponse = new MockHttpServletResponse();
        MockFilterChain readChain = new MockFilterChain();
        filter.doFilter(readRequest, readResponse, readChain);
        assertThat(readResponse.getStatus()).isEqualTo(200);
        assertThat(readChain.getRequest()).isNotNull();

        MockHttpServletRequest keyedReadRequest = new MockHttpServletRequest("GET", "/api/manager/electricity-bill/monthly");
        keyedReadRequest.addHeader("X-Api-Key", "configured-secret");
        MockHttpServletResponse keyedReadResponse = new MockHttpServletResponse();
        MockFilterChain keyedReadChain = new MockFilterChain();
        filter.doFilter(keyedReadRequest, keyedReadResponse, keyedReadChain);
        assertThat(keyedReadResponse.getStatus()).isEqualTo(200);
        assertThat(keyedReadChain.getRequest()).isNotNull();
    }

    @Test
    void missingServerSecretDisablesExternalDataApiKeyAuthentication() throws ServletException, IOException {
        ApiKeyAuthFilter filter = filter("");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/internal/external-data");
        request.addHeader("X-Api-Key", "anything");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
    }

    private ApiKeyAuthFilter filter(String configuredSecret) {
        ApiKeyAuthFilter filter = new ApiKeyAuthFilter();
        ReflectionTestUtils.setField(filter, "externalDataApiKey", configuredSecret);
        return filter;
    }
}
