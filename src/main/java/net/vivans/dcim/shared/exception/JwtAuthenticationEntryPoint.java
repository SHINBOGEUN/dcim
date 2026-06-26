package net.vivans.dcim.shared.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.vivans.dcim.shared.api.ApiResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String jwtError = (String) request.getAttribute("JWT_ERROR");
        String errorMessage;

        if (jwtError != null) {
            log.debug("JWT Authentication Entry Point - Already logged in filter: {}", jwtError);
            errorMessage = "Invalid or expired JWT token";
        } else {
            log.error("JWT Authentication Entry Point - {}", authException.getMessage());
            errorMessage = "Authentication required";
        }

        ApiResponse<Void> errorResponse = ApiResponse.error(401, "Please provide valid credentials", errorMessage);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
